#!/bin/bash
# 第8回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# 1. スレッドプールと仮想スレッド(各3回)
echo "executor,work,tasks,elapsed_ms" > many-waits.csv
for spec in "fixed100 wait" "fixed1000 wait" "virtual wait" "fixed16 cpu" "virtual cpu"; do
  set -- $spec
  for i in 1 2 3; do
    java -cp "$WORK" ManyWaits $1 $2 | awk -F, '{print $1","$2","$3","$4}' >> many-waits.csv
  done
done

# 2. synchronized の中で待つ(3回。1回目を pinning-jdk-21.csv に)
java -cp "$WORK" Pinning > pinning-jdk-21.csv
for i in 2 3; do java -cp "$WORK" Pinning | sed -n 2p; done > pinning-runs.txt

# 3. JFR でピン留めを検出する
java -XX:StartFlightRecording=filename="$WORK/pin.jfr",settings=default -cp "$WORK" Pinning > /dev/null 2>&1
{
  echo "== jdk-21: jdk.VirtualThreadPinned の件数 = $(jfr print --events jdk.VirtualThreadPinned "$WORK/pin.jfr" | grep -c 'jdk.VirtualThreadPinned {')"
  jfr print --events jdk.VirtualThreadPinned "$WORK/pin.jfr" | awk '/jdk.VirtualThreadPinned \{/{n++} n==1'
} > jfr-pinned.txt

# 4. -Djdk.tracePinnedThreads(JDK 21にある、ピン留めのスタックトレースの出力)
java -Djdk.tracePinnedThreads=short -cp "$WORK" Pinning 2>&1 | grep -A3 "reason:MONITOR" | head -8 > trace-pinned.txt
rm -rf "$WORK"
