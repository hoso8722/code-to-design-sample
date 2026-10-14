// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

public class Main {
    public static void main(String[] args) {
        Exam exam = new ScholarshipExam();   // 呼び出し側が、具体的なサブクラスを選ぶ
        System.out.println(exam.describe());
        System.out.println(exam.judge(Samples.HANAKO));
    }
}
