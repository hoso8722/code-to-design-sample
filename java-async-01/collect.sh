#!/bin/bash
# 第1回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# 1. 順番と同時の比較
java -cp "$WORK" WaitTasks > wait-tasks.txt 2>&1
# 順番と同時の時間(あと2回。範囲を書くため)
for i in 2 3; do java -cp "$WORK" WaitTasks 2>&1 | grep "実行:"; done > wait-tasks-extra.txt

# 2. スレッド数を変えたときの時間(各3回)
echo "kind,threads,elapsed_ms" > scale.csv
for kind in wait cpu; do
  for n in 1 2 4 8 16 32; do
    for i in 1 2 3; do java -cp "$WORK" ScaleTasks $kind $n >> scale.csv; done
  done
done

# 3. 並行と並列(CPUを1つに制限した場合と、すべてのCPUを使う場合)
java -cp "$WORK" CpuTimeline > cpu-timeline-all.txt
taskset -c 0 java -cp "$WORK" CpuTimeline > cpu-timeline-1cpu.txt
rm -rf "$WORK"
