#!/bin/bash
# 第5回の出力を集め直す。JAVA_HOME か PATH に JDK 25 を置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
: > output-table-after.txt
for v in flag stateclass enumtype sealedv; do
  for p in before after; do
    mkdir -p "$WORK/$v-$p"
    javac -d "$WORK/$v-$p" $v/$p/exam/*.java
  done
done
: > output-same.txt
for p in before after; do
  for v in stateclass enumtype sealedv; do
    if diff -q <(java -cp "$WORK/flag-$p" exam.Check) <(java -cp "$WORK/$v-$p" exam.Check) > /dev/null; then r="同じ"; else r="違う"; fi
    echo "$p: $v はフラグ版と出力が$r" >> output-same.txt
  done
done
java -cp "$WORK/flag-before" exam.Check > output-table-before.txt
java -cp "$WORK/flag-after" exam.Check > output-table-after.txt
python3 diffstat.py > output-diffstat.txt
python3 sizes.py > output-sizes.txt
: > output-missing.txt
for v in flag stateclass enumtype sealedv; do
  echo "== $v" >> output-missing.txt
  mkdir -p "$WORK/missing-$v"
  if javac -d "$WORK/missing-$v" missing/$v/exam/*.java >> output-missing.txt 2>&1; then
    echo "コンパイル: 成功" >> output-missing.txt
    java -cp "$WORK/missing-$v" exam.Cancel >> output-missing.txt 2>&1 || true
  fi
done
mkdir -p "$WORK/cancel-stateclass"
javac -d "$WORK/cancel-stateclass" missing/cancel-stateclass/exam/*.java > output-cancel-forgotten.txt 2>&1 && echo "コンパイル: 成功" >> output-cancel-forgotten.txt
java -cp "$WORK/cancel-stateclass" exam.CancelTaken >> output-cancel-forgotten.txt 2>&1 || true
sed -i '/^\tat /d; s#^Exception in thread "main" #実行時: #' output-cancel-forgotten.txt
sed -i 's#^Exception in thread "main" #実行時: #; /^\tat /d' output-missing.txt
# 状態ごとに型を分けた書き方：許されない操作は、コンパイルできない
mkdir -p "$WORK/typed"
javac -d "$WORK/typed" typed/exam/Typed.java typed/exam/Good.java
java -cp "$WORK/typed" exam.Good > output-typed-good.txt
javac -d "$WORK/typed-bad" typed/exam/Typed.java typed/exam/Bad.java > output-typed-error.txt 2>&1 || true
rm -rf "$WORK"
