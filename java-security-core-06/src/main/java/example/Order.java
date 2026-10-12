package example;

// 注文。持ち主(owner)の利用者名を持つ
public record Order(long id, String owner, String item) {}
