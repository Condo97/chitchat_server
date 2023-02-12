package objects;

public class IAPObject {
    private boolean isPremium;
    private int expirationIntent; // -1 means no expiration intent, 0 means receipt was marked expired for other reasons

    public IAPObject(boolean isPremium, int expirationIntent) {
        this.isPremium = isPremium;
        this.expirationIntent = expirationIntent;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public int getExpirationIntent() {
        return expirationIntent;
    }
}
