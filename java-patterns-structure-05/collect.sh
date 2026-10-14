#!/bin/bash
# 第5回の出力を集め直す。JAVA_HOME か PATH に JDK 25 を置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" exam/*.java
: > output-race.txt
for i in 1 2 3; do
  echo "== ${i}回目の実行" >> output-race.txt
  java -cp "$WORK" exam.Race >> output-race.txt
done
java -cp "$WORK" exam.RuntimeCheck > output-runtime.txt
# 3つのテストを、すべての順序で実行する（順序ごとに新しいJVM）
: > output-order.txt
for mode in singleton config; do
  for order in T1,T2,T3 T1,T3,T2 T2,T1,T3 T2,T3,T1 T3,T1,T2 T3,T2,T1; do
    echo "$mode $order -> $(java -cp "$WORK" exam.Harness $mode $order)" >> output-order.txt
  done
done
java -cp "$WORK" exam.Breaking > output-breaking.txt
rm -rf "$WORK"
