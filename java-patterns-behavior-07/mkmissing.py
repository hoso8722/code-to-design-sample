#!/usr/bin/env python3
"""種類や処理を足したが、使う側を直していない状態のソースを作る（gen.py のあとに実行する）"""
import os, shutil

def prep(name, src, edits, adds):
    dst = f'missing/{name}/exam'
    shutil.rmtree(f'missing/{name}', ignore_errors=True)
    shutil.copytree(f'{src}/before/exam', dst)
    for f, (a, b) in edits.items():
        p = f'{dst}/{f}'
        s = open(p, encoding='utf-8').read()
        assert a in s, (name, f, a)
        open(p, 'w', encoding='utf-8').write(s.replace(a, b))
    for f, t in adds.items():
        open(f'{dst}/{f}', 'w', encoding='utf-8').write(t)
    for f in ('App.java', 'Samples.java'):
        if os.path.exists(f'{dst}/{f}'):
            os.remove(f'{dst}/{f}')

prep('type-plain', 'plain', {}, {'Oral.java': 'package exam;\n\npublic class Oral extends Subject {\n    public Oral(String name, int points) { super(name, points); }\n}\n'})
prep('type-visitor', 'visitor', {'SubjectVisitor.java': ('}\n', '    R visitOral(Oral subject);\n}\n')},
     {'Oral.java': 'package exam;\n\npublic record Oral(String name, int points) implements Subject {\n    @Override\n    public <R> R accept(SubjectVisitor<R> visitor) { return visitor.visitOral(this); }\n}\n'})
prep('type-sealedv', 'sealedv', {'Subject.java': ('permits Required, Elective, Practical', 'permits Required, Elective, Practical, Oral')},
     {'Oral.java': 'package exam;\n\npublic record Oral(String name, int points) implements Subject {}\n'})
prep('op-plain', 'plain', {'Subject.java': ('abstract boolean valid();\n', 'abstract boolean valid();\n    abstract int maxPoints();\n')}, {})
