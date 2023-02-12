package exceptions;

public class ReceiptExistsException extends Exception {
    public int userID;
    public String receiptString;

    public ReceiptExistsException(int userID, String receiptString) {
        this.userID = userID;
        this.receiptString = receiptString;
    }

    public int getUserID() {
        return userID;
    }

    public String getReceiptString() {
        return receiptString;
    }

    @Override
    public String toString() {
        return "ReceiptExistsException{" +
                "userID='" + userID + '\'' +
                ", receiptString='" + receiptString + '\'' +
                '}';
    }
}
