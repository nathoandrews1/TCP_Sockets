package TCP_PRACTICE;

public class IncorrectActionException extends RuntimeException {
    public IncorrectActionException(Throwable err, String inMsg)
    {
        super(inMsg, err);
    }
}
