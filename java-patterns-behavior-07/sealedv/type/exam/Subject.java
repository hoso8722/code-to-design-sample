package exam;

// 科目。種類は sealed で閉じ、処理は switch で書く
public sealed interface Subject permits Required, Elective, Practical, Oral {
    String name();
    int points();
}
