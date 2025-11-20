package Parser;

/**
 * Special exception for Parsing
 */
public class ParseException extends Exception {
    /**
     * constructor of Exception
     *
     * @param message the error message
     */
    public ParseException(String message){
        super(message);
    }
}
