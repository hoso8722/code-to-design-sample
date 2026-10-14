#!/bin/bash
# 第1回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
# 使い方: ./collect.sh
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -g -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# 1. ヒープとRSS(コンパイルして3回、ソースファイルモードで1回)
{
  for i in 1 2 3; do java -cp "$WORK" HeapVsRss; echo; done
  echo "--- source-file mode"
  java src/HeapVsRss.java
} > heap-vs-rss.txt

# 2. 保持するデータを増やしていく
java -cp "$WORK" AllocSteps > alloc-steps.csv

# 3. NMT(何も保持しない場合と、256MB保持する場合)
for mb in 0 256; do
  java -XX:NativeMemoryTracking=summary -cp "$WORK" Idle $mb > /dev/null &
  PID=$!
  sleep 4
  jcmd $PID VM.native_memory summary scale=KB > nmt-hold-$mb.txt
  kill $PID; wait $PID 2>/dev/null || true
done

# 4. RSSの内訳(NMTなしで起動した場合)
for mb in 0 256; do
  java -cp "$WORK" Idle $mb > /dev/null &
  PID=$!
  sleep 4
  grep -E '^(VmRSS|RssAnon|RssFile|RssShmem):' /proc/$PID/status > rss-parts-$mb.txt
  if [ "$mb" = 0 ]; then
    # smaps のRssを、読み込んでいるファイルごとに合計する
    awk '/^[0-9a-f]+-[0-9a-f]+ /{name=($6==""?"[anon]":$6); n=split(name,p,"/"); name=p[n]} /^Rss:/{sum[name]+=$2} END{for(k in sum) printf "%8d kB  %s\n", sum[k], k}' /proc/$PID/smaps | sort -rn | head -8 > rss-file-breakdown.txt
  fi
  kill $PID; wait $PID 2>/dev/null || true
done

# 5. ヒープを広げたときのGCのログ
java -Xlog:gc+ergo+heap=debug -cp "$WORK" HeapVsRss | grep -E "Heap expansion|Expand the heap" | head -2 > heap-expansion.txt

# 6. ヒープを縮めたあと、メモリをOSに返すログ
java -Xlog:gc*=debug -cp "$WORK" HeapVsRss | grep -E "^(起動直後|256MB保持|GC直後|1秒後)|Uncommit|Uncommittable" > uncommit-log.txt

rm -rf "$WORK"
