#!/usr/bin/env python3
"""3つの書き方の大きさと、2つの変更の依頼（処理を足す、種類を足す）での変更箇所を数える（Samples.java、App.java は数えない）"""
import difflib, glob, os

VERSIONS = [("plain", "各クラスのメソッド"), ("visitor", "Visitor"), ("sealedv", "sealed + switch")]
REQUESTS = [("op", "処理を1つ足す（maxPoints）"), ("type", "種類を1つ足す（口述 Oral）")]

def read(root):
    out = {}
    for f in os.listdir(os.path.join(root, "exam")):
        if f.endswith(".java") and f not in ("Samples.java", "App.java"):
            out[f] = open(os.path.join(root, "exam", f), encoding="utf-8").read()
    return out

def code_lines(text):
    return [l for l in text.split("\n") if l.strip() and not l.strip().startswith("//")]

print("書き方,ファイル数,行数の合計")
for v, label in VERSIONS:
    files = read(f"{v}/before")
    print(f"{label},{len(files)},{sum(len(code_lines(t)) for t in files.values())}")

print()
print("依頼,書き方,変更したファイル,追加したファイル,追加した行,削除した行,変更したファイルの名前")
for r, rlabel in REQUESTS:
    for v, label in VERSIONS:
        before, after = read(f"{v}/before"), read(f"{v}/{r}")
        changed = added = plus = minus = 0
        names = []
        for name in sorted(after):
            if name not in before:
                added += 1
                plus += len(code_lines(after[name]))
                continue
            diff = list(difflib.unified_diff(before[name].split("\n"), after[name].split("\n"), lineterm="", n=0))
            p = sum(1 for l in diff if l.startswith("+") and not l.startswith("+++"))
            m = sum(1 for l in diff if l.startswith("-") and not l.startswith("---"))
            if p or m:
                changed += 1
                names.append(name.replace(".java", ""))
                plus += p
                minus += m
        print(f"{rlabel},{label},{changed},{added},{plus},{minus},{' '.join(names)}")
