#!/bin/bash
# 第9回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# スレッドダンプを取る: プログラムを動かし、pid が出力されたら、jcmd で調べる
dump() {  # dump <クラス名> <出力ファイル> <ダンプのファイル>
  java -cp "$WORK" "$1" > "$2" 2>&1 &
  local pid=$!
  until grep -q "pid=" "$2"; do sleep 0.5; done
  sleep 1
  jcmd "$pid" Thread.print > "$3"
  kill "$pid" 2>/dev/null || true; wait "$pid" 2>/dev/null || true
}
dump Deadlock deadlock-output.txt deadlock-thread-dump.txt
dump Starvation starvation-output.txt starvation-thread-dump.txt

# commonPool が詰まる(ブロックする処理の数を変える。各3回)
java -cp "$WORK" CommonPoolJam 30 > common-pool-jam-30.txt
echo "blocking,run,fast_latency_ms" > common-pool-jam.csv
for b in 0 5 10 14 15 16 20 30 45; do
  for i in 1 2 3; do
    echo "$b,$i,$(java -cp "$WORK" CommonPoolJam $b | sed -n 's/^fast を投入してから結果が出るまで: \([0-9]*\) ms/\1/p')" >> common-pool-jam.csv
  done
done

# tryLock
java -cp "$WORK" TryLock > trylock.txt

# 仮想スレッド: Thread.print と Thread.dump_to_file
java -cp "$WORK" VtDump > "$WORK/vt.out" &
pid=$!
until grep -q "pid=" "$WORK/vt.out"; do sleep 0.5; done
sleep 1
{
  echo "# Thread.print に vt- を含む行: $(jcmd "$pid" Thread.print | grep -c 'vt-')"
  jcmd "$pid" Thread.dump_to_file "$WORK/threads.txt" > /dev/null
  cat "$WORK/threads.txt"
} > virtual-thread-dump.txt
kill "$pid" 2>/dev/null || true; wait "$pid" 2>/dev/null || true
rm -rf "$WORK"
