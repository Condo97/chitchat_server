package objects;

public class User_AuthToken {
    private int userID;
    private String authToken;

    public User_AuthToken(int userID, String authToken) {
        this.userID = userID;
        this.authToken = authToken;
    }

    public int getUserID() {
        return userID;
    }

    public String getAuthToken() {
        return authToken;
    }

    @Override
    public String toString() {
        return "User_AuthToken{" +
                "userID=" + userID +
                ", authToken='" + authToken + '\'' +
                '}';
    }
}
