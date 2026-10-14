package exam;

// 引数を型でまとめた版：Scores を1つ渡す。bonus と preference は、別の型にする
public record TypedApplication(String name, Scores scores, Bonus bonus, Preference preference) {
    public record Bonus(int points) {
        public Bonus {
            if (points < 0 || points > 20) throw new IllegalArgumentException("bonus=" + points);
        }
    }

    public record Preference(int rank) {
        public Preference {
            if (rank < 1 || rank > 3) throw new IllegalArgumentException("preference=" + rank);
        }
    }

    public TypedApplication {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("name");
    }

    public int score() {
        int extra = preference.rank() == 1 ? bonus.points() : bonus.points() / 2;
        return scores.math() + scores.english() * 2 + scores.science() + extra;
    }
}
