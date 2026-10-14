#!/bin/bash
# 第7回の出力を集め直す。JAVA_HOME か PATH に JDK 25 を置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
python3 gen.py
python3 mkmissing.py
WORK=$(mktemp -d)
: > output-same.txt
for r in before op type; do
  for v in plain visitor sealedv; do
    mkdir -p "$WORK/$v-$r"
    javac -d "$WORK/$v-$r" $v/$r/exam/*.java
  done
  for v in visitor sealedv; do
    if diff -q <(java -cp "$WORK/plain-$r" exam.App) <(java -cp "$WORK/$v-$r" exam.App) > /dev/null; then res="同じ"; else res="違う"; fi
    echo "$r: $v は plain と出力が$res" >> output-same.txt
  done
done
java -cp "$WORK/sealedv-type" exam.App > output-run.txt
python3 metrics.py > output-metrics.txt
: > output-missing.txt
for m in type-plain type-visitor type-sealedv op-plain; do
  echo "== $m" >> output-missing.txt
  javac -d "$WORK/miss-$m" missing/$m/exam/*.java >> output-missing.txt 2>&1 || true
done
rm -rf "$WORK"
