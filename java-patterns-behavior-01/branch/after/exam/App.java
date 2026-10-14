// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

public class App {
    public static void main(String[] args) {
        Student s = new Student("佐藤", 60, 70, 80);
        ExamService service = new ExamService();
        for (String type : new String[] {"general", "scholarship", "transfer", "recommendation"}) {
            System.out.println(type + ": " + service.judge(type, s) + "（" + service.explain(type) + "）");
        }
    }
}
