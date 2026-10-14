#!/usr/bin/env python3
"""奨学生入試の判定を直すときに、開いて読むことになるファイルの行数（空行を除く）"""
import os

def lines(path):
    return sum(1 for l in open(path, encoding="utf-8") if l.strip())

print("方式,奨学生入試の条件が書かれたファイル,そのファイルの行数")
for name, label, file in [("branch", "条件分岐", "ExamService.java"), ("iface", "interface（Strategy）", "ScholarshipRule.java"),
                          ("lambda", "関数（ラムダ）", "ExamService.java"), ("enumtype", "enum", "ExamType.java")]:
    print(f"{label},{file},{lines(f'{name}/before/exam/{file}')}")
