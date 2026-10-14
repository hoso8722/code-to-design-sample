package exam;

import java.util.List;

// 子クラスで、final の run を上書きしようとする
public class OverrideRun extends ExamProcess {
    @java.lang.Override protected boolean judge(Student s) { return true; }
    @java.lang.Override protected String label() { return "上書き"; }

    @java.lang.Override
    public String run(List<String> lines) {
        return "流れを変えてしまった";
    }
}
