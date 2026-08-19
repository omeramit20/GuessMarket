package engine.exception;

public class DuplicateEventIdException extends GuessMarketException {
    public DuplicateEventIdException(int id) {
        super("Error: Duplicate Event ID found (" + id + "). Every event must have a unique ID.");
    }
}