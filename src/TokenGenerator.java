import java.security.SecureRandom;

public class TokenGenerator {
    protected static SecureRandom random = new SecureRandom();

    public synchronized String generateToken() {
        long longToken = Math.abs(random.nextLong());
        return Long.toString(longToken, 64);
    }
}
