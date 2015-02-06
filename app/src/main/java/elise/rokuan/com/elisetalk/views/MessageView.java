package elise.rokuan.com.elisetalk.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import elise.rokuan.com.elisetalk.R;
import elise.rokuan.com.elisetalk.data.EliseMessage;

/**
 * Created by LEBEAU Christophe on 03/02/2015.
 */
public class MessageView extends LinearLayout {
    private EliseMessage message;

    public MessageView(Context context, EliseMessage msg) {
        super(context);
        message = msg;
        initMessageView();
    }

    private void initMessageView(){
        if(message != null){
            TextView messageContent;
            LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if(message.isSelf()){
                inflater.inflate(R.layout.my_message_item, this);
                messageContent = (TextView)this.findViewById(R.id.my_message_item_content);
            } else {
                inflater.inflate(R.layout.user_message_item, this);
                ((TextView)this.findViewById(R.id.user_message_item_from)).setText(message.getFrom());
                messageContent = (TextView)this.findViewById(R.id.user_message_item_content);
            }

            messageContent.setText(message.getContent());
        }
    }
}
