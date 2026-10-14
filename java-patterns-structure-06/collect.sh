#!/bin/bash
# 第6回の出力を集め直す。JAVA_HOME か PATH に JDK 25 を置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
run() {  # run <名前> <ソースのフォルダ> <メインクラス>
  mkdir -p "$WORK/$1"
  javac -d "$WORK/$1" $2/exam/*.java
  java -cp "$WORK/$1" "$3"
}
{
  for v in direct adapter; do
    echo "== $v/before（旧形式）"; run $v-b $v/before exam.App
    echo "== $v/after（新形式）"; run $v-a $v/after exam.App
  done
} > output-run.txt
python3 diffstat.py > output-diffstat.txt
{ run bad-d direct/before exam.BadDirect; run bad-a translate exam.Bad; } > output-bad.txt
{ run forgot direct/before exam.BadForgotten; } > output-forgotten.txt
run forms forms exam.Forms > output-forms.txt
java bench/exam/Bench.java > output-bench.txt
rm -rf "$WORK"
