package exam;

import java.util.function.Predicate;

// 関数と説明を組にした値
public record Rule(Predicate<Student> test, String explanation) {}
