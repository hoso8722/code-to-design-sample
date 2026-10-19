#!/bin/bash
# 第8回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
# コンテナには rootless の Podman を使い、ホストのJDKを /jdk に読み取り専用で渡す
set -e
cd "$(dirname "$0")"
JDK=$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")
WORK=$(mktemp -d)
mkdir -p "$WORK/app" logs
javac -g -d "$WORK/app" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"
IMAGE=debian:bookworm-slim

gc_option() {
  case $1 in
    serial) echo "-XX:+UseSerialGC";;
    parallel) echo "-XX:+UseParallelGC";;
    g1) echo "-XX:+UseG1GC";;
    zgc) echo "-XX:+UseZGC";;
    zgcgen) echo "-XX:+UseZGC -XX:+ZGenerational";;   # JDK 21 では、世代別ZGCを選ぶ指定
  esac
}
# 確保待ち(Allocation Stall)の合計ミリ秒
stall_ms() { grep -o 'Allocation Stall.* [0-9.]*ms' "$1" | awk '{gsub("ms","",$NF); s+=$NF} END {printf "%d", s}'; }
stall_count() { grep -c 'Allocation Stall' "$1" || true; }

# 1. 4つのGC × 3回 (--memory=2g --cpus=4, ヒープ1GB)
echo "gc,run,elapsed_ms,peak_rss_mb" > gc-bench.csv
: > stalls.txt
for gc in serial parallel g1 zgc zgcgen; do
  for run in 1 2 3; do
    out=$(podman run --rm --memory=2g --cpus=4 -v $JDK:/jdk:ro -v $WORK/app:/app $IMAGE \
      /jdk/bin/java -Xms1g -Xmx1g $(gc_option $gc) \
      -Xlog:safepoint:file=/app/safepoint.log -Xlog:gc:file=/app/gc.log -cp /app GcBench)
    echo "$gc,$run,$(echo "$out" | sed -n 's/.*elapsed_ms=\([0-9]*\).*/\1/p'),$(echo "$out" | sed -n 's/.*peak_rss_mb=\([0-9]*\).*/\1/p')" >> gc-bench.csv
    cp "$WORK/app/safepoint.log" logs/safepoint-$gc-$run.log
    cp "$WORK/app/gc.log" logs/gc-$gc-$run.log
    case $gc in zgc|zgcgen) echo "$gc-$run stall_total_ms=$(stall_ms logs/gc-$gc-$run.log) count=$(stall_count logs/gc-$gc-$run.log)" >> stalls.txt;; esac
  done
done
python3 parse-safepoints.py safepoint > pauses.csv

# 2. ヒープに余裕を持たせる (--memory=4g, ヒープ3GB, G1・ZGC・Parallel × 3回)
echo "gc,run,elapsed_ms,peak_rss_mb" > gc-bench-3g.csv
for gc in g1 zgc zgcgen parallel; do
  for run in 1 2 3; do
    out=$(podman run --rm --memory=4g --cpus=4 -v $JDK:/jdk:ro -v $WORK/app:/app $IMAGE \
      /jdk/bin/java -Xms3g -Xmx3g $(gc_option $gc) \
      -Xlog:safepoint:file=/app/safepoint.log -Xlog:gc:file=/app/gc.log -cp /app GcBench)
    echo "$gc,$run,$(echo "$out" | sed -n 's/.*elapsed_ms=\([0-9]*\).*/\1/p'),$(echo "$out" | sed -n 's/.*peak_rss_mb=\([0-9]*\).*/\1/p')" >> gc-bench-3g.csv
    cp "$WORK/app/safepoint.log" logs/safepoint3g-$gc-$run.log
    cp "$WORK/app/gc.log" logs/gc3g-$gc-$run.log
    echo "3g $gc-$run stall_ms=$(stall_ms logs/gc3g-$gc-$run.log)" >> stalls.txt
  done
done
python3 parse-safepoints.py safepoint3g > pauses-3g.csv

# 3. コンテナの制限と、ヒープの上限・GC
echo "memory,cpus,option,max_heap_mb,gc,active_processors" > container-heap.csv
for mem in 256m 512m 1g 1792m 2g 4g; do
  for cpus in 1 2 4; do
    for opt in default -XX:MaxRAMPercentage=75; do
      flags=""; [ $opt != default ] && flags=$opt
      out=$(podman run --rm --memory=$mem --cpus=$cpus -v $JDK:/jdk:ro $IMAGE \
        /jdk/bin/java $flags -XX:+PrintFlagsFinal -version 2>/dev/null)
      heap=$(echo "$out" | awk '$2=="MaxHeapSize" {print $4/1048576}')
      gc=$(echo "$out" | awk '$1=="bool" && $2 ~ /^Use(Serial|Parallel|G1|Z)GC$/ && $4=="true" {print $2}')
      ap=$(echo "$out" | awk '$2=="ActiveProcessorCount" {print $4}')
      echo "$mem,$cpus,$opt,$heap,$gc,$ap" >> container-heap.csv
    done
  done
done

# 4. JVMが読み取ったコンテナの制限
: > container-cpus.txt
for cpus in 1 2 4; do
  out=$(podman run --rm --memory=2g --cpus=$cpus -v $JDK:/jdk:ro $IMAGE /jdk/bin/java -XshowSettings:system -version 2>&1)
  echo "    Effective CPU Count: $(echo "$out" | sed -n 's/.*Effective CPU Count: //p')     Memory Limit: $(echo "$out" | sed -n 's/.*Memory Limit: //p')  (--cpus=$cpus)" >> container-cpus.txt
done

# 5. ヒープの外で制限を超えると (--memory=512m, スワップなし, CPU 1 なのでSerial GC)
oom_run() {  # oom_run <MaxRAMPercentage> <Direct BufferのMB>
  set +e
  podman run --name fill-oom --memory=512m --memory-swap=512m --cpus=1 -v $JDK:/jdk:ro -v $WORK/app:/app $IMAGE \
    sh -c "/jdk/bin/java -XX:MaxRAMPercentage=$1 -cp /app FillMemory 300 $2; code=\$?; echo exit code: \$code; cat /sys/fs/cgroup/memory.events | sed 's/^/events: /'; exit \$code" \
    >> container-oom.txt 2>&1
  set -e
  echo "podman: $(podman inspect fill-oom --format 'ExitCode={{.State.ExitCode}} OOMKilled={{.State.OOMKilled}}')" >> container-oom.txt
  podman rm fill-oom > /dev/null
}
: > container-oom.txt
for pct in 75 50; do
  echo "== --memory=512m --cpus=1 -XX:MaxRAMPercentage=$pct, heap 300MB + direct 200MB" >> container-oom.txt
  oom_run $pct 200
done
# Direct Buffer が150MBのとき(合計が制限にほぼ並ぶ)は、3回実行する
for i in 1 2 3; do
  echo "== --memory=512m --cpus=1 -XX:MaxRAMPercentage=75, heap 300MB + direct 150MB (run $i)" >> container-oom.txt
  oom_run 75 150
done

# 6. ヒープ全体に書き込んだときのRSS(-Xms1g -Xmx1g -XX:+AlwaysPreTouch)
: > zgc-rss.txt
for gc in zgc zgcgen g1; do
  podman run --rm --memory=2g --cpus=4 -v $JDK:/jdk:ro -v $WORK/app:/app $IMAGE \
    sh -c "/jdk/bin/java -Xms1g -Xmx1g -XX:+AlwaysPreTouch $(gc_option $gc) -cp /app Idle & sleep 4; echo -n '$(gc_option $gc): '; grep -E 'VmRSS|RssAnon|RssFile|RssShmem' /proc/\$!/status | tr -s ' \t' ' ' | tr '\n' ' '; echo" >> zgc-rss.txt
done
rm -rf "$WORK"
