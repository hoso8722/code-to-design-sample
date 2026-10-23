#!/bin/bash
# 第2回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# 1. 同じ処理を、run() と start() で呼ぶ
java -cp "$WORK" RunVsStart > run-vs-start.csv
# 2. スレッドを作るコスト
java -cp "$WORK" ThreadCost > thread-cost.txt
# あと2回(範囲を書くため)
for i in 2 3; do java -cp "$WORK" ThreadCost | head -1; done > thread-cost-extra.txt
# 3. スレッドの中の例外
java -cp "$WORK" ThreadException > thread-exception.txt 2>&1
# 4. デーモンスレッド
: > daemon.txt
for mode in normal daemon; do
  echo "== $mode" >> daemon.txt
  arg=""; [ $mode = daemon ] && arg=daemon
  s=$(date +%s%N); java -cp "$WORK" Daemon $arg >> daemon.txt; echo "プロセスの実行時間: $(( ($(date +%s%N) - s) / 1000000 )) ms" >> daemon.txt
done
rm -rf "$WORK"
