package exceptions;

public class UnavailableQualityException extends Exception {
    private String description;
    private int quality;
    private boolean isPremium;

    public UnavailableQualityException(String description, int quality, boolean isPremium) {
        this.description = description;
        this.quality = quality;
        this.isPremium = isPremium;
    }

    public String getDescription() {
        return description;
    }

    public int getQuality() {
        return quality;
    }

    public boolean isPremium() {
        return isPremium;
    }

    @Override
    public String toString() {
        return "UnavailableQualityException{" +
                "description='" + description + '\'' +
                ", quality=" + quality +
                '}';
    }
}
