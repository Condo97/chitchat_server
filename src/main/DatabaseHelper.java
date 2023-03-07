package main;

import exceptions.*;
import objects.Chat;
import objects.Receipt;
import objects.User_Login;

import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Random;


//ResultSet rs = ps.executeQuery(query)

public class DatabaseHelper {
    private Connection conn = null;
    public DatabaseHelper() throws SQLException {
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/chitchat_schema?user=serverconnection&password=" + SecretSomething.mysqlPassword);
    }

    public void close() throws SQLException {
        conn.close();
    }

    /*** Register User
     *
     * Inserts new User into DB, obtaining user_id.
     * auth_token is generated.
     * user_id and auth_token are stored in User_AuthToken.
     * jUser is created and returned.
     *
     * @return AuthToken as String
     * @throws SQLException
     * @throws SomethingWeirdHappenedException
     */
    public String registerUser() throws SQLException, SomethingWeirdHappenedException {
        /* Generate AuthToken */
        Random rd = new Random();
        byte[] bytes = new byte[128];
        rd.nextBytes(bytes);

        String authToken = Base64.getEncoder().encodeToString(bytes);

        /* Store AuthToken */
        PreparedStatement ps = conn.prepareStatement("INSERT INTO User_AuthToken (user_id, auth_token) VALUES (NULL, ?);");
        ps.setString(1, authToken);

        ps.executeUpdate();
        ps.close();

        /* Get userID */
        ps = conn.prepareStatement("SELECT user_id FROM User_AuthToken WHERE auth_token=?");
        ps.setString(1, authToken);

        ResultSet rs = ps.executeQuery();

        int count = 0;
        int userID = -1;
        while(rs.next()) {
            userID = rs.getInt("user_id");
            count++;
        }

        ps.close();

        if(count > 1) throw new SomethingWeirdHappenedException("registerUser() validate: Unexpected row while registering new User... SQL get a hold of yourself, in theory this message is impossible!");
        if(userID == -1) throw new SomethingWeirdHappenedException("registerUser() validate: Didn't raise SQLException, but didn't create a row? In theory I should never see this message...");

        /* Store UserLogin */
        Calendar cal = Calendar.getInstance();
        java.util.Date now = cal.getTime();
        long time = now.getTime();

        ps = conn.prepareStatement("INSERT INTO User_Login (login_id, user_id, login_date) VALUES (NULL, ?, ?);");

        ps.setInt(1, userID);
        ps.setTimestamp(2, new java.sql.Timestamp(time));

        ps.executeUpdate();
        ps.close();

        /* Validate UserLogin */
        ps = conn.prepareStatement( "SELECT login_date FROM User_Login WHERE user_id=?;");
        ps.setInt(1, userID);

        rs = ps.executeQuery();

        count = 0;
        while(rs.next()) {
            count++;
        }

        ps.close();

        if(count == 0) throw new SomethingWeirdHappenedException("registerUser() userlogin: Couldn't register the user on a previous step but somehow didn't throw an SQLException.");
        if(count > 1) throw new SomethingWeirdHappenedException("registerUser() userlogin: Multiple logins even though this is the user's first login? Weird...");

        /* Return AuthToken! :) */
        return authToken;
    }

    /*** Login
     *
     * Exclusively used for analytics, this simply uses the UserID to add a login for a user.
     */
    public void login(final int userID) throws SQLException, MissingRowException, DuplicateRowException, SomethingWeirdHappenedException {
        /* Store UserLogin */
        Calendar cal = Calendar.getInstance();
        java.util.Date now = cal.getTime();
        long time = now.getTime();

        PreparedStatement ps = conn.prepareStatement("INSERT INTO User_Login (login_id, user_id, login_date) VALUES (NULL, ?, ?);");

        ps.setInt(1, userID);
        ps.setTimestamp(2, new java.sql.Timestamp(time));

        ps.executeUpdate();
        ps.close();
    }

    public int getUserIDFromAuthToken(String authToken) throws SQLException, DuplicateRowException, MissingRowException {
        /* Get userID */
        PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM User_AuthToken WHERE auth_token=?");
        ps.setString(1, authToken);

        ResultSet rs = ps.executeQuery();

        int count = 0;
        int userID = -1;
        while(rs.next()) {
            userID = rs.getInt("user_id");
            count++;
        }

        ps.close();

        if(count > 1) throw new DuplicateRowException("userID","getUserIDFromAuthToken()");
        if(userID == -1) throw new MissingRowException("userID", "getUserIDFromAuthToken()");

        return userID;
    }

    public ArrayList<User_Login> getLogins(final int userID) throws SQLException {
        /* Get Logins List */
        PreparedStatement ps = conn.prepareStatement("SELECT login_id, login_date FROM User_Login WHERE user_id=?;");
        ps.setInt(1, userID);

        ResultSet rs = ps.executeQuery();

        ArrayList<User_Login> array = new ArrayList<User_Login>();
        while(rs.next()) {
            int login_id = rs.getInt("login_id");
            java.sql.Timestamp date = rs.getTimestamp("login_date");

            array.add(new User_Login(login_id, userID, date));
        }

        ps.close();

        return array;
    }

    /***
     * Creates a Chat object in Database and returns the chat_id
     *
     * @param userID
     * @param userText
     * @param date
     * @return
     * @throws SQLException
     */
    public long addChat(final int userID, final String userText, final Timestamp date) throws SQLException, SomethingWeirdHappenedException {
        /* Insert Chat */
        PreparedStatement ps = conn.prepareStatement("INSERT INTO Chat (chat_id, user_id, user_text, date) VALUES (NULL, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, userID);
        ps.setString(2, userText);
        ps.setTimestamp(3, date);

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        long chatID = -1;
        while (rs.next()) {
            System.out.println(rs.getLong(1));
            chatID = rs.getLong(1);
        }

        ps.close();

        if (chatID == -1) throw new SomethingWeirdHappenedException("No new row in addChat?");

        return chatID;
    }

    public void updateChat(final long chatID, final String aiText) throws SQLException {
        /* Update Chat Adding AI Text */
        PreparedStatement ps = conn.prepareStatement("UPDATE Chat SET ai_text=? WHERE chat_id=?");
        ps.setString(1, aiText);
        ps.setLong(2, chatID);

        ps.executeUpdate();
        ps.close();
    }

    public ArrayList<Chat> getAllChats(int userID) throws SQLException {
        /* Query DB for Chat */
        PreparedStatement ps = conn.prepareStatement("SELECT chat_id, user_text, ai_text, date FROM Chat WHERE user_id=?;");
        ps.setInt(1, userID);

        ResultSet rs = ps.executeQuery();

        ArrayList<Chat> allChats = new ArrayList<Chat>();
        while (rs.next()) {
            int chat_id = rs.getInt("chat_id");
            String user_text = rs.getString("user_text");
            String ai_text = rs.getString("ai_text");
            Timestamp date = rs.getTimestamp("date");


            allChats.add(new Chat(chat_id, userID, user_text, ai_text, date));
        }

        ps.close();

        return allChats;
    }

    public java.sql.Timestamp getTimestampOfLastChatGenerated(int userID) throws SQLException, SomethingWeirdHappenedException {
        /* Query DB for Most Recent Chat Generated by userID */
        PreparedStatement ps = conn.prepareStatement("SELECT chat_id, date FROM Chat WHERE user_id=? ORDER BY date DESC LIMIT 1;");
        ps.setInt(1, userID);

        ResultSet rs = ps.executeQuery();

        java.sql.Timestamp date = null;
        int count = 0;
        while(rs.next()) {
            date = rs.getTimestamp("date");
            count++;
        }

        ps.close();

        if(count == 0) return null;
        if(count > 1) throw new SomethingWeirdHappenedException("getTimestampOfLastChatGenerated(): Got multiple rows when trying to find the most recent chat created.");

        return date;
    }

    public Receipt getMostRecentReceipt(int userID) throws SQLException, SomethingWeirdHappenedException {
        /* Query DB for Most Recent Receipt by userID */
        PreparedStatement ps = conn.prepareStatement("SELECT receipt_id, receipt_data, record_date, check_date, expired FROM Receipt WHERE user_id=? ORDER BY record_date DESC LIMIT 1;");
        ps.setInt(1, userID);

        ResultSet rs = ps.executeQuery();

        Receipt receipt = null;
        int count = 0;
        while(rs.next()) {
            receipt = new Receipt(rs.getInt("receipt_id"), userID, rs.getString("receipt_data"), rs.getTimestamp("record_date"), rs.getTimestamp("check_date"), rs.getBoolean("expired"));
            count++;
        }

        ps.close();

        if(count > 1) throw new SomethingWeirdHappenedException("getMostRecentReceipt(): Got multiple rows when trying to find the most recent receipt.");

        return receipt;
    }

    public boolean receiptExists(int userID, String receiptData) throws SQLException {
        /* Query DB for a receipt with userID and receiptData */
        PreparedStatement ps = conn.prepareStatement("SELECT receipt_id FROM Receipt WHERE user_id=? AND receipt_data=? ORDER BY record_date DESC LIMIT 1;");
        ps.setInt(1, userID);
        ps.setString(2, receiptData);

        ResultSet rs = ps.executeQuery();

        boolean exists = false;
        while(rs.next()) {
            exists = true;
        }

        ps.close();

        return exists;
    }

    public boolean receiptIsExpired(int userID, String receiptData) throws SQLException {
        /* Query DB for a receipt with userID and receiptData */
        PreparedStatement ps = conn.prepareStatement("SELECT receipt_id, expired FROM Receipt WHERE user_id=? AND receipt_data=? ORDER BY record_date DESC LIMIT 1;");
        ps.setInt(1, userID);
        ps.setString(2, receiptData);

        ResultSet rs = ps.executeQuery();

        boolean expired = false;
        while(rs.next()) {
            expired = rs.getBoolean("expired");
        }

        ps.close();

        return expired;
    }

    public void saveReceiptData(int userID, String receiptData, boolean expired) throws SQLException, ReceiptExistsException {
        if(receiptExists(userID, receiptData)) throw new ReceiptExistsException(userID, receiptData);

        /* Get Date for Timestamp */
        Calendar cal = Calendar.getInstance();
        java.util.Date now = cal.getTime();
        long time = now.getTime();

        /* Save Receipt */
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Receipt (receipt_id, user_id, receipt_data, record_date, check_date, expired) VALUES (NULL, ?, ?, ?, ?, ?);");

            try {
                ps.setInt(1, userID);
                ps.setString(2, receiptData);
                ps.setTimestamp(3, new java.sql.Timestamp(time));
                ps.setTimestamp(4, new java.sql.Timestamp(time));
                ps.setBoolean(5, expired);

                ps.executeUpdate();
            } catch (SQLException e) {
                System.out.println("SQLException when setting objects in ps in saveReceiptData...");
                e.printStackTrace();
            } finally {
                ps.close();
            }
        } catch (SQLException e) {
            System.out.println("SQLException when creating or closing ps in saveReceiptData...");
            e.printStackTrace();
        }
    }

    public void expireReceipt(int userID, String receiptData) throws SQLException {
        /* Update the receipt to Expired status */
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE Receipt SET expired=? WHERE user_id=? AND receipt_data=?;");

            try {
                ps.setBoolean(1, true);
                ps.setInt(2, userID);
                ps.setString(3, receiptData);

                ps.executeUpdate();
            } catch (SQLException e) {
                throw e;
            } finally {
                ps.close();
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public void updateReceiptCheckDate(int userID, String receiptData) throws SQLException {
        /* Get Date for Timestamp */
        Calendar cal = Calendar.getInstance();
        java.util.Date now = cal.getTime();
        long time = now.getTime();

        /* Save Receipt */
        PreparedStatement ps = conn.prepareStatement("UPDATE Receipt SET check_date=? WHERE user_id=? AND receipt_data=?");
        ps.setTimestamp(1, new java.sql.Timestamp(time));
        ps.setInt(2, userID);
        ps.setString(3, receiptData);

        ps.executeUpdate();
        ps.close();
    }

    public ArrayList<Receipt> getAllReceiptsBetweenDates(Timestamp begin, Timestamp end) throws SQLException {
        /* Get All Receipts Between Timestamps */
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM Receipt WHERE record_date >= ? AND record_date < ?");
        ps.setTimestamp(1, begin);
        ps.setTimestamp(2, end);
        ResultSet rs = ps.executeQuery();

        ArrayList<Receipt> receipts = new ArrayList<Receipt>();
        while (rs.next()) {
            int receiptID = rs.getInt("receipt_id");
            int userID = rs.getInt("user_id");
            String receiptData = rs.getString("receipt_data");
            Timestamp recordDate = rs.getTimestamp("record_date");
            Timestamp checkDate = rs.getTimestamp("check_date");
            Boolean isExpired = rs.getBoolean("expired");

            receipts.add(new Receipt(receiptID, userID, receiptData, recordDate, checkDate, isExpired));
        }

        ps.close();

        return receipts;
    }

    public Chat getMostRecentGeneratedChat(int userID) throws SQLException, SomethingWeirdHappenedException {
        /* Query DB for Most Recent Chat by userID */
        PreparedStatement ps = conn.prepareStatement("SELECT chat_id, user_text, ai_text, date FROM Chat WHERE user_id=? ORDER BY date DESC LIMIT 1;");
        ps.setInt(1, userID);

        ResultSet rs = ps.executeQuery();

        Chat chat = null;
        int count = 0;
        while(rs.next()) {
            int chat_id = rs.getInt("chat_id");
            String user_text = rs.getString("user_text");
            String ai_text = rs.getString("ai_text");
            Timestamp date = rs.getTimestamp("date");

            chat = new Chat(chat_id, userID, user_text, ai_text, date);
            count++;
        }

        ps.close();

        if(count > 1) throw new SomethingWeirdHappenedException("getMostRecentChat(): Got multiple rows when trying to find the most recent chat.");

        return chat;
    }

    public int countTodaysChats(int userID) throws SQLException {

        Calendar startOfDayCal = Calendar.getInstance();
        Calendar endOfDayCal = Calendar.getInstance();

        startOfDayCal.set(Calendar.HOUR, 0);
        startOfDayCal.set(Calendar.MINUTE, 0);
        startOfDayCal.set(Calendar.SECOND, 0);

        endOfDayCal.set(Calendar.HOUR, 23);
        endOfDayCal.set(Calendar.MINUTE, 59);
        endOfDayCal.set(Calendar.SECOND, 59);

        Timestamp startOfDayTimestamp = new Timestamp(startOfDayCal.getTime().getTime());
        Timestamp endOfDayTimestamp = new Timestamp(endOfDayCal.getTime().getTime());

        PreparedStatement ps = conn.prepareStatement("SELECT chat_id FROM Chat WHERE user_id=? AND date BETWEEN ? AND ?;");
        ps.setInt(1, userID);
        ps.setTimestamp(2, startOfDayTimestamp);
        ps.setTimestamp(3, endOfDayTimestamp);

//        System.out.println(startOfDayTimestamp);
//        System.out.println(endOfDayTimestamp);

        ResultSet rs = ps.executeQuery();

        int count = 0;
        while(rs.next()) count++;

        ps.close();

        return count;
    }
}
