#!/bin/bash
# 第3回の出力を集め直す。JAVA_HOME か PATH に JDK 25 を置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
run() {  # run <名前> <ソースのフォルダ...> -- <メインクラス>
  local out=$WORK/$1; shift
  mkdir -p "$out"
  local srcs=()
  while [ "$1" != "--" ]; do srcs+=("$1"/exam/*.java); shift; done; shift
  javac -d "$out" "${srcs[@]}"
  java -cp "$out" "$1"
}
{
  for v in scatter factory enumtype; do
    echo "== $v/before"; run $v-b $v/before -- exam.Main
    echo "== $v/after (推薦を追加)"; run $v-a $v/after -- exam.MainRecommendation
  done
} > output-run.txt
{
  echo "== Factory Method（呼び出し側がサブクラスを選ぶ）"; run fm factorymethod -- exam.Main
  echo "== Factory Method に推薦を足す（既存のファイルは変えない）"; run fm2 factorymethod factorymethod/add -- exam.MainRecommendation
  echo "== Abstract Factory"; run fam family -- exam.Main
} > output-variants.txt
python3 diffstat.py > output-diffstat.txt
# Factory を経由せず、具体的なクラスを直接作ろうとしたとき
mkdir -p "$WORK/client"
javac -d "$WORK/client" factory/before/exam/*.java client/app/BadClient.java > output-compile-error.txt 2>&1 || true
javac -d "$WORK/client" factory/before/exam/*.java client/app/GoodClient.java
java -cp "$WORK/client" app.GoodClient > output-good-client.txt
java bench/exam/Bench.java > output-bench.txt
# 分岐を各所に書く方式で、ExamService だけを直した場合（ReportPrinter と CsvImporter は変更前のまま）
mkdir -p "$WORK/forgot/exam"
cp scatter/after/exam/*.java "$WORK/forgot/exam/"
cp scatter/before/exam/ReportPrinter.java scatter/before/exam/CsvImporter.java "$WORK/forgot/exam/"
javac -d "$WORK/forgot/o" "$WORK"/forgot/exam/*.java
java -cp "$WORK/forgot/o" exam.MainRecommendation > output-forgot.txt 2>&1 || true
rm -rf "$WORK"
