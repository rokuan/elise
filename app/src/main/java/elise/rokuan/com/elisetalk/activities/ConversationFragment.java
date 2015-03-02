package elise.rokuan.com.elisetalk.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import elise.rokuan.com.elisetalk.R;
import elise.rokuan.com.elisetalk.data.EliseMessage;
import elise.rokuan.com.elisetalk.data.User;
import elise.rokuan.com.elisetalk.views.MessageView;

/**
 * A fragment containing a form to send and receive messages
 */
public class ConversationFragment extends Fragment implements View.OnClickListener {
    private User user;
    private EditText messageText;
    private ListView messagesList;
    private MessageAdapter adapter;

    private ArrayList<EliseMessage> conversationMessages = new ArrayList<EliseMessage>();

    public static ConversationFragment newInstance(User source){
        ConversationFragment fragment = new ConversationFragment();
        fragment.user = source;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);

        view.findViewById(R.id.conversation_send_message).setOnClickListener(this);
        messageText = (EditText)view.findViewById(R.id.conversation_message_area);
        messagesList = (ListView)view.findViewById(R.id.conversation_messages_list);
        adapter = new MessageAdapter(this.getActivity(), R.layout.user_message_item, conversationMessages);
        messagesList.setAdapter(adapter);

        return view;
    }

    //public void appendMessage(User from, )

    /**
     * Appends a new message to the conversation
     * @param message the message to be added
     */
    public void appendMessage(EliseMessage message){
        /*if(adapter == null){
            adapter = new MessageAdapter(this.getActivity(), R.layout.user_message_item, new ArrayList<EliseMessage>());
            messagesList.setAdapter(adapter);
        }*/
        if(adapter != null) {
            adapter.add(message);
        } else {
            conversationMessages.add(message);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.conversation_send_message:
                String msg = messageText.getText().toString().trim();

                if(msg.length() > 0){
                    messageText.getText().clear();
                    EliseMessage message = new EliseMessage(true, null, msg);
                    appendMessage(message);
                    ((MessageActivity)this.getActivity()).sendMessage(user, message);
                }
                break;
        }
    }

    public class MessageAdapter extends ArrayAdapter<EliseMessage> {
        private static final int MAX_CAPACITY = 100;
        private List<EliseMessage> messages;

        public MessageAdapter(Context context, int resource, List<EliseMessage> messagesSource) {
            super(context, resource, messagesSource);
            messages = messagesSource;
        }

        @Override
        public int getCount(){
            return messages.size();
        }

        @Override
        public EliseMessage getItem(int position){
            return messages.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            return new MessageView(this.getContext(), this.getItem(position));
        }
    }
}
