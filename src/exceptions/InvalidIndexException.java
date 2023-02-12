package exceptions;

public class InvalidIndexException extends Exception {
    private String description;

    public InvalidIndexException(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "InvalidIndexException{" +
                "description='" + description + '\'' +
                '}';
    }
}
