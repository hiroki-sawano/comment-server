package commentserver;

/**
 * User register exception
 *
 * @author Hiroki Sawano
 *
 */
public class RegisterException extends Exception {

    public RegisterException() {
        super("Can't accept a user anymore");
    }
}
