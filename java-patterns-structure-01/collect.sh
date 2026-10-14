#!/bin/bash
# 第1回の出力を集め直す。JAVA_HOME と PATH に、JDK 25 と Maven を置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
mvn -q test-compile
java -cp target/classes exam.Demo > output-demo.txt
java -cp target/classes:target/test-classes exam.Trial > output-trial.txt
mvn test 2>&1 | grep -E "Tests run:|BUILD" > output-mvn-test.txt
