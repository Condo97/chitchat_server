package objects;

import java.sql.Date;
import java.sql.Timestamp;

public class User_Login {
    private int loginID, userID;
    private Timestamp date;

    public User_Login(int loginID, int userID, Timestamp date) {
        this.loginID = loginID;
        this.userID = userID;
        this.date = date;
    }

    public int getLoginID() {
        return loginID;
    }

    public int getUserID() {
        return userID;
    }

    public Timestamp getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "User_Login{" +
                "loginID=" + loginID +
                ", userID=" + userID +
                ", date=" + date +
                '}';
    }
}
