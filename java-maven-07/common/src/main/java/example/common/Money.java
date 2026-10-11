package example.common;

public record Money(long yen) {
    public Money plus(Money other) {
        return new Money(yen + other.yen);
    }
}
