package elise.rokuan.com.elisetalk.message;

/**
 * Created by LEBEAU Christophe on 02/02/2015.
 */
public class Message {
    private boolean self;
    private String from;
    private String content;

    public Message(boolean userMessage, String messageFrom, String messageContent){
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
