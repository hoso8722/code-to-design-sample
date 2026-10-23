#!/bin/bash
# 第7回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

java -cp "$WORK" LostUpdate > lost-update.txt
java -cp "$WORK" VolatileLostUpdate > volatile-lost-update.txt
java -cp "$WORK" MapCount > map-count.txt 2>&1
# あと2回(3回の傾向を書くため)
for i in 2 3; do java -cp "$WORK" MapCount 2>&1; echo ---; done > map-count-extra.txt
# スレッド数を変えたときの時間(各3回)
echo "method,threads,elapsed_ms,result" > counters.csv
for n in 1 2 4 8 16; do
  for i in 1 2 3; do java -cp "$WORK" Counters $n >> counters.csv; done
done
# 可視性: volatile なしと、ありを各3回
: > visibility.txt
for mode in none volatile; do
  for i in 1 2 3; do java -cp "$WORK" Visibility $mode >> visibility.txt; done
done
rm -rf "$WORK"
