package dateBase;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class PasswordClient {
    private static final Random RANDOM = new SecureRandom();
    private PasswordClient() { }
    public static byte[] getSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return salt;
    }

    // TODO: 27.06.2023 загуглить что этот метод делает
    public static byte[] hash(byte[] password, byte[] salt) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD2");

            byte[] saltedPassword = new byte[password.length + salt.length];
            System.arraycopy(password, 0, saltedPassword, 0, password.length);
            System.arraycopy(salt, 0, saltedPassword, password.length, salt.length);

            byte[] passwordDigest =  messageDigest.digest(saltedPassword);
            return passwordDigest;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("Ошибка при хешировании пароля: " + e.getMessage());
        }
    }
    public static boolean isExpectedPassword(byte[] password, byte[] salt, byte[] expectedHash) {
        byte[] passwordHash = hash(password, salt);
        if (passwordHash.length != expectedHash.length) {
            return false;
        }
        for (int i = 0; i < passwordHash.length; i++) {
            if (passwordHash[i] != expectedHash[i]) {
                return false;
            }
        }
        return true;
    }

    public static String bytesToString(byte[] bytes) {
        BigInteger no = new BigInteger(1, bytes);
        return no.toString(16);
    }


}
