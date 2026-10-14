#!/bin/bash
# 第2回の出力を集め直す。JAVA_HOME と PATH に、JDK 25 と Maven を置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
mvn test 2>&1 | grep -E "Tests run:|BUILD" > output-mvn-test.txt
python3 metrics.py > output-metrics.txt
