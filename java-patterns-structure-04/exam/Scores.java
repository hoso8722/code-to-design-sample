package exam;

// 科目の点数を、名前の付いた型にまとめる（引数の取り違えを、型で防ぐ）
public record Scores(int math, int english, int science) {}
