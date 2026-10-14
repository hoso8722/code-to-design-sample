// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import exam.SecuredRepository.Role;

// 保護プロキシと、記録用のプロキシ
public class SecureDemo {
    public static void main(String[] args) {
        LoggingRepository logging = new LoggingRepository(new DatabaseRepository());

        StudentRepository staff = new SecuredRepository(logging, Role.STAFF);
        StudentRepository guest = new SecuredRepository(logging, Role.GUEST);

        System.out.println("職員: " + staff.find("s1"));
        try {
            guest.find("s1");
        } catch (SecurityException e) {
            System.out.println("ゲスト: " + e.getMessage());
        }
        System.out.println("本物に届いた呼び出しの回数: " + logging.calls);
    }
}
