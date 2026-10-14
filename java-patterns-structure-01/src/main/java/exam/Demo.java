// 実行: mvn -q compile のあと、java -cp target/classes exam.Demo
package exam;

// 現在の動きを確かめるために、いくつかの入力を判定して表示する
public class Demo {
    public static void main(String[] args) {
        ExamJudge.verbose = true;
        ExamJudge.judge("佐藤", 70, 70, 70, "general");
        ExamJudge.judge("鈴木", 60, 60, 59, "general");
        ExamJudge.judge("高橋", 70, 70, 69, "scholarship");
        ExamJudge.judge("田中", 100, 100, 59, "scholarship");
        ExamJudge.judge("伊藤", 60, 60, 0, "transfer");
        ExamJudge.judge("", 70, 70, 70, "general");
        ExamJudge.judge("渡辺", 101, 70, 70, "general");
    }
}
