package elise.rokuan.com.elisetalk;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import elise.rokuan.com.elisetalk.data.EliseMessage;
import elise.rokuan.com.elisetalk.views.MessageView;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    private static final String HOST_ADDRESS = "88.182.100.15";
    private Socket socket;

    private EditText loginText;
    private Button connectButton;

    private ListView messagesList;
    private MessageAdapter messages;

    private View connectionFrame;
    private View messageFrame;

    private String username;
    private long id;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        connectButton = (Button)this.findViewById(R.id.connect_button);
        loginText = (EditText)this.findViewById(R.id.login_text);
        connectionFrame = this.findViewById(R.id.connection_frame);
        messageFrame = this.findViewById(R.id.message_frame);
        messagesList = (ListView)this.findViewById(R.id.messages_list);

        messageFrame.setVisibility(View.INVISIBLE);
        connectionFrame.setVisibility(View.VISIBLE);

        messages = new MessageAdapter(this, R.layout.my_message_item, new ArrayList<EliseMessage>(100));
        messagesList.setAdapter(messages);

        connectButton.setOnClickListener(this);

        try {
            //socket = IO.socket("http://localhost");
            socket = IO.socket("http://focused.azurewebsites.net/");

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i("Socket", "connected");
                }

            }).on("try_connect_response", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i("Socket", "try_connect_response");

                    JSONObject data = (JSONObject)args[0];

                    try {
                        if(data.getBoolean("result")){
                            Log.i("Connection", "SUCCESS");

                            id = data.getLong("id");
                            username = data.getString("username");

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    connectionFrame.setVisibility(View.INVISIBLE);
                                    messageFrame.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            Log.i("Connection error", data.getString("errorMessage"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    /*if(data.result){
                        $("#connection-form").hide();

                        socket.id = data.id;
                        socket.name = data.username;
                        me = new Contact(data.id, data.username, data.status, true);

                        connectAccountUser(data);

                        $("#all").show();
                    } else {
                        alert("Erreur: " + data.errorMessage);
                    }*/
                }

            }).on("online_users", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i("Socket", "online_users");
                    /*for(var i=0; i<data.users.length; i++){
                        createUser(data.users[i]);
                    }*/
                }

            }).on("user_status_change", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i("Socket", "user_status_change");
                    //switchUserStatus(data.id, data.status);
                }

            }).on("send_message_response", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i("Socket", "disconnected");
                    /*if(!data.result){
                        // TODO: afficher un message d"erreur dans la conversation avec data.message
                        appendErrorMessage(data.to, data.message);
                    }*/
                }

            }).on("new_message", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i("Socket", "new_message");

                    try {
                        JSONObject data = (JSONObject)args[0];

                        /*Iterator<String> keys = data.keys();
                        StringBuilder keysBuffer = new StringBuilder();

                        while(keys.hasNext()){
                            keysBuffer.append(keys.next());
                            keysBuffer.append(';');
                        }

                        Log.i("Message keys", keysBuffer.toString());*/

                        final EliseMessage msg = new EliseMessage(false, data.getString("from"), data.getString("message"));

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                messages.add(msg);
                                messages.notifyDataSetChanged();
                                Log.i("Message", "added '" + msg.getContent() + "'");
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    /*try {
                        openConversation(data.from);
                        conversations[data.from].appendMessageFrom(data.message);
                    }catch(err){
                        // TODO
                    }*/
                }

            }).on("new_user", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    //createUser(data);
                }

            }).on("user_disconnect", new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    //removeUser(data);
                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i("Socket", "disconnected");
                }

            });

            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy(){
        if(socket != null && socket.connected()) {
            socket.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.connect_button:
                if(loginText.getText().length() > 0){
                    JSONObject data = new JSONObject();

                    try {
                        data.put("username", loginText.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }

                    Log.i("Connection", "emitting signal");
                    socket.emit("try_connect_request", data);
                    Log.i("Connection", "request sent");
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
