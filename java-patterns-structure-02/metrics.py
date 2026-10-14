#!/usr/bin/env python3
"""手順0〜6のソースを数える。使い方: python3 metrics.py（このフォルダで実行）"""
import re

def strip_comments(text):
    return "\n".join(l for l in text.split("\n") if not l.strip().startswith("//"))

def methods(code):
    """メソッド名 -> 本体の行数、メソッド名 -> 呼んでいるメソッド名の集合"""
    lines = code.split("\n")
    result, i = {}, 0
    head = re.compile(r"^\s+(?:public |private )?static [\w<>\[\]]+ (\w+)\(.*\{\s*$")
    while i < len(lines):
        m = head.match(lines[i])
        if m and "=" not in lines[i].split("(")[0]:
            name, depth, j = m.group(1), 0, i
            while True:
                depth += lines[j].count("{") - lines[j].count("}")
                if depth == 0:
                    break
                j += 1
            result[name] = "\n".join(lines[i:j + 1])
            i = j
        i += 1
    return result

print("手順,行数,メソッド数,最長メソッド(行),60の数,100の数,PASSの数,たどるメソッドの最大の深さ")
for n in range(7):
    code = strip_comments(open(f"src/main/java/exam/Step{n}.java", encoding="utf-8").read())
    body = [l for l in code.split("\n") if l.strip()]
    ms = methods(code)
    longest = max(len(b.split("\n")) for b in ms.values())
    def depth(name):
        calls = [o for o in ms if o != name and re.search(r"\b%s\(" % o, ms[name])]
        return 1 + max((depth(o) for o in calls), default=0)
    reach = depth("judge") - 1
    print(",".join(str(x) for x in [n, len(body), len(ms), longest,
          len(re.findall(r"\b60\b", code)), len(re.findall(r"\b100\b", code)),
          len(re.findall(r'"PASS"', code)), reach]))
