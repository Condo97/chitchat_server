package objects;

import java.sql.Timestamp;

public class Receipt {
    private int receiptID, userID;
    private String receiptData;
    private java.sql.Timestamp recordDate, checkDate;
    private boolean expired;

    public Receipt(int receiptID, int userID, String receiptData, Timestamp recordDate, Timestamp checkDate, boolean expired) {
        this.receiptID = receiptID;
        this.userID = userID;
        this.receiptData = receiptData;
        this.recordDate = recordDate;
        this.checkDate = checkDate;
        this.expired = expired;
    }

    public int getReceiptID() {
        return receiptID;
    }

    public int getUserID() {
        return userID;
    }

    public String getReceiptData() {
        return receiptData;
    }

    public Timestamp getRecordDate() {
        return recordDate;
    }

    public Timestamp getCheckDate() {
        return checkDate;
    }

    public boolean isExpired() {
        return expired;
    }
}
