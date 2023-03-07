package main;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import constants.Constants;
import exceptions.*;
import objects.Chat;
import objects.IAPObject;
import objects.OpenAIChat;
import objects.Receipt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class HTTPSHelper implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Start\t" + Thread.currentThread().getName());

        JSONObject responseJSON = new JSONObject();
        int policyRetrieval = Constants.JSON_RETRIEVAL;

        /* Input Stream for POST */
        JSONObject bodyJSON = new JSONObject();
        if(exchange.getRequestMethod().equals("POST")) {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);

            try {
                StringBuilder bodyString = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    bodyString.append(line);
                }

                bodyJSON = new JSONObject(bodyString.toString());

            } catch (Exception e) {
                throw e;
            } finally {
                isr.close();
                br.close();
            }
        }

        System.out.println("Mid\t" + Thread.currentThread().getName());

        try {
            if(exchange.getRequestURI().equals(Constants.GET_CHAT_URI) && exchange.getRequestMethod().equals("POST")) responseJSON.put("Body", generateChat(bodyJSON));
            else if(exchange.getRequestURI().equals(Constants.GET_REMAINING_URI) && exchange.getRequestMethod().equals("POST")) responseJSON.put("Body", getRemaining(bodyJSON));
            else if(exchange.getRequestURI().equals(Constants.REGISTER_USER_URI) && exchange.getRequestMethod().equals("POST")) responseJSON.put("Body", registerUser());
            else if(exchange.getRequestURI().equals(Constants.GET_IMPORTANT_CONSTANTS_URI) && exchange.getRequestMethod().equals("POST")) responseJSON.put("Body", getImportantConstants());
            else if(exchange.getRequestURI().equals(Constants.FULL_VALIDATE_PREMIUM_URI) && exchange.getRequestMethod().equals("POST")) responseJSON.put("Body", fullValidatePremium(bodyJSON));
            else if(exchange.getRequestURI().equals(Constants.QUICK_VALIDATE_PREMIUM_URI) && exchange.getRequestMethod().equals("POST")) responseJSON.put("Body", quickValidatePremium(bodyJSON));
            else if(exchange.getRequestURI().equals(Constants.GET_IAP_STUFF_URI) && exchange.getRequestMethod().equals("POST")) responseJSON.put("Body", getIAPStuff(bodyJSON));
            else if(exchange.getRequestURI().equals(Constants.PRIVACY_POLICY_URI) && exchange.getRequestMethod().equals("GET")) policyRetrieval = Constants.PRIVACY_POLICY_RETRIEVAL;
            else if(exchange.getRequestURI().equals(Constants.TERMS_AND_CONDITIONS_URI) && exchange.getRequestMethod().equals("GET")) policyRetrieval = Constants.TERMS_AND_CONDITIONS_RETRIEVAL;
            else if(exchange.getRequestURI().equals(Constants.PRINT_ALL_ACTIVE_SUBSCRIPTIONS_URI) && exchange.getRequestMethod().equals("POST")) responseJSON.put("Body", printAllActiveSubscriptions());

            // Legacy
            else if(exchange.getRequestURI().equals(Constants.GET_DISPLAY_PRICE_URI) && exchange.getRequestMethod().equals("POST")) responseJSON.put("Body", legacyGetDisplayPrice());
            else if(exchange.getRequestURI().equals(Constants.GET_SHARE_URL_URI) && exchange.getRequestMethod().equals("POST")) responseJSON.put("Body", legacyGetShareURL());

            responseJSON.put("Success", Constants.SUCCESS_Success);
        } catch(JSONException e) {
            responseJSON.put("Success", Constants.SUCCESS_JSONException);
            responseJSON.put("Type", "JSONException (Typically a Formatting Issue)");
            responseJSON.put("Message", e.getMessage());
            System.out.println(e);
        } catch(IOException e) {
            responseJSON.put("Success", Constants.SUCCESS_IOException);
            responseJSON.put("Type", "IOException");
            responseJSON.put("Message", e.getMessage());
            System.out.println(e);
        } catch(InterruptedException e) {
            responseJSON.put("Success", Constants.SUCCESS_InterruptedException);
            responseJSON.put("Type", "InterruptedException");
            responseJSON.put("Message", e.getMessage());
            System.out.println(e);
        } catch (SQLException e) {
            responseJSON.put("Success", Constants.SUCCESS_SQLException);
            responseJSON.put("Type", "SQLException");
            responseJSON.put("Message", e.getMessage());
            System.out.println(e);
            e.printStackTrace();
        } catch (SomethingWeirdHappenedException e) {
            responseJSON.put("Success", Constants.SUCCESS_SomethingWeirdHappenedException);
            responseJSON.put("Type", "SomethingWeirdHappenedException");
            responseJSON.put("Message", e.toString()); //toString because this is a custom class
            System.out.println(e);
        } catch (MissingRowException e) {
            responseJSON.put("Success", Constants.SUCCESS_MissingRowException);
            responseJSON.put("Type", "MissingRowException");
            responseJSON.put("Message", e.toString()); //toString because this is a custom class
            System.out.println(e);
        } catch (DuplicateRowException e) {
            responseJSON.put("Success", Constants.SUCCESS_DuplicateRowException);
            responseJSON.put("Type", "DuplicateRowException");
            responseJSON.put("Message", e.toString()); //toString because this is a custom class
            System.out.println(e);
        } catch (InvalidIndexException e) {
            responseJSON.put("Success", Constants.SUCCESS_InvalidIndexException);
            responseJSON.put("Type", "InvalidIndexException");
            responseJSON.put("Message", e.toString()); //toString because this is a custom class
            System.out.println(e);
        } catch (GenerateCapException e) {
            responseJSON.put("Success", Constants.SUCCESS_GenerateCapException);
            responseJSON.put("Type", "GenerateCapException");
            responseJSON.put("Message", e.toString()); //toString because this is a custom class

            String[] responses = {"I'd love to keep chatting, but my program uses a lot of computer power. Please upgrade to unlock unlimited chats.",
                    "Thank you for chatting with me. To continue, please upgrade to unlimited chats.",
                    "I hope I was able to help. If you'd like to keep chatting, please subscribe for unlimited chats. There's a 3 day free trial!",
                    "You are appreciated. You are loved. Show us some support and subscribe to keep chatting.",
                    "Upgrade today for unlimited chats and a free 3 day trial!"};
            int randomIndex = new Random().nextInt(responses.length - 1);

            responseJSON.put("Body", new JSONObject().put("output", responses[randomIndex]));
            System.out.println(e);
        } catch (CooldownException e) {
            responseJSON.put("Success", Constants.SUCCESS_CooldownException);
            responseJSON.put("Type", "CooldownException");
            responseJSON.put("Message", e.getMessage());
            responseJSON.put("SecondsUntilReady", e.getSecondsUntilReady());
            System.out.println(e);
        } catch (UnavailableQualityException e) {
            responseJSON.put("Success", Constants.SUCCESS_UnavailableQualityException);
            responseJSON.put("Type", "UnavailableQualityException");
            responseJSON.put("Message", e.toString()); //toString because this is a custom class
            System.out.println(e);
        } catch (ReceiptExistsException e) {
            responseJSON.put("Success", Constants.SUCCESS_ReceiptExistsException);
            responseJSON.put("Type", "ReceiptExistsException");
            responseJSON.put("Message", e.toString());
            System.out.println(e);
//        } catch (StillGeneratingException e) {
//            responseJSON.put("Success", Constants.SUCCESS_StillGeneratingException);
//            responseJSON.put("Type", "StillGeneratingException");
//            responseJSON.put("Message", e.toString());
        } catch (ChatSaveException e) {
            responseJSON.put("Success", Constants.SUCCESS_ChatSaveException);
            responseJSON.put("Type", "ChatSaveException");
            responseJSON.put("Message", e.toString());
            System.out.println(e);
        } catch (Exception e) {
            responseJSON.put("Success", Constants.SUCCESS_UnhandledException);
            responseJSON.put("Type", "UnhandledException");
            responseJSON.put("Message", e.toString());
            System.out.println("Unhandled Exception...");
            System.out.println(e);
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        } catch (Throwable t) {
            System.out.println(t.toString());
        }

//        System.out.println(responseJSON);

        /* Handle Output Stream */
        OutputStream os = exchange.getResponseBody();

        /* Handle Policy HTML Output */
        if (policyRetrieval == Constants.JSON_RETRIEVAL) {
            //No policy, send JSON
            exchange.sendResponseHeaders(200, responseJSON.toString().length());
            os.write(responseJSON.toString().getBytes());

        } else if(policyRetrieval == Constants.PRIVACY_POLICY_RETRIEVAL) {
            exchange.sendResponseHeaders(200, 0);

            //Privacy policy, load and send
            Scanner sc = new Scanner(new File(Constants.PRIVACY_POLICY_LOCATION));
            String htmlString = sc.useDelimiter("\\Z").next();
            sc.close();
            os.write(htmlString.getBytes("UTF-8"));
            os.write("\r\n\r\n".getBytes());
        } else if(policyRetrieval == Constants.TERMS_AND_CONDITIONS_RETRIEVAL) {
            exchange.sendResponseHeaders(200, 0);

            //Terms and conditions, load and send
            Scanner sc = new Scanner(new File(Constants.TERMS_AND_CONDITIONS_LOCATION));
            String htmlString = sc.useDelimiter("\\Z").next();
            sc.close();
            os.write(htmlString.getBytes("UTF-8"));
            os.write("\r\n\r\n".getBytes());
        } else {
            //Shouldn't ever be called if constants are set up correctly
            responseJSON.put("Success", Constants.SUCCESS_OddRetrievalValue);

            System.out.println("WEIRD THING HAPPENED!");

            exchange.sendResponseHeaders(200, responseJSON.toString().length());

            os.write(responseJSON.toString().getBytes());
        }

        exchange.close();

        os.flush();
        os.close();

        System.out.println("End\t" + Thread.currentThread().getName());
    }

    private JSONObject generateChat(JSONObject json) throws IOException, InterruptedException, JSONException, SQLException, SomethingWeirdHappenedException, MissingRowException, DuplicateRowException, CooldownException, UnavailableQualityException, InvalidIndexException, GenerateCapException, ReceiptExistsException, ChatSaveException {
        /***
         * JSONObject json
         *  authToken (String)
         *  inputText (String)
         *
         */

        // Setup Database
        DatabaseHelper db = new DatabaseHelper();

        try {
            // Testing Time
            long startTime = System.nanoTime();

            /* Read JSON... should catch any JSON errors here before we get into db operations */
            final String authToken = json.getString("authToken");
            final String inputText = json.getString("inputText");

            /* Get userID from AuthToken */
            int userID = db.getUserIDFromAuthToken(authToken);
//        System.out.println(userID);

            /* Add Chat to database */
            /* Generate the Timestamp */
            Calendar cal = Calendar.getInstance();
            java.util.Date now = cal.getTime();
            long time = now.getTime();
            Timestamp timestamp = new Timestamp(time);

            long chatID = db.addChat(userID, inputText, timestamp);

            /* Attempt to quick-validate receipt (within time range) */
            boolean userIsPremium = false;
            Receipt recentReceipt = db.getMostRecentReceipt(userID);

            if (recentReceipt != null) {
                //Get date of most recent receipt
                java.util.Date prevReceiptCheckDate = new java.util.Date(recentReceipt.getCheckDate().getTime());

                long currentSeconds = new java.util.Date().getTime() / 1000;
                long prevReceiptCheckDateSeconds = prevReceiptCheckDate.getTime() / 1000;

//            System.out.println("About to check for premium...");

                if ((currentSeconds - prevReceiptCheckDateSeconds) >= Constants.Delay_Seconds_Premium_Check) {
                    //Apple Premium Check
                    updateAndCheckReceipt(userID, recentReceipt.getReceiptData());

                    recentReceipt = db.getMostRecentReceipt(userID);

//                System.out.println("Successfully checked for premium...");
                }

//            System.out.println("Finished checking for premium. Expired: " + recentReceipt.isExpired());

                userIsPremium = !recentReceipt.isExpired();
            }

            /* Check Last Chat Generated is Within Time Range - Not yet implemented */
            Chat currentChat = db.getMostRecentGeneratedChat(userID);
            long currentSeconds = new java.util.Date().getTime() / 1000;
            long prevChatSeconds = -1;
//        System.out.println("asdfadsf!");

            if (currentChat != null) {
                prevChatSeconds = currentChat.getGenerateDate().getTime() / 1000;
            }

//        System.out.println("Prev Chat Seconds: " + prevArtSeconds);
//        System.out.println("Current Seconds: " + currentSeconds);

            /* Get caps for Quality and (Free or Paid) */
            int cap = -1;

            if (!userIsPremium) cap = Constants.Cap_Chat_Daily_Free;
            else cap = Constants.Cap_Chat_Daily_Paid;

            /* Check Chat Cap for Day */
            int chatCount = db.countTodaysChats(userID);
//        System.out.println("Chat Count: " + chatCount);

            if (cap != -1 && chatCount >= cap)
                throw new GenerateCapException("Too many chats generated today for the user."); //Kind've lazy implementation, could implement it up in the ifs to give more detailed expressions, but it's just gonna be used by me so !

            /* Generate the Chat! */
            OpenAIChat outputChat = AIHelper.generateChat(inputText, userIsPremium);

            /* Save the Chat and get the Chat object */
            db.updateChat(chatID, outputChat.getText());

            /* Get Generate Info URL */
            JSONObject outputJson = new JSONObject();
            outputJson.put("output", outputChat.getText());
            outputJson.put("finishReason", outputChat.getFinishReason());

            if (cap == -1) {
                outputJson.put("remaining", -1);
            } else {
                outputJson.put("remaining", cap - chatCount - 1);
            }

            // Testing time
//        System.out.println((System.nanoTime() - startTime) / 1000000  + "\tms on thread\t" + Thread.currentThread().getName() + "\t\t" + (new SimpleDateFormat("HH:mm", Locale.US)).format(new Date()));

            return outputJson;
        } catch (Exception e) {
            throw e;
        } finally {
            db.close();
        }
    }

    private JSONObject registerUser() throws SQLException, SomethingWeirdHappenedException, JSONException {
        /* Register User in DB, obtaining AuthToken */
        DatabaseHelper db = new DatabaseHelper();
        String authToken = db.registerUser();
        db.close();

        return new JSONObject().put("authToken", authToken);
    }

    private JSONObject getImportantConstants() {
        /* Get Important Constants for the Client */
        JSONObject importantConstants = new JSONObject();

        // Display Prices
        importantConstants.put("weeklyDisplayPrice", Constants.WEEKLY_PRICE);
        importantConstants.put("monthlyDisplayPrice", Constants.MONTHLY_PRICE);
        importantConstants.put("annualDisplayPrice", Constants.YEARLY_PRICE);

        // Other stuff
        importantConstants.put("shareURL", Constants.SHARE_URL);
        importantConstants.put("freeEssayCap", Constants.Cap_Free_Total_Essays);

        return importantConstants;
    }

    private JSONObject getRemaining(JSONObject json) throws SQLException, MissingRowException, DuplicateRowException, SomethingWeirdHappenedException, ReceiptExistsException, IOException, InterruptedException {
        /**
         * JSONObject json
         *  authToken (String)
         *
         */
        /* Initialize DB object */
        DatabaseHelper db = new DatabaseHelper();

        try {
            /* Read JSON... should catch any JSON errors here before we get into db operations */
            final String authToken = json.getString("authToken");

            /* Get userID from AuthToken */
            int userID = db.getUserIDFromAuthToken(authToken);

            /* Attempt to quick-validate receipt (within time range) */
            boolean userIsPremium = false;
            Receipt recentReceipt = db.getMostRecentReceipt(userID);

            if (recentReceipt != null) {
                //Get date of most recent receipt
                java.util.Date prevReceiptCheckDate = new java.util.Date(recentReceipt.getCheckDate().getTime());

                long currentSeconds = new java.util.Date().getTime() / 1000;
                long prevReceiptCheckDateSeconds = prevReceiptCheckDate.getTime() / 1000;

//            System.out.println("About to check for premium...");

                if ((currentSeconds - prevReceiptCheckDateSeconds) >= Constants.Delay_Seconds_Premium_Check) {
                    //Apple Premium Check
                    updateAndCheckReceipt(userID, recentReceipt.getReceiptData());

                    recentReceipt = db.getMostRecentReceipt(userID);

//                System.out.println("Successfully checked for premium...");
                }

//            System.out.println("Finished checking for premium. Expired: " + recentReceipt.isExpired());

                userIsPremium = !recentReceipt.isExpired();
            }

            /* Get caps for Quality and (Free or Paid) */
            int cap = -1;

            if (!userIsPremium) cap = Constants.Cap_Chat_Daily_Free;
            else cap = Constants.Cap_Chat_Daily_Paid;

            /* Check Chat Cap for Day */
            int chatCount = db.countTodaysChats(userID);
//        System.out.println("Chat Count: " + chatCount);

            JSONObject outputJson = new JSONObject();
            if (cap == -1) {
                outputJson.put("remaining", -1);
            } else {
                outputJson.put("remaining", cap - chatCount);
            }

            return outputJson;
        } catch (Exception e) {
            throw e;
        } finally {
            db.close();
        }
    }

    private JSONObject fullValidatePremium(JSONObject json) throws IOException, InterruptedException, SQLException, MissingRowException, DuplicateRowException, ReceiptExistsException, JSONException {
        final String authToken = json.getString("authToken");
        final String receiptString = json.getString("receiptString");

        DatabaseHelper db = new DatabaseHelper();

        final int userID = db.getUserIDFromAuthToken(authToken);

        db.close();

        boolean isPremiumAppleValidated = updateAndCheckReceipt(userID, receiptString);

        return new JSONObject().put("isPremium", isPremiumAppleValidated ? 1 : 0);
    }

    private JSONObject quickValidatePremium(JSONObject json) throws SQLException, MissingRowException, DuplicateRowException, SomethingWeirdHappenedException, JSONException {
        final String authToken = json.getString("authToken");

        DatabaseHelper db = new DatabaseHelper();
        boolean isValid = false;

        try {
            final int userID = db.getUserIDFromAuthToken(authToken);
            isValid = !db.getMostRecentReceipt(userID).isExpired();
        } catch (Exception e) {
            System.out.println("Error at quicklyValidatePremium()");
            throw e;
        } finally {
            db.close();
        }

        return new JSONObject().put("isPremium", isValid ? 1 : 0);
    }

    private JSONObject getIAPStuff(JSONObject json) throws SQLException, MissingRowException, DuplicateRowException {
        final String authToken = json.getString("authToken");

        // Verify AuthToken is in the DB
        DatabaseHelper db = new DatabaseHelper();
        db.getUserIDFromAuthToken(authToken);
        db.close();

        // Get product IDs and put them in a JSONArray
        JSONArray outputProductIDs = new JSONArray();
        outputProductIDs.put(Constants.WEEKLY_NAME);
        outputProductIDs.put(Constants.YEARLY_NAME);
        outputProductIDs.put(Constants.MONTHLY_NAME);

//        for(String productID : SecretSomething.productIDs) {
//            outputProductIDs.put(productID);
//        }

        JSONObject outputJson = new JSONObject();
        outputJson.put("sharedSecret", SecretSomething.sharedAppSecret);
        outputJson.put("productIDs", outputProductIDs);

        return outputJson;
    }

    private boolean updateAndCheckReceipt(int userID, String receiptString) throws SQLException, IOException, InterruptedException, ReceiptExistsException, JSONException {
        DatabaseHelper db = new DatabaseHelper();

        final boolean isPremiumAppleValidated = AppleHelper.isPremium(receiptString);

        if (!db.receiptExists(userID, receiptString)) {
            try {
                db.saveReceiptData(userID, receiptString, !isPremiumAppleValidated); //!isPremiumAppleValidated because if it is validated, that means it is not expired
            } catch(ReceiptExistsException e) {
                e.printStackTrace();
//                System.out.println("Error saving receipt because receipt exists. Why is this happening?!?!?!");
            }
        } else {
            if (!isPremiumAppleValidated) {
                //Apple says the receipt is expired
                if(!db.receiptIsExpired(userID, receiptString)) {
                    //DB says receipt is NOT expired
                    db.expireReceipt(userID, receiptString);
                }
            }
        }

        db.updateReceiptCheckDate(userID, receiptString);

        db.close();

        return isPremiumAppleValidated;
    }

    private JSONObject printAllActiveSubscriptions() throws SQLException, IOException, InterruptedException {
        DatabaseHelper db = new DatabaseHelper();

        ArrayList<Integer> premiumCounts = new ArrayList<>(), counts = new ArrayList<>();
        ArrayList<IAPObject> expiredIAPObjects = new ArrayList<>();
        ArrayList<Timestamp> maxDates = new ArrayList<>();
        int days = 1;
        for(int i = 0; i < days; i++) {

            Calendar beginCal = Calendar.getInstance();
            Calendar endCal = Calendar.getInstance();

            beginCal.add(Calendar.DAY_OF_MONTH, -1);//(i + 1) * -1);
            endCal.add(Calendar.DAY_OF_MONTH, 0);//i * -1);

            java.util.Date beginTime = beginCal.getTime();
            java.util.Date endTime = endCal.getTime();

            Timestamp beginTimestamp = new Timestamp(beginTime.getTime());
            Timestamp endTimestamp = new Timestamp(endTime.getTime());
//            long begin = beginTime.getTime();
//            long endTime = endTime.getTime();

            ArrayList<Receipt> receipts = db.getAllReceiptsBetweenDates(beginTimestamp, endTimestamp);
            ArrayList<Integer> userIDs = new ArrayList<>();

            int premiumCount = 0, count = 0;
            IAPObject iapObject = null;
            Timestamp maxDate = null;
            for (Receipt receipt : receipts) {
                iapObject = AppleHelper.isPremiumForAnalytics(receipt.getReceiptData());
                if (iapObject.isPremium() && !userIDs.contains(receipt.getUserID())) {
                    userIDs.add(receipt.getUserID());
                    premiumCount++;
                } else if (!iapObject.isPremium()) {
                    expiredIAPObjects.add(iapObject);
                }

                maxDate = receipt.getRecordDate();
                count++;
            }

            premiumCounts.add(premiumCount);
            counts.add(count);
            maxDates.add(maxDate);
        }

        db.close();

        int eim1 = 0, ei0 = 0, ei1 = 0, ei2 = 0, ei3 = 0, ei4 = 0, ei5 = 0;
        int ar0 = 0, ar1 = 0;
        for (IAPObject iapObject: expiredIAPObjects) {
            switch (iapObject.getExpirationIntent()) {
                case -1:
                    eim1++;
                    break;
                case 0:
                    ei0++;
                    break;
                case 1:
                    ei1++;
                    break;
                case 2:
                    ei2++;
                    break;
                case 3:
                    ei3++;
                    break;
                case 4:
                    ei4++;
                    break;
                case 5:
                    ei5++;
                    break;
            }
        }

        for (int i = 0; i < counts.size(); i++) {
            System.out.println("Count for " + i + " to " + (i + 1) + " days ago.,,");
            System.out.println(" Total Receipts: " + counts.get(i));
            System.out.println(" Expiration Intent -1:" + eim1 + " 0:" + ei0 + " 1:" + ei1 + " 2:" + ei2 + " 3:" + ei3 + " 4:" + ei4 + " 5:" + ei5);
            System.out.println(" Total premium: " + premiumCounts.get(i));
            System.out.println(" Max date: " + maxDates.get(i));
        }

        return new JSONObject().put("Success", 1);
    }

    /*** LEGACY FUNCTIONS ***/
    private JSONObject legacyGetDisplayPrice() {
        /* Just return a simple JSON containing all the products */
        JSONObject displayPrices = new JSONObject();
        displayPrices.put("weeklyDisplayPrice", Constants.WEEKLY_PRICE);
        displayPrices.put("displayPrice", Constants.WEEKLY_PRICE); // For app versions below 1.4
        displayPrices.put("annualDisplayPrice", Constants.YEARLY_PRICE);
        displayPrices.put("monthlyDisplayPrice", Constants.MONTHLY_PRICE);

        return displayPrices;
    }

    private JSONObject legacyGetShareURL() {
        /* Just return a simple JSON containing all the products */
        JSONObject displayPrices = new JSONObject();
        displayPrices.put("shareURL", Constants.SHARE_URL);

        return displayPrices;
    }
}
