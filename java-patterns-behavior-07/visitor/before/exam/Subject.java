package exam;

// 科目。処理は、Visitor に任せる
public interface Subject {
    String name();
    int points();

    <R> R accept(SubjectVisitor<R> visitor);
}
