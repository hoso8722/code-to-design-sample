#!/bin/bash
# 第3回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
# JOL は環境変数 JOL_JAR に jol-cli-0.17-full.jar のパスを指定する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -g -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# 1. new で確保したバイト数
java -cp "$WORK" AllocSize > alloc-size.txt
java -XX:-UseCompressedOops -cp "$WORK" AllocSize > alloc-size-no-compressed-oops.txt

# 2. 文字列の大きさ
java -cp "$WORK" StringSize > string-size.csv
java -XX:-CompactStrings -cp "$WORK" StringSize > string-size-nocompact.csv

# 3. コレクションが保持するバイト数
java -cp "$WORK" Retained > retained.txt
java -XX:-UseCompressedOops -cp "$WORK" Retained > retained-no-compressed-oops.txt

# 4. クラスヒストグラム
java -cp "$WORK" Histogram > "$WORK/histogram.out" &
PID=$!
until grep -q '^pid=' "$WORK/histogram.out"; do sleep 0.5; done
jcmd "$PID" GC.class_histogram | head -12 > class-histogram.txt
kill "$PID" 2>/dev/null || true

# 5. JOL でオブジェクトのレイアウトを見る
: > jol-layouts.txt
for c in java.lang.Long java.lang.Integer java.lang.String java.util.ArrayList \
         'java.util.HashMap$Node' 'java.util.LinkedList$Node'; do
  java -jar "$JOL_JAR" internals "$c" 2>/dev/null >> jol-layouts.txt
done
java -Djol.magicFieldOffset=true -cp "$WORK:$JOL_JAR" org.openjdk.jol.Main internals 'AllocSize$Point' 2>/dev/null >> jol-layouts.txt
# JOLが先頭に付ける注釈(#で始まる行)と、インスタンス生成の案内を除く
grep -v -e '^#' -e '^Instantiated' -e '^Failed to find' -e '^$' jol-layouts.txt > "$WORK/jol.tmp" && mv "$WORK/jol.tmp" jol-layouts.txt
rm -rf "$WORK"
