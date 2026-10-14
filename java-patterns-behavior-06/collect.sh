#!/bin/bash
# 第6回の出力を集め直す。JAVA_HOME か PATH に JDK 25 を置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
for v in longm chain; do
  for p in before after; do
    mkdir -p "$WORK/$v-$p"
    javac -d "$WORK/$v-$p" $v/$p/exam/*.java
  done
done
: > output-same.txt
for p in before after; do
  if diff -q <(java -cp "$WORK/longm-$p" exam.App) <(java -cp "$WORK/chain-$p" exam.App) > /dev/null; then r="同じ"; else r="違う"; fi
  echo "$p: 検証を連ねた版は、長いメソッド版と出力が$r" >> output-same.txt
done
java -cp "$WORK/chain-after" exam.App > output-run.txt
python3 diffstat.py > output-diffstat.txt
java order/Order.java > output-order.txt
java errors/Errors.java > output-errors.txt
java discount/Discount.java > output-discount.txt
rm -rf "$WORK"
