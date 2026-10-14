#!/bin/bash
# 第1回の出力を集め直す。JAVA_HOME か PATH に JDK 25 を置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
: > output-run.txt
for v in branch iface lambda enumtype; do
  for p in before after; do
    mkdir -p "$WORK/$v-$p"
    javac -d "$WORK/$v-$p" $v/$p/exam/*.java
    echo "== $v/$p" >> output-run.txt
    java -cp "$WORK/$v-$p" exam.App >> output-run.txt
  done
done
# 4つの形で、出力が同じかどうか（branch を基準にする）
: > output-same.txt
for p in before after; do
  for v in iface lambda enumtype; do
    if diff -q <(java -cp "$WORK/branch-$p" exam.App) <(java -cp "$WORK/$v-$p" exam.App) > /dev/null; then r="同じ"; else r="違う"; fi
    echo "$p: $v は branch と出力が$r" >> output-same.txt
  done
done
python3 diffstat.py > output-diffstat.txt
python3 filesize.py > output-filesize.txt
mkdir -p "$WORK/pitfall"
javac -d "$WORK/pitfall" pitfall/Quota.java
java -cp "$WORK/pitfall" Quota > output-pitfall.txt
java switching/Switching.java > output-switching.txt
java bench/Bench.java > output-bench.txt 2>/dev/null
rm -rf "$WORK"
