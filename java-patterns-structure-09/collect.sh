#!/bin/bash
# 第9回の出力を集め直す。JAVA_HOME か PATH に JDK 25 を置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK/c" composite/classic/exam/*.java
javac -d "$WORK/m" composite/modern/exam/*.java
{ java -cp "$WORK/c" exam.Demo; java -cp "$WORK/m" exam.Demo; } > output-composite.txt
{ java -cp "$WORK/c" exam.Depth; java -cp "$WORK/m" exam.Depth; } > output-depth.txt
javac -d "$WORK/bad" composite/modern-add/exam/*.java > output-composite-error.txt 2>&1 || true
# 3つの設計 × 変更の依頼（r1〜r4）：すべてコンパイルして実行し、設計が違っても出力が同じことを確かめる
(cd compare && python3 gen.py)
: > output-compare-run.txt
for v in plain enum patterns; do
  for r in before r1 r2 r3 r4; do
    mkdir -p "$WORK/$v-$r"
    javac -d "$WORK/$v-$r" compare/$v/$r/exam/*.java
    echo "$v $r $(java -cp "$WORK/$v-$r" exam.App | md5sum | cut -c1-8)" >> output-compare-run.txt
  done
done
java -cp "$WORK/plain-r2" exam.App > output-compare-sample-r2.txt
(cd compare && python3 metrics.py) > output-compare-metrics.txt
rm -rf "$WORK"
