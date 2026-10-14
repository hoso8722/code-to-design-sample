package exam;

public class MainRecommendation {
    public static void main(String[] args) {
        Exam exam = new RecommendationExam();
        System.out.println(exam.describe());
        System.out.println(exam.judge(new Student("山田", 50, 70, 40)));
    }
}
