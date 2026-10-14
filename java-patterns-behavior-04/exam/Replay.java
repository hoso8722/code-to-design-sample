package exam;

import java.util.List;

// 操作の記録（文字列）から、同じ操作を作り直して実行する
public class Replay {
    public static void apply(ScoreBook book, List<String> log) {
        for (String line : log) {
            String[] t = line.split(" ");
            Command command = switch (t[0]) {
                case "ADD" -> new AddStudent(book, new Student(t[1], Integer.parseInt(t[2]), Integer.parseInt(t[3]), Integer.parseInt(t[4])));
                case "SET" -> new SetScore(book, t[1], t[2], Integer.parseInt(t[3]));
                case "REMOVE" -> new RemoveStudent(book, t[1]);
                default -> throw new IllegalArgumentException(line);
            };
            command.execute();
        }
    }
}
