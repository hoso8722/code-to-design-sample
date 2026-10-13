// 実行: java TextBlock.java
void main() {
    String json = """
        {
          "name": "田中",
          "score": 75
        }
        """;
    IO.print(json);
    IO.println("最後の改行を含めた長さ: " + json.length());

    // 行末の \ で、改行を入れずに続けられる
    String oneLine = """
        これは一行に \
        つながります
        """;
    IO.print(oneLine);

    // 通常の文字列でも書ける。改行や引用符は、エスケープが必要
    String old = "{\n  \"name\": \"田中\",\n  \"score\": 75\n}\n";
    IO.println("テキストブロックと同じ内容: " + old.equals(json));
}
