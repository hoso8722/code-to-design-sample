#!/usr/bin/env python3
"""4つの書き方の、変更前（4状態）の大きさ（コメントと空行を除く。動作確認用の Check.java は除く）"""
import glob

def lines(path):
    return sum(1 for l in open(path, encoding="utf-8") if l.strip() and not l.strip().startswith("//"))

print("書き方,ファイル数,行数の合計")
for v, label in [("flag", "フラグと条件分岐"), ("stateclass", "状態ごとのクラス"), ("enumtype", "enum"), ("sealedv", "sealed + switch")]:
    files = [f for f in glob.glob(f"{v}/before/exam/*.java") if not f.endswith("Check.java")]
    print(f"{label},{len(files)},{sum(lines(f) for f in files)}")
