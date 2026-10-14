#!/usr/bin/env python3
"""3つの設計の大きさ（ファイル数、行数）、条件を読むまでにたどるファイルの数、変更の依頼ごとの変更箇所を数える（Samples.java は数えない）"""
import difflib, os, re
from collections import deque

VERSIONS = [("plain", "パターンなし"), ("enum", "enum + ラムダ"), ("patterns", "パターンをすべて使う")]
REQUESTS = [("r1", "一般入試の合格点を180から170に変える"), ("r2", "入試区分「推薦」を足す"),
            ("r3", "ログに3科目の点数を足す"), ("r4", "CSVが新形式（列の順序が変わる）になる")]

def read(root):
    out = {}
    for f in os.listdir(os.path.join(root, "exam")):
        if f.endswith(".java") and f != "Samples.java":
            out[f] = open(os.path.join(root, "exam", f), encoding="utf-8").read()
    return out

def code_lines(text):
    return [l for l in text.split("\n") if l.strip() and not l.strip().startswith("//")]

def hops(files):
    """App.java から、奨学生入試の条件（s.total() / 3 >= 70）が書かれたファイルまで、型名の参照をたどった最短のファイル数"""
    names = {f[:-5]: f for f in files}
    graph = {f: [names[n] for n in names if names[n] != f and re.search(r"\b%s\b" % n, files[f])] for f in files}
    target = [f for f, t in files.items() if "total() / 3 >= 70" in t][0]
    queue, seen = deque([("App.java", 1)]), {"App.java"}
    while queue:
        cur, d = queue.popleft()
        if cur == target:
            return d
        for nxt in graph[cur]:
            if nxt not in seen:
                seen.add(nxt)
                queue.append((nxt, d + 1))

print("設計,ファイル数,コードの行数,型の数,奨学生入試の条件を読むまでに開くファイル数")
for v, label in VERSIONS:
    files = read(f"{v}/before")
    lines = sum(len(code_lines(t)) for t in files.values())
    types = sum(len(re.findall(r"^\s*(?:public |final )*(?:class|record|interface|enum) ", t, re.M)) for t in files.values())
    print(f"{label},{len(files)},{lines},{types},{hops(files)}")

print()
print("依頼,設計,変更したファイル,追加したファイル,追加した行,削除した行")
for r, rlabel in REQUESTS:
    for v, label in VERSIONS:
        before, after = read(f"{v}/before"), read(f"{v}/{r}")
        changed = added = plus = minus = 0
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
                plus += p
                minus += m
        print(f"{rlabel},{label},{changed},{added},{plus},{minus}")
