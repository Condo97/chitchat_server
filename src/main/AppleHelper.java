package main;

import constants.Constants;
import objects.IAPObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AppleHelper {
    private static HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).connectTimeout(Duration.ofMinutes(Constants.APPLE_TIMEOUT_MINUTES)).build();
    private static IAPObject isPremium(String receiptString, boolean isSandbox) throws IOException, InterruptedException, JSONException {
        JSONObject json = new JSONObject();
        json.put("receipt-data", receiptString);
        json.put("password", SecretSomething.sharedAppSecret);
        json.put("exclude-old-transactions", true);

        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json.toString())).uri(URI.create(isSandbox ? Constants.Sandbox_Apple_URL : Constants.Apple_URL)).setHeader("Content-Type", "application/json").build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

//        System.out.println("isPremiumAppleResponse: " + response.body());

        JSONObject body = new JSONObject(response.body());

        // Check if the call is from Sandbox
        if(body.has("status")) {
            try {
                if (body.getInt("status") == 21007)
                    // Try again with the Sandbox URL
                    return isPremium(receiptString, true);
            } catch(JSONException e) {
                System.out.println("Apple status was not an int!");
            }
        }

        if(body.has("receipt")) {
            JSONObject receiptJSONObject = body.getJSONObject("receipt");
            if (receiptJSONObject.has("in_app")) {
                JSONArray inApp = receiptJSONObject.getJSONArray("in_app");
                if (inApp.length() > 0) {
                    if (body.has("pending_renewal_info")) {
//                        System.out.println("Pending renewal Info found!");
                        JSONArray objectArray = body.getJSONArray("pending_renewal_info");
//                        System.out.println("Pending Renewal Object: " + objectArray.getJSONObject(0));

                        if (objectArray.length() > 0) {
                            JSONObject object = objectArray.getJSONObject(0);
//                            System.out.println(object);

                            if (object.has("expiration_intent")) {
                                // if expiration_intent is present, it tells us the subscription has expired
//                                if (object.getString("expiration_intent").equals("1"))
//                                    return false;
                                return new IAPObject(false, Integer.parseInt(object.getString("expiration_intent")));
                            } else {
                                return new IAPObject(true, -1);
                            }
                        } else {
                            //No objects in object_array
                            return new IAPObject(false, 0);
                        }
                    } else {
                        //TODO: - When would this be called if ever?
                        return new IAPObject(false, 0);
                    }
                } else {
                    //No in app purchases for receipt
                    return new IAPObject(false, 0);
                }
            } else {
                //Doesn't have in_app field
                return new IAPObject(false, 0);
            }
        } else {
            //No receipt field somehow
            return new IAPObject(false, 0);
        }
    }

    /***
     * Ensure all calls to isPremium try the production Apple servers first
     *
     * @param receiptString
     * @return
     */
    public static boolean isPremium(String receiptString) throws IOException, InterruptedException {
        return isPremium(receiptString, false).isPremium();
    }

    /***
     * Gets the IAPObject for analytics purposes
     *
     * @param receiptString
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static IAPObject isPremiumForAnalytics(String receiptString) throws IOException, InterruptedException {
        return isPremium(receiptString, false);
    }
}
