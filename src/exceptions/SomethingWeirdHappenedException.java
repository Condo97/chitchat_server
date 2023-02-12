package exceptions;

public class SomethingWeirdHappenedException extends Throwable {
    private String description;

    public SomethingWeirdHappenedException(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "SomethingWeirdHappenedException{" +
                "description='" + description + '\'' +
                '}';
    }
}
