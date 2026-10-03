// 実行: java MutableKey.java
import java.util.*;

public class MutableKey {
    // フィールドを変更できるキー（equals と hashCode は name から作る）
    static final class User {
        String name;
        User(String name) { this.name = name; }
        @Override public boolean equals(Object o) { return o instanceof User u && name.equals(u.name); }
        @Override public int hashCode() { return name.hashCode(); }
        @Override public String toString() { return "User(" + name + ")"; }
    }

    public static void main(String[] args) {
        Map<User, String> roles = new HashMap<>();
        User user = new User("alice");
        roles.put(user, "admin");

        user.name = "bob";  // Map に入れたあとで、キーのフィールドを変える

        System.out.println("size()                     = " + roles.size());
        System.out.println("get(user)                  = " + roles.get(user));
        System.out.println("get(new User(\"bob\"))       = " + roles.get(new User("bob")));
        System.out.println("get(new User(\"alice\"))     = " + roles.get(new User("alice")));
        System.out.println("containsKey(user)          = " + roles.containsKey(user));
        System.out.println("remove(user)               = " + roles.remove(user));
        System.out.println("反復して見えるエントリ     = " + roles.entrySet());
    }
}
