package main;

import constants.Constants;
import objects.OpenAIChat;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AIHelper {

    /***
     * Generate Chat
     *
     * Gets a generated chat from the ChatSonic AI
     *
     * @param inputText
     * @return Generated chat
     * @throws IOException
     * @throws InterruptedException
     */
    public static OpenAIChat generateChat(String inputText, boolean isPremium) throws IOException, InterruptedException {
        System.out.print("Started Generate Chat - ");
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofMinutes(Constants.AI_TIMEOUT_MINUTES)).build();

        JSONObject inputJSON = new JSONObject();
        inputJSON.put("model", Constants.Model_Name);
        inputJSON.put("temperature", Constants.Temperature);
        inputJSON.put("max_tokens", isPremium ? Constants.Token_Limit_Paid : Constants.Token_Limit_Free);
        inputJSON.put("prompt", inputText);

        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(inputJSON.toString()))
                .uri(Constants.OPENAI_URI)
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", "Bearer " + SecretSomething.openAiApi)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject outputJSON = new JSONObject(response.body());
        System.out.println(outputJSON);
//        String outputMessage = "There was an error getting the chat response. Please try again in a few minutes.";
        OpenAIChat chat;
        if (outputJSON.has("choices")) {
            JSONArray choices = outputJSON.getJSONArray("choices");
            if (choices.length() > 0) {
                if (choices.get(0) instanceof JSONObject) {
                    JSONObject choice = (JSONObject)choices.get(0);
                    if (choice.has("text")) {
                        if (choice.has("finish_reason")) {
                            chat = new OpenAIChat(choice.getString("text"), choice.getString("finish_reason"));
                        } else {
                            chat = new OpenAIChat(choice.getString("text"), "none");
                        }
                    } else {
                        chat = new OpenAIChat("There was an error getting the chat response. Please try again in a few minutes.", "");
                    }
                } else {
                    chat = new OpenAIChat("There was an error getting the chat response. Please try again in a few minutes.", "");
                }
            } else {
                chat = new OpenAIChat("There was an error getting the chat response. Please try again in a few minutes.", "");
            }
        } else {
            chat = new OpenAIChat("There was no message object from the server! Please try again.", "");
//            System.out.println(outputJSON);
        }

        return chat;
    }

//    /***
//     * Generate Chat
//     *
//     * Gets a generated chat from the ChatSonic AI
//     *
//     * @param inputText
//     * @return Generated chat
//     * @throws IOException
//     * @throws InterruptedException
//     */
//    public static String generateChat(String inputText) throws IOException, InterruptedException {
//        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
//
//        JSONObject inputJSON = new JSONObject();
//        inputJSON.put("enable_google_results", "true");
//        inputJSON.put("enable_memory", "false");
//        inputJSON.put("input_text", inputText);
//
//        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(inputJSON.toString()))
//                .uri(Constants.CHATSONIC_URI)
//                .setHeader("Content-Type", "application/json")
//                .setHeader("Accept", "application/json")
//                .setHeader("X-API-KEY", SecretSomething.chatsonicApi)
//                .build();
//
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        JSONObject outputJSON = new JSONObject(response.body());
//        String outputMessage;
//        if (outputJSON.has("message")) {
//            outputMessage = outputJSON.getString("message");
//        } else {
//            outputMessage = "There was no message object from the server! Please try again.";
//            System.out.println(outputJSON);
//        }
//
//        return outputMessage;
//    }

//    /**
//     * Generate Art
//     *
//     * Takes unfinished ProcessedArt and adds image data to it
//     *
//     * @param proposedArt
//     * @return Art
//     */
//    public static String getGenerateInfoUrl(Art proposedArt) throws ArtSaveException, IOException, InterruptedException {
//        //Check to see if proposed art has art already
//        if(proposedArt.didGenerateImage()) throw new ArtSaveException("Art for this ID has already been generated! This shouldn't happen tho...");
//
//        //Setup HTTPClient
//        HttpClient client = HttpClient.newBuilder()
//                .version(HttpClient.Version.HTTP_1_1)
//                .build();
//
////        StringBuilder json = new StringBuilder("{\"input\": {" +
////                "\"prompt\": \"" + proposedArt.getPrompt() + "\"," +
////                "\"width\": \"" + proposedArt.getWidth() + "\"," +
////                "\"height\": \"" + proposedArt.getHeight() +"\"," +
////                "\"prompt_strength\": \"" + proposedArt.getPromptStrength() + "\", " +
////                "\"num_outputs\": \"" + proposedArt.getNumOutputs() + "\", " +
////                "\"num_inference_steps\": \"" + proposedArt.getNumInferenceSteps() + "\", " +
////                "\"guidance_scale\": \"" + proposedArt.getGuidanceScale() + "\" ");
//        JSONObject inputJSON = new JSONObject();
//        inputJSON.put("prompt", proposedArt.getPrompt());
//        inputJSON.put("width", proposedArt.getWidth());
//        inputJSON.put("height", proposedArt.getHeight());
//        inputJSON.put("prompt_strength", proposedArt.getPromptStrength());
//        inputJSON.put("num_outputs", proposedArt.getNumOutputs());
//        inputJSON.put("num_inference_steps", proposedArt.getNumInferenceSteps());
//        inputJSON.put("guidance_scale", proposedArt.getGuidanceScale());
//
//        if(proposedArt.getSeed() != -1) inputJSON.put("seed", proposedArt.getSeed());
//
//        JSONObject json = new JSONObject();
//        json.put("version", Constants.Stable_Diffusion_Version_ID);
//        json.put("input", inputJSON);
//
//        //Make request to AI server
//        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json.toString()))
//                .uri(Constants.REPLICATE_STABLE_DIFFUSION_URI)
//                .setHeader("Content-Type", "application/json")
//                .setHeader("Authorization", SecretSomething.replicateAPI)
//                .build();
//
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        //Parse the image out
////        String outputString = new JSONObject(response.body()).get("output").toString().substring(24);
////        String imageString = outputString.substring(0, outputString.length() - 2);
//        JSONObject outputJSON = new JSONObject(response.body());
//        JSONObject urls = outputJSON.getJSONObject("urls");
//        String getURL = urls.getString("get");
//
//
//        //Generate current timestamp
////        Calendar cal = Calendar.getInstance();
////        java.util.Date now = cal.getTime();
////        long time = now.getTime();
////        Timestamp timestamp = new Timestamp(time);
////
////        proposedArt.setImageData(imageString, timestamp);
//
//        return getURL;
//    }
//
//    public static JSONObject getGenerateInfoJSON(String url) throws IOException, InterruptedException {
//        //Setup HTTPClient
//        HttpClient client = HttpClient.newBuilder()
//                .version(HttpClient.Version.HTTP_1_1)
//                .build();
//
//        HttpRequest request = HttpRequest.newBuilder().GET()
//                .uri(URI.create(url))
//                .setHeader("Content-Type", "application/json")
//                .setHeader("Authorization", SecretSomething.replicateAPI)
//                .build();
//
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        return new JSONObject(response.body());
//    }

//    private static boolean canGenerateImage(String userID) {
//        return true;
//    }

    /**
     * Generate Art on the Server
     *
     * Takes unfinished ProposedArt and adds image data to it
     *
     * @param proposedArt
     * @return
     */
    /*
    public static Art generateImage(Art proposedArt) throws ArtSaveException, IOException, InterruptedException {
        //Check to see if proposed art has art already
        if(proposedArt.didGenerateImage()) throw new ArtSaveException("Art for this ID has already been generated! This shouldn't happen tho...");

        //Setup HTTPClient
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        StringBuilder json = new StringBuilder("{\"input\": {" +
                "\"prompt\": \"" + proposedArt.getPrompt() + "\"," +
                "\"width\": \"" + proposedArt.getWidth() + "\"," +
                  "\"height\": \"" + proposedArt.getHeight() +"\"," +
                    "\"prompt_strength\": \"" + proposedArt.getPromptStrength() + "\", " +
                    "\"num_outputs\": \"" + proposedArt.getNumOutputs() + "\", " +
                    "\"num_inference_steps\": \"" + proposedArt.getNumInferenceSteps() + "\", " +
                    "\"guidance_scale\": \"" + proposedArt.getGuidanceScale() + "\" ");
        if(proposedArt.getSeed() != -1) json.append("\"seed\": \"" + proposedArt.getSeed() + "\" ");
        json.append("}}");

        //Make request to AI server
        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .uri(URI.create(Constants.AI_URL))
                .setHeader("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //Parse the image out
        String outputString = new JSONObject(response.body()).get("output").toString().substring(24);
        String imageString = outputString.substring(0, outputString.length() - 2);

        //Generate current timestamp
        Calendar cal = Calendar.getInstance();
        java.util.Date now = cal.getTime();
        long time = now.getTime();
        Timestamp timestamp = new Timestamp(time);

        proposedArt.setImageData(imageString, timestamp);

        return proposedArt;
    }

    private static boolean canGenerateImage(String userID) {
        return true;
    }
    */
}
