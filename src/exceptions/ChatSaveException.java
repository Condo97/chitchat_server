package exceptions;

public class ChatSaveException extends Exception {
    private String description;

    public ChatSaveException(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ArtSaveException{" +
                "description='" + description + '\'' +
                '}';
    }
}
