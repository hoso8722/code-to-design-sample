#!/bin/bash
# 第6回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -g -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# 1. Metaspace
java -cp "$WORK" ManyClasses 20000 > many-classes.csv
java -XX:MaxMetaspaceSize=16m -cp "$WORK" ManyClasses 20000 2>&1 | grep -v '^\s*at ' > many-classes-max16m.txt || true

# 2. Direct Buffer
java -cp "$WORK" DirectBuffers 512 > direct-buffers.csv
java -Xmx64m -cp "$WORK" DirectBuffers 512 2>&1 | grep -v '^\s*at ' > direct-buffers-xmx64m.txt || true
java -XX:MaxDirectMemorySize=128m -cp "$WORK" DirectBuffers 512 2>&1 | grep -v '^\s*at ' > direct-buffers-max128m.txt || true

# 3. NMTの差分(baselineは起動の10秒以内、summary.diffは "allocated" のあと)
java -XX:NativeMemoryTracking=summary -cp "$WORK" OffHeap > "$WORK/offheap.out" &
PID=$!
until grep -q '^pid=' "$WORK/offheap.out"; do sleep 0.2; done
jcmd "$PID" VM.native_memory baseline > /dev/null
BASE_RSS=$(grep VmRSS /proc/$PID/status)
until grep -q '^allocated' "$WORK/offheap.out"; do sleep 0.5; done
jcmd "$PID" VM.native_memory summary.diff scale=KB > nmt-diff.txt
grep VmRSS /proc/$PID/status >> nmt-diff.txt
echo "baseline $BASE_RSS" >> nmt-diff.txt
kill "$PID" 2>/dev/null || true
rm -rf "$WORK"
