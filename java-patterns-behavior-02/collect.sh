#!/bin/bash
# 第2回の出力を集め直す。JAVA_HOME か PATH に JDK 25 を置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
: > output-run.txt
for v in dup template function; do
  for p in before after; do
    mkdir -p "$WORK/$v-$p"
    javac -d "$WORK/$v-$p" $v/$p/exam/*.java
  done
done
: > output-same.txt
for p in before after; do
  for v in template function; do
    if diff -q <(java -cp "$WORK/dup-$p" exam.App) <(java -cp "$WORK/$v-$p" exam.App) > /dev/null; then r="同じ"; else r="違う"; fi
    echo "$p: $v は複製版と出力が$r" >> output-same.txt
  done
done
java -cp "$WORK/template-before" exam.App > output-run.txt
python3 diffstat.py > output-diffstat.txt
python3 sizes.py > output-sizes.txt
for ver in v1 v2; do
  mkdir -p "$WORK/fragile-$ver"
  javac -d "$WORK/fragile-$ver" fragile/$ver/BaseProcess.java fragile/CountingProcess.java fragile/Main.java
  echo "親クラス $ver: $(java -cp "$WORK/fragile-$ver" exam.Main)" >> output-fragile.txt.tmp
done
mv output-fragile.txt.tmp output-fragile.txt
mkdir -p "$WORK/fin"
javac -d "$WORK/fin" template/before/exam/*.java finalerr/OverrideRun.java > output-final-error.txt 2>&1 || true
java jdk/JdkTemplates.java > output-jdk.txt
rm -rf "$WORK"
