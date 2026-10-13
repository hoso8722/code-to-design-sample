// 実行: java NpeMessage.java
class Address {
    String city;
}

class Person {
    Address address;       // 設定しないと null
}

public class NpeMessage {
    public static void main(String[] args) {
        Person person = new Person();
        try {
            System.out.println(person.address.city.length());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }
}
