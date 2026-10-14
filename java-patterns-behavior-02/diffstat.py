#!/usr/bin/env python3
"""before と after を比べて、変更・追加されたファイルの数と、追加・削除された行の数を数える（動作確認用の App.java は除く）"""
import difflib, os, sys

def files(root):
    out = {}
    for d, _, fs in os.walk(root):
        for f in fs:
            if f.endswith(".java") and f != "App.java":
                p = os.path.join(d, f)
                out[os.path.relpath(p, root)] = open(p, encoding="utf-8").read().split("\n")
    return out

print("方式,変更したファイル,追加したファイル,追加した行,削除した行")
for variant, label in [("dup", "複製（3つのクラス）"), ("template", "Template Method"), ("function", "関数を渡す")]:
    before, after = files(f"{variant}/before"), files(f"{variant}/after")
    changed = added = plus = minus = 0
    for name in sorted(after):
        if name not in before:
            added += 1
            plus += len(after[name]) - 1
            continue
        diff = list(difflib.unified_diff(before[name], after[name], lineterm="", n=0))
        p = sum(1 for l in diff if l.startswith("+") and not l.startswith("+++"))
        m = sum(1 for l in diff if l.startswith("-") and not l.startswith("---"))
        if p or m:
            changed += 1
            plus += p
            minus += m
    print(f"{label},{changed},{added},{plus},{minus}")
