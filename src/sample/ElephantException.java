package sample;

/**
 * Created by regnarock on 15/06/2014.
 */
public class ElephantException extends RuntimeException {

    public ElephantException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElephantException(String message) {
        super(message);
    }
}
