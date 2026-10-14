#!/bin/bash
# 第7回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -g -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# 1. 本数と深さを変えて、各3回
echo "kind,count,depth,rss_delta_mb,heap_delta_mb" > threads.csv
for depth in 10 100; do
  for kind_counts in "platform 500 1000 2000 4000" "virtual 500 1000 2000 4000 10000 100000"; do
    set -- $kind_counts; kind=$1; shift
    for count in "$@"; do
      for i in 1 2 3; do java -cp "$WORK" ManyThreads $kind $count $depth >> threads.csv; done
    done
  done
done

# 2. 0本・100本(スレッドを作る前の差を見る)
: > threads-small.csv
for kind in platform virtual; do
  for count in 0 100; do
    for i in 1 2 3; do java -cp "$WORK" ManyThreads $kind $count 10 >> threads-small.csv; done
  done
done

# 3. -Xss を変えても増分が変わらないこと
: > threads-xss.csv
for xss in 256k 1m; do
  for i in 1 2 3; do echo "$xss,$(java -Xss$xss -cp "$WORK" ManyThreads platform 4000 10)" >> threads-xss.csv; done
done

# 4. MALLOC_ARENA_MAX=2
: > threads-arena2.csv
for count in 100 500 1000 2000 4000; do
  for i in 1 2; do MALLOC_ARENA_MAX=2 java -cp "$WORK" ManyThreads platform $count 10 >> threads-arena2.csv; done
done

# 5. cgroup の pids.max
echo "pids.max (init.scope): $(cat /sys/fs/cgroup/init.scope/pids.max)" > limits.txt

# 6. NMT と クラスヒストグラム (wait を付けて、30秒のうちに jcmd で調べる)
inspect() {  # inspect <出力ファイル> <jcmdの引数...> -- <ManyThreadsの引数...>
  local out=$1; shift
  local jcmd_args=()
  while [ "$1" != "--" ]; do jcmd_args+=("$1"); shift; done; shift
  java -XX:NativeMemoryTracking=summary -cp "$WORK" ManyThreads "$@" wait > "$WORK/inspect.out" &
  local pid=$!
  until grep -q '^pid=' "$WORK/inspect.out"; do sleep 0.5; done
  if [ "${jcmd_args[0]}" = GC.class_histogram ]; then
    jcmd "$pid" "${jcmd_args[@]}" | head -9 > "$out"
  else
    { jcmd "$pid" "${jcmd_args[@]}"; grep VmRSS /proc/$pid/status; } > "$out"
  fi
  kill "$pid" 2>/dev/null || true
  wait "$pid" 2>/dev/null || true
}
inspect nmt-platform-0.txt VM.native_memory summary scale=KB -- platform 0 10
inspect nmt-platform-500.txt VM.native_memory summary scale=KB -- platform 500 10
inspect nmt-platform-4000.txt VM.native_memory summary scale=KB -- platform 4000 10
inspect nmt-virtual-100000.txt VM.native_memory summary scale=KB -- virtual 100000 10
inspect histogram-virtual-100000-depth10.txt GC.class_histogram -- virtual 100000 10
inspect histogram-virtual-100000-depth100.txt GC.class_histogram -- virtual 100000 100

# 7. スタックごとのRSS(/proc/<pid>/smaps): 通常 / -Xint / -XX:StackShadowPages=10
for name in default xint shadow10; do
  case $name in
    default) opts="";;
    xint) opts="-Xint";;
    shadow10) opts="-XX:StackShadowPages=10";;
  esac
  java $opts -cp "$WORK" ManyThreads platform 4000 10 wait > "$WORK/stack.out" &
  pid=$!
  until grep -q '^pid=' "$WORK/stack.out"; do sleep 0.5; done
  { head -1 "$WORK/stack.out"; python3 stack-rss.py "$pid"; } > stack-rss-$name.txt
  kill "$pid" 2>/dev/null || true
  wait "$pid" 2>/dev/null || true
done
rm -rf "$WORK"
