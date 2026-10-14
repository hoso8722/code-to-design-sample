package exam;

final class Work {
    private Work() {}

    // 時間のかかる計算のふり（結果は合計点と同じ）
    static int slowTotal(Student s) {
        int x = 0;
        for (int i = 0; i < 2_000; i++) x += i % 3;
        return s.total() + (x < 0 ? 1 : 0);
    }
}
