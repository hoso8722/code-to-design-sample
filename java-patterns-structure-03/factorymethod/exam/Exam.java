package exam;

// Factory Method：判定ルールの作り方は、サブクラスが決める
public abstract class Exam {
    protected abstract PassingRule createRule();

    public final boolean judge(Student student) {
        return createRule().passes(student);
    }

    public final String describe() {
        return createRule().describe();
    }
}
