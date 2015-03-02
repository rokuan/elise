package elise.rokuan.com.elisetalk.data;

/**
 * This class contains the data for a single message
 */
public class EliseMessage {
    private boolean self;
    private String from;
    private String content;

    public EliseMessage(boolean userMessage, String messageFrom, String messageContent){
        this.self = userMessage;
        this.from = messageFrom;
        this.content = messageContent;
    }

    public boolean isSelf() {
        return self;
    }

    public String getFrom() {
        return from;
    }

    public String getContent() {
        return content;
    }
}
