package exceptions;

public class CooldownException extends Exception {
    private String description;
    private long secondsUntilReady;

    public CooldownException(String description, long secondsUntilReady) {
        this.description = description;
        this.secondsUntilReady = secondsUntilReady; //test
    }

    public String getMessage() {
        return description;
    }
    public long getSecondsUntilReady() {
        return secondsUntilReady;
    }

    @Override
    public String toString() {
        return "CooldownException{" +
                "description='" + description + '\'' +
                ", secondsUntilReady=" + secondsUntilReady +
                '}';
    }
}
