package engine.exception;

public class InvalidCommissionException extends GuessMarketException {
    public InvalidCommissionException(String eventName, int commission) {
        super("Error: Commission for event '" + eventName + "' is " + commission + ". It must be between 0 and 90.");
    }
}