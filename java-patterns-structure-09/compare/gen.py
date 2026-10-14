#!/usr/bin/env python3
"""同じ仕様を3つの設計（plain / enum / patterns）で書き、4つの変更の依頼（r1〜r4）を適用したソースを作る。
使い方: python3 gen.py（このフォルダで実行する）"""
import os, shutil, textwrap

def put(root, files):
    for path, text in files.items():
        full = os.path.join(root, 'exam', path)
        os.makedirs(os.path.dirname(full), exist_ok=True)
        open(full, 'w').write(textwrap.dedent(text).lstrip('\n'))

STUDENT = '''
    package exam;

    public record Student(String name, int math, int english, int science) {
        public int total() { return math + english + science; }
    }
'''
# 入力データ（動作確認用。変更の数には数えない）
BASE_V1 = ["general,佐藤,60,70,80", "scholarship,鈴木,90,50,40", "transfer,高橋,70,70,50", "general,伊藤,60,55,55"]
BASE_V2 = ["general,101,80,60,70,佐藤", "scholarship,102,40,90,50,鈴木", "transfer,103,50,70,70,高橋", "general,104,55,60,55,伊藤"]

def samples(lines):
    body = ',\n            '.join('"%s"' % l for l in lines)
    return f'''
    // 検証しない: 動作確認用の入力
    package exam;

    import java.util.List;

    public class Samples {{
        public static final List<String> LINES = List.of(
            {body});
    }}
'''

def app(wiring_header, loop):
    return f'''
    // 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
    package exam;

    public class App {{
        public static void main(String[] args) {{
{wiring_header}            for (String line : Samples.LINES) {{
{loop}
            }}
        }}
    }}
'''

def logexpr(var_type, fmt):
    base = f's.name() + " (" + {var_type} + ") '
    if fmt:
        return base + '" + s.math() + "/" + s.english() + "/" + s.science() + " -> " + (r ? "PASS" : "FAIL")'
    return base + '-> " + (r ? "PASS" : "FAIL")'

def csv_parse(v2, wrap_type):
    if v2:
        return f'''            String[] c = line.split(",");
            return new Entry({wrap_type("c[0]")}, new Student(c[5], Integer.parseInt(c[3]), Integer.parseInt(c[4]), Integer.parseInt(c[2])));'''
    return f'''            String[] c = line.split(",");
            return new Entry({wrap_type("c[0]")}, new Student(c[1], Integer.parseInt(c[2]), Integer.parseInt(c[3]), Integer.parseInt(c[4])));'''

def plain(line=180, recommend=False, logfmt=False, v2=False):
    chain = '            else if (type.equals("recommendation")) r = s.total() >= 150 && s.english() >= 70;\n' if recommend else ''
    exam = f'''
    package exam;

    public class Exam {{
        public static boolean judge(String type, Student s) {{
            boolean r;
            if (type.equals("general")) r = s.total() >= {line};
            else if (type.equals("scholarship")) r = s.total() / 3 >= 70 && s.math() >= 60 && s.english() >= 60 && s.science() >= 60;
            else if (type.equals("transfer")) r = s.math() + s.english() >= 120;
{chain}            else throw new IllegalArgumentException("unknown type: " + type);
            System.out.println({logexpr("type", logfmt)});
            return r;
        }}
    }}
'''
    csv = f'''
    package exam;

    public class CsvLine {{
        public record Entry(String type, Student student) {{}}

        public static Entry parse(String line) {{
{csv_parse(v2, lambda t: t)}
        }}
    }}
'''
    return {'Student.java': STUDENT, 'Exam.java': exam, 'CsvLine.java': csv,
            'App.java': app('', '''                CsvLine.Entry e = CsvLine.parse(line);
                Exam.judge(e.type(), e.student());''')}

def enumv(line=180, recommend=False, logfmt=False, v2=False):
    rec = '        RECOMMENDATION(s -> s.total() >= 150 && s.english() >= 70),\n' if recommend else ''
    et = f'''
    package exam;

    import java.util.function.Predicate;

    public enum ExamType {{
        GENERAL(s -> s.total() >= {line}),
        SCHOLARSHIP(s -> s.total() / 3 >= 70 && s.math() >= 60 && s.english() >= 60 && s.science() >= 60),
{rec}        TRANSFER(s -> s.math() + s.english() >= 120);

        private final Predicate<Student> rule;

        ExamType(Predicate<Student> rule) {{ this.rule = rule; }}

        public boolean passes(Student s) {{ return rule.test(s); }}

        public static ExamType from(String text) {{ return valueOf(text.toUpperCase()); }}
    }}
'''
    exam = f'''
    package exam;

    public class Exam {{
        public static boolean judge(ExamType type, Student s) {{
            boolean r = type.passes(s);
            System.out.println({logexpr("type.name().toLowerCase()", logfmt)});
            return r;
        }}
    }}
'''
    csv = f'''
    package exam;

    public class CsvLine {{
        public record Entry(ExamType type, Student student) {{}}

        public static Entry parse(String line) {{
{csv_parse(v2, lambda t: "ExamType.from(%s)" % t)}
        }}
    }}
'''
    return {'Student.java': STUDENT, 'ExamType.java': et, 'Exam.java': exam, 'CsvLine.java': csv,
            'App.java': app('', '''                CsvLine.Entry e = CsvLine.parse(line);
                Exam.judge(e.type(), e.student());''')}

def patterns(line=180, recommend=False, logfmt=False, v2=False):
    f = {'Student.java': STUDENT}
    f['PassingRule.java'] = '''
    package exam;

    public interface PassingRule {
        boolean passes(Student student);
    }
'''
    f['GeneralRule.java'] = f'''
    package exam;

    class GeneralRule implements PassingRule {{
        @Override
        public boolean passes(Student s) {{ return s.total() >= {line}; }}
    }}
'''
    f['ScholarshipRule.java'] = '''
    package exam;

    class ScholarshipRule implements PassingRule {
        @Override
        public boolean passes(Student s) {
            return s.total() / 3 >= 70 && s.math() >= 60 && s.english() >= 60 && s.science() >= 60;
        }
    }
'''
    f['TransferRule.java'] = '''
    package exam;

    class TransferRule implements PassingRule {
        @Override
        public boolean passes(Student s) { return s.math() + s.english() >= 120; }
    }
'''
    if recommend:
        f['RecommendationRule.java'] = '''
    package exam;

    class RecommendationRule implements PassingRule {
        @Override
        public boolean passes(Student s) { return s.total() >= 150 && s.english() >= 70; }
    }
'''
    rec = '                case "recommendation" -> new RecommendationRule();\n' if recommend else ''
    f['PassingRules.java'] = f'''
    package exam;

    public final class PassingRules {{
        private PassingRules() {{}}

        public static PassingRule of(String type) {{
            return switch (type) {{
                case "general" -> new GeneralRule();
                case "scholarship" -> new ScholarshipRule();
                case "transfer" -> new TransferRule();
{rec}                default -> throw new IllegalArgumentException("unknown type: " + type);
            }};
        }}
    }}
'''
    f['Judge.java'] = '''
    package exam;

    public interface Judge {
        boolean passes(String type, Student student);
    }
'''
    f['ScoreJudge.java'] = '''
    package exam;

    public class ScoreJudge implements Judge {
        @Override
        public boolean passes(String type, Student student) {
            return PassingRules.of(type).passes(student);
        }
    }
'''
    f['LoggingJudge.java'] = f'''
    package exam;

    public class LoggingJudge implements Judge {{
        private final Judge inner;

        public LoggingJudge(Judge inner) {{ this.inner = inner; }}

        @Override
        public boolean passes(String type, Student s) {{
            boolean r = inner.passes(type, s);
            System.out.println({logexpr("type", logfmt)});
            return r;
        }}
    }}
'''
    f['StudentSource.java'] = '''
    package exam;

    public interface StudentSource {
        Entry parse(String line);

        record Entry(String type, Student student) {}
    }
'''
    f['CsvV1Adapter.java'] = '''
    package exam;

    public class CsvV1Adapter implements StudentSource {
        @Override
        public Entry parse(String line) {
            String[] c = line.split(",");
            return new Entry(c[0], new Student(c[1], Integer.parseInt(c[2]), Integer.parseInt(c[3]), Integer.parseInt(c[4])));
        }
    }
'''
    if v2:
        f['CsvV2Adapter.java'] = '''
    package exam;

    public class CsvV2Adapter implements StudentSource {
        @Override
        public Entry parse(String line) {
            String[] c = line.split(",");
            return new Entry(c[0], new Student(c[5], Integer.parseInt(c[3]), Integer.parseInt(c[4]), Integer.parseInt(c[2])));
        }
    }
'''
    adapter = 'CsvV2Adapter' if v2 else 'CsvV1Adapter'
    f['App.java'] = app(f'''            StudentSource source = new {adapter}();
            Judge judge = new LoggingJudge(new ScoreJudge());
''', '''                StudentSource.Entry e = source.parse(line);
                judge.passes(e.type(), e.student());''')
    return f

VERSIONS = {'plain': plain, 'enum': enumv, 'patterns': patterns}
REQUESTS = {
    'before': dict(),
    'r1': dict(line=170),
    'r2': dict(recommend=True),
    'r3': dict(logfmt=True),
    'r4': dict(v2=True),
}
SAMPLES = {
    'before': BASE_V1,
    'r1': BASE_V1,
    'r2': BASE_V1 + ["recommendation,山田,50,70,40"],
    'r3': BASE_V1,
    'r4': BASE_V2,
}
for v, fn in VERSIONS.items():
    for r, kw in REQUESTS.items():
        shutil.rmtree(f'{v}/{r}', ignore_errors=True)
        files = fn(**kw)
        files['Samples.java'] = samples(SAMPLES[r])
        put(f'{v}/{r}', files)
