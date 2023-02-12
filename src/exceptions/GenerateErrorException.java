package exceptions;

import org.json.JSONObject;

public class GenerateErrorException extends Exception {

    private String description;
    private JSONObject generateInfoJSON;

    public GenerateErrorException(String description, JSONObject generateInfoJSON) {
        this.description = description;
        this.generateInfoJSON = generateInfoJSON;
    }

    public String getDescription() {
        return description;
    }

    public JSONObject getGenerateInfoJSON() {
        return generateInfoJSON;
    }

    @Override
    public String toString() {
        return "GenerateErrorException{" +
                "description='" + description + '\'' +
                "\ngenerateInfoJSON=" + generateInfoJSON +
                '}';
    }
}
