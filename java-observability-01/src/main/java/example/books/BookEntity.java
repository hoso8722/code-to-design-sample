package example.books;

// 内部で保存する本。公開するデータではないもの(社内メモ)も持つ
public record BookEntity(long id, String title, int copies, String internalMemo) {}
