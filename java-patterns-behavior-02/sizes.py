#!/usr/bin/env python3
"""3つの書き方の大きさ（空行を除いた行数）"""
import glob

def lines(path):
    return sum(1 for l in open(path, encoding="utf-8") if l.strip() and not l.strip().startswith("//"))

print("書き方,ファイル数,行数の合計,流れ（読み込み→判定→出力）が書かれた場所の数")
for v, label in [("dup", "複製（3つのクラス）"), ("template", "Template Method"), ("function", "関数を渡す")]:
    files = [f for f in glob.glob(f"{v}/before/exam/*.java") if not f.endswith(("App.java", "Samples.java", "Student.java"))]
    count = sum(1 for f in files if 'split(",")' in open(f, encoding="utf-8").read())
    print(f"{label},{len(files)},{sum(lines(f) for f in files)},{count}")
