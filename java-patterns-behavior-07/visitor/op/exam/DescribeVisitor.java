package exam;

public class DescribeVisitor implements SubjectVisitor<String> {
    @Override public String visitRequired(Required s) { return "必須科目「" + s.name() + "」" + s.points() + "点（配点100%）"; }
    @Override public String visitElective(Elective s) { return "選択科目「" + s.name() + "」" + s.points() + "点（配点50%）"; }
    @Override public String visitPractical(Practical s) { return "実技科目「" + s.name() + "」" + s.points() + "点（配点80%）"; }
}
