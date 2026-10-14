package exam;

// 状態ごとの振る舞い。許されない操作は、既定で例外にする
public interface State {
    default State take() { throw new IllegalStateException(name() + " では受験できません"); }
    default State grade(int score) { throw new IllegalStateException(name() + " では採点できません"); }
    default State announce() { throw new IllegalStateException(name() + " では発表できません"); }
    default State cancel() { throw new IllegalStateException(name() + " では取り消せません"); }

    String name();
    String label();
}
