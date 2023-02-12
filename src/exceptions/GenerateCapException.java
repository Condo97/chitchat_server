package exceptions;

public class GenerateCapException extends Exception {
    String description;

    public GenerateCapException(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "GenerateCapException{" +
                "description='" + description + '\'' +
                '}';
    }
}
