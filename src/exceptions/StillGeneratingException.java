package exceptions;

public class StillGeneratingException extends Exception {
    private String description;

    public StillGeneratingException(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "StillGeneratingException{" +
                "description='" + description + '\'' +
                '}';
    }
}
