#!/bin/bash
# 第3回の出力を集め直す。JAVA_HOME か PATH に JDK 21 を置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
for v in direct listener; do
  for p in before after; do
    mkdir -p "$WORK/$v-$p"
    javac -d "$WORK/$v-$p" $v/$p/exam/*.java
  done
done
: > output-same.txt
for p in before after; do
  if diff -q <(java -cp "$WORK/direct-$p" exam.App) <(java -cp "$WORK/listener-$p" exam.App) > /dev/null; then r="同じ"; else r="違う"; fi
  echo "$p: リスナー版は直接呼ぶ版と出力が$r" >> output-same.txt
done
java -cp "$WORK/direct-before" exam.App > output-before.txt
java -cp "$WORK/listener-after" exam.App > output-run.txt
python3 diffstat.py > output-diffstat.txt
: > output-leak.txt
for mode in strong unregister weak; do java -Xmx512m leak/Leak.java $mode >> output-leak.txt; done
java errors/Errors.java > output-errors.txt
java slow/FlowErrors.java > output-flow-errors.txt
: > output-slow.txt
for i in 1 2 3; do
  echo "== ${i}回目の実行" >> output-slow.txt
  for mode in sync submit16 submit256 offer16; do java slow/Slow.java $mode >> output-slow.txt; done
done
javac -Xlint:deprecation -d "$WORK/old" old/OldObservable.java > output-old.txt 2>&1 || true
rm -rf "$WORK"
