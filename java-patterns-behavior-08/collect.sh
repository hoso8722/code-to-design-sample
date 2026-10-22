#!/bin/bash
# 第8回の出力を集め直す。JAVA_HOME か PATH に JDK 21 を置いて、このフォルダの中で実行する。先に第1〜7回の collect.sh を実行しておく
set -e
cd "$(dirname "$0")"
java same/Same.java > output-same.txt
python3 summary.py > output-summary.txt
