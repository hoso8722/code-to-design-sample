#!/bin/bash
# 第3回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# 1. プールの大きさを変えたときの時間(各3回)
echo "kind,pool_size,elapsed_ms" > pool-size.csv
for kind in wait cpu; do
  for n in 1 2 4 8 16 32 64; do
    for i in 1 2 3; do java -cp "$WORK" PoolSize $kind $n >> pool-size.csv; done
  done
done

# 2. キューのタイムライン
java -cp "$WORK" QueueTimeline > queue-timeline.csv

# 3. cached と fixed(fixed は約25秒かかる)
{ java -cp "$WORK" CachedVsFixed cached; java -cp "$WORK" CachedVsFixed fixed; } > cached-vs-fixed.txt

# 4. shutdown しない場合(5秒で打ち切る)
: > no-shutdown.txt
for mode in noclose close; do
  echo "== $mode" >> no-shutdown.txt
  s=$(date +%s%N)
  set +e; timeout 5 java -cp "$WORK" NoShutdown $mode >> no-shutdown.txt; rc=$?; set -e
  echo "終了コード: $rc  経過: $(( ($(date +%s%N) - s) / 1000000 )) ms" >> no-shutdown.txt
done
rm -rf "$WORK"
