import java.security.SecureRandom;

public class ConfirmationCodeGenerator {
    protected static SecureRandom random = new SecureRandom();

    public synchronized String generateConfirmationCode() {
        long longToken = Math.abs(random.nextLong());
        return Long.toString(longToken, 64).substring(0,5);
    }
}
