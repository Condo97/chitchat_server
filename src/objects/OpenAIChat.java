package objects;

public class OpenAIChat {

    private String text, finishReason;

    public OpenAIChat(String text, String finishReason) {
        this.text = text;
        this.finishReason = finishReason;
    }

    public String getText() {
        return text;
    }

    public String getFinishReason() {
        return finishReason;
    }
}
