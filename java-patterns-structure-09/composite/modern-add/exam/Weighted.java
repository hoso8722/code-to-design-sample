package exam;

// 新しい種類を足す：配点を倍にするグループ
public record Weighted(Node inner, int factor) implements Node {}
