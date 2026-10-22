#!/bin/bash
# 第4回の出力を集め直す。JAVA_HOME か PATH に JDK 21 を置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" exam/Legacy*.java exam/Application.java exam/Scores.java exam/TypedApplication.java exam/ApplicationBuilder.java exam/Mixup.java exam/Validation.java exam/Bench.java
java -cp "$WORK" exam.Mixup > output-mixup.txt
java -cp "$WORK" exam.Validation > output-validation.txt
java -cp "$WORK" exam.Bench > output-bench.txt
javac -d "$WORK/bad" -cp "$WORK" exam/BadSwap.java > output-compile-error.txt 2>&1 || true
python3 callsite.py > output-callsite.txt
rm -rf "$WORK"
