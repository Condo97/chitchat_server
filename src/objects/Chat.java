package objects;

import java.sql.Timestamp;

public class Chat {
    private int chatID, userID;
    private String userText, aiText;
    private Timestamp generateDate;

    public Chat(int chatID, int userID, String userText, String aiText, Timestamp generateDate) {
        this.chatID = chatID;
        this.userID = userID;
        this.userText = userText;
        this.aiText = aiText;
        this.generateDate = generateDate;
    }

    public int getChatID() {
        return chatID;
    }

    public int getUserID() {
        return userID;
    }

    public String getUserText() {
        return userText;
    }

    public String getAiText() {
        return aiText;
    }

    public Timestamp getGenerateDate() {
        return generateDate;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "chatID=" + chatID +
                ", userID=" + userID +
                ", userText='" + userText + '\'' +
                ", aiText='" + aiText + '\'' +
                ", generateDate=" + generateDate +
                '}';
    }
}
