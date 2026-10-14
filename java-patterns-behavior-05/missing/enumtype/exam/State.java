package exam;

// enum：定数ごとに、操作の振る舞いを持つ
public enum State {
    APPLIED("申込済み") {
        @Override State take() { return TAKEN; }
        @Override State grade(int score) { throw new IllegalStateException(name() + " では採点できません"); }
        @Override State announce() { throw new IllegalStateException(name() + " では発表できません"); }
        @Override State cancel() { return CANCELLED; }
    },
    TAKEN("受験済み") {
        @Override State take() { throw new IllegalStateException(name() + " では受験できません"); }
        @Override State grade(int score) { return GRADING; }
        @Override State announce() { throw new IllegalStateException(name() + " では発表できません"); }
        @Override State cancel() { return CANCELLED; }
    },
    GRADING("採点中") {
        @Override State take() { throw new IllegalStateException(name() + " では受験できません"); }
        @Override State grade(int score) { throw new IllegalStateException(name() + " では採点できません"); }
        @Override State announce() { return ANNOUNCED; }
        @Override State cancel() { throw new IllegalStateException(name() + " では取り消しできません"); }
    },
    ANNOUNCED("結果発表済み") {
        @Override State take() { throw new IllegalStateException(name() + " では受験できません"); }
        @Override State grade(int score) { throw new IllegalStateException(name() + " では採点できません"); }
        @Override State announce() { throw new IllegalStateException(name() + " では発表できません"); }
        @Override State cancel() { throw new IllegalStateException(name() + " では取り消しできません"); }
    },
    CANCELLED {
        @Override State take() { throw new IllegalStateException(name() + " では受験できません"); }
        @Override State grade(int score) { throw new IllegalStateException(name() + " では採点できません"); }
        @Override State announce() { throw new IllegalStateException(name() + " では発表できません"); }
        @Override State cancel() { throw new IllegalStateException(name() + " では取り消しできません"); }
    };

    private final String label;

    State(String label) { this.label = label; }

    abstract State take();
    abstract State grade(int score);
    abstract State announce();
    abstract State cancel();

    public String label() { return label; }
}
