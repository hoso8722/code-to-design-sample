#!/usr/bin/env python3
"""同じ仕様を3つの書き方（plain / visitor / sealedv）で書き、2つの変更の依頼（op：処理を足す、type：種類を足す）を適用したソースを作る。
使い方: python3 gen.py（このフォルダで実行する）"""
import os, shutil, textwrap

def put(root, files):
    for path, text in files.items():
        full = os.path.join(root, 'exam', path)
        os.makedirs(os.path.dirname(full), exist_ok=True)
        open(full, 'w').write(textwrap.dedent(text).lstrip('\n'))

# 科目の種類：必須（満点の100%）、選択（50%）、実技（80%）。口述（60%）は、あとで足す種類
TYPES = [('Required', '必須', '100', 'points'), ('Elective', '選択', '50', 'points / 2'), ('Practical', '実技', '80', 'points * 8 / 10')]
ORAL = ('Oral', '口述', '60', 'points * 6 / 10')

SAMPLES = '''
    // 検証しない: 動作確認用の入力
    package exam;

    import java.util.List;

    public class Samples {
%LIST%
    }
'''
def samples(with_oral):
    items = ['new Required("数学", 80)', 'new Elective("音楽", 90)', 'new Practical("実験", 70)', 'new Required("英語", 105)']
    if with_oral:
        items.append('new Oral("面接", 60)')
    body = ',\n            '.join(items)
    return SAMPLES.replace('%LIST%', f'        public static final List<Subject> ALL = List.of(\n            {body});')

def app(with_max, name_ops):
    lines = ''.join(f'            System.out.println(s.getClass().getSimpleName() + ": " + {e});\n' for e in name_ops)
    return f'''
    // 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
    package exam;

    public class App {{
        public static void main(String[] args) {{
            for (Subject s : Samples.ALL) {{
{lines}            }}
        }}
    }}
'''

# ---------- plain：処理は、各クラスのメソッド ----------
def plain(op, typ):
    types = TYPES + ([ORAL] if typ else [])
    files = {}
    maxabs = '    abstract int maxPoints();\n' if op else ''
    files['Subject.java'] = f'''
    package exam;

    // 科目。処理は、各クラスのメソッドとして書く
    public abstract class Subject {{
        protected final String name;
        protected final int points;

        protected Subject(String name, int points) {{
            this.name = name;
            this.points = points;
        }}

        abstract int score();
        abstract String describe();
        abstract boolean valid();
{maxabs}    }}
'''
    for cls, label, pct, expr in types:
        maxm = f'    @Override int maxPoints() {{ return 100 * {pct} / 100; }}\n' if op else ''
        files[f'{cls}.java'] = f'''
    package exam;

    public class {cls} extends Subject {{
        public {cls}(String name, int points) {{ super(name, points); }}

        @Override int score() {{ return {expr}; }}
        @Override String describe() {{ return "{label}科目「" + name + "」" + points + "点（配点{pct}%）"; }}
        @Override boolean valid() {{ return points >= 0 && points <= 100; }}
{maxm}    }}
'''
    ops = ['s.score()', 's.describe()', 's.valid()'] + (['s.maxPoints()'] if op else [])
    files['App.java'] = app(op, ops)
    files['Samples.java'] = samples(typ)
    return files

# ---------- visitor：処理は、Visitor ----------
def visitor(op, typ):
    types = TYPES + ([ORAL] if typ else [])
    files = {}
    methods = ''.join(f'        R visit{cls}({cls} subject);\n' for cls, *_ in types)
    files['Subject.java'] = '''
    package exam;

    // 科目。処理は、Visitor に任せる
    public interface Subject {
        String name();
        int points();

        <R> R accept(SubjectVisitor<R> visitor);
    }
'''
    files['SubjectVisitor.java'] = f'''
    package exam;

    public interface SubjectVisitor<R> {{
{methods}    }}
'''
    for cls, label, pct, expr in types:
        files[f'{cls}.java'] = f'''
    package exam;

    public record {cls}(String name, int points) implements Subject {{
        @Override
        public <R> R accept(SubjectVisitor<R> visitor) {{ return visitor.visit{cls}(this); }}
    }}
'''
    def visitor_class(vname, rtype, body):
        cases = ''.join(f'''        @Override public {rtype} visit{cls}({cls} s) {{ return {body(cls, label, pct, expr)}; }}
''' for cls, label, pct, expr in types)
        return f'''
    package exam;

    public class {vname} implements SubjectVisitor<{rtype}> {{
{cases}    }}
'''
    files['ScoreVisitor.java'] = visitor_class('ScoreVisitor', 'Integer', lambda c, l, p, e: e.replace('points', 's.points()'))
    files['DescribeVisitor.java'] = visitor_class('DescribeVisitor', 'String', lambda c, l, p, e: f'"{l}科目「" + s.name() + "」" + s.points() + "点（配点{p}%）"')
    files['ValidVisitor.java'] = visitor_class('ValidVisitor', 'Boolean', lambda c, l, p, e: 's.points() >= 0 && s.points() <= 100')
    if op:
        files['MaxVisitor.java'] = visitor_class('MaxVisitor', 'Integer', lambda c, l, p, e: f'100 * {p} / 100')
    ops = ['s.accept(new ScoreVisitor())', 's.accept(new DescribeVisitor())', 's.accept(new ValidVisitor())'] + (['s.accept(new MaxVisitor())'] if op else [])
    files['App.java'] = app(op, ops)
    files['Samples.java'] = samples(typ)
    return files

# ---------- sealed + switch ----------
def sealedv(op, typ):
    types = TYPES + ([ORAL] if typ else [])
    files = {}
    permits = ', '.join(c for c, *_ in types)
    files['Subject.java'] = f'''
    package exam;

    // 科目。種類は sealed で閉じ、処理は switch で書く
    public sealed interface Subject permits {permits} {{
        String name();
        int points();
    }}
'''
    for cls, *_ in types:
        files[f'{cls}.java'] = f'''
    package exam;

    public record {cls}(String name, int points) implements Subject {{}}
'''
    def sw(fn, rtype, body):
        cases = ''.join(f'            case {cls} s -> {body(cls, label, pct, expr)};\n' for cls, label, pct, expr in types)
        return f'''
    static {rtype} {fn}(Subject subject) {{
        return switch (subject) {{
{cases}        }};
    }}
'''
    methods = (sw('score', 'int', lambda c, l, p, e: e.replace('points', 's.points()'))
               + sw('describe', 'String', lambda c, l, p, e: f'"{l}科目「" + s.name() + "」" + s.points() + "点（配点{p}%）"')
               + sw('valid', 'boolean', lambda c, l, p, e: 's.points() >= 0 && s.points() <= 100')
               + (sw('maxPoints', 'int', lambda c, l, p, e: f'100 * {p} / 100') if op else ''))
    files['Subjects.java'] = f'''
    package exam;

    // 科目に対する処理。switch の網羅性は、コンパイラが確かめる
    public class Subjects {{
{textwrap.indent(textwrap.dedent(methods), '    ')}    }}
'''
    ops = ['Subjects.score(s)', 'Subjects.describe(s)', 'Subjects.valid(s)'] + (['Subjects.maxPoints(s)'] if op else [])
    files['App.java'] = app(op, ops)
    files['Samples.java'] = samples(typ)
    return files

VERSIONS = {'plain': plain, 'visitor': visitor, 'sealedv': sealedv}
REQUESTS = {'before': (False, False), 'op': (True, False), 'type': (False, True)}
for v, fn in VERSIONS.items():
    for r, (op, typ) in REQUESTS.items():
        shutil.rmtree(f'{v}/{r}', ignore_errors=True)
        put(f'{v}/{r}', fn(op, typ))

# ブレースの深さだけで、インデントを整える簡易な整形
import glob
for path in glob.glob('*/*/exam/*.java'):
    depth, out = 0, []
    for raw in open(path, encoding='utf-8').read().split('\n'):
        line = raw.strip()
        if not line:
            out.append('')
            continue
        opens = line.count('{') - line.count('}')
        lead = depth - (1 if line.startswith('}') else 0)
        out.append('    ' * max(lead, 0) + line)
        depth += opens
    open(path, 'w', encoding='utf-8').write('\n'.join(out))
