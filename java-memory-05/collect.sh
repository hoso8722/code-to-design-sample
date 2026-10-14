#!/bin/bash
# 第5回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -g -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# 1. キャッシュ: 上限なし / 上限1万件 / ヒープ128MBでOOM
java -cp "$WORK" CacheLeak unbounded > cache-unbounded.csv
java -cp "$WORK" CacheLeak bounded > cache-bounded.csv
java -Xmx128m -cp "$WORK" CacheLeak unbounded 2>&1 | tail -4 > cache-unbounded-128m.txt || true

# 2. クラスヒストグラム
for mode in unbounded bounded; do
  java -Xmx256m -cp "$WORK" CacheLeak $mode wait > "$WORK/$mode.out" &
  PID=$!
  until grep -q '^pid=' "$WORK/$mode.out"; do sleep 0.5; done
  jcmd "$PID" GC.class_histogram | head -8 > histogram-$mode.txt
  kill "$PID" 2>/dev/null || true
  wait "$PID" 2>/dev/null || true
done

# 3. ThreadLocal(各2回)
: > threadlocal.txt
for mode in noremove remove; do
  echo "== $mode" >> threadlocal.txt
  for i in 1 2; do java -cp "$WORK" ThreadLocalLeak $mode >> threadlocal.txt; done
done

# 4. WeakHashMap(各3回)
: > weak.txt
for mode in hash weak weak-value-key; do
  for i in 1 2 3; do java -cp "$WORK" WeakDemo $mode >> weak.txt; done
done

# 5. WeakReference
java -cp "$WORK" WeakRef > weakref.txt
rm -rf "$WORK"
