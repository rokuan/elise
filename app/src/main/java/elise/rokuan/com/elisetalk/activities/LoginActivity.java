package elise.rokuan.com.elisetalk.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import elise.rokuan.com.elisetalk.R;
import elise.rokuan.com.elisetalk.service.MessageService;

/**
 * Created by LEBEAU Christophe on 04/02/2015.
 */
public class LoginActivity extends Activity implements View.OnClickListener {
    private EditText loginText;
    private boolean bound;

    private MessageService service;

    //private Messenger serviceMessenger = new Messenger(new IMHandler());
    private Messenger serviceMessenger;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceMessenger = new Messenger(service);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceMessenger = null;
            bound = false;
        }
    };

    class IMHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            /*
            public static final int LOGIN_REQUEST = 0;
            public static final int LOGIN_RESPONSE = 1;
            public static final int ONLINE_USERS = 2;
            public static final int USER_STATUS_CHANGE = 3;
            public static final int STATUS_CHANGE = 4;
            public static final int USER_CONNECT = 5;
            public static final int USER_DISCONNECT = 6;
            public static final int NEW_USER_MESSAGE = 7;
            public static final int NEW_GROUP_MESSAGE = 8;
            public static final int SEND_MESSAGE = 9;
            public static final int SEND_GROUP_MESSAGE = 10;
            public static final int MESSAGE_NOT_DELIVERED = 11;
            public static final int GROUP_MESSAGE_NOT_DELIVERED = 12;
            public static final int LOGOUT = 13;
            */
            switch(msg.what){
                case MessageService.LOGIN_RESPONSE:
                    JSONObject data = (JSONObject)msg.obj;

                    try {
                        if(data.getBoolean("result")){
                            Log.i("Login result", "SUCCESS");
                        } else {
                            Log.i("Login result", "ERROR (" + data.getString("errorMessage") + ")");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginText = (EditText)findViewById(R.id.login_field);
        findViewById(R.id.login_button).setOnClickListener(this);
        findViewById(R.id.logout_button).setOnClickListener(this);

        startMessagingService();
    }

    @Override
    public void onStart(){
        super.onStart();
        bindService(new Intent(this, MessageService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }
    }

    private void startMessagingService(){
        startService(new Intent(LoginActivity.this, MessageService.class));
    }

    private void stopMessagingService(){
        stopService(new Intent(LoginActivity.this, MessageService.class));
    }

    @Override
    public void onClick(View v) {
        Message msg;

        switch(v.getId()){
            case R.id.login_button:
                if(loginText.getText().length() > 0){
                    JSONObject data = new JSONObject();

                    try {
                        data.put("username", loginText.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }

                    msg = Message.obtain(null, MessageService.LOGIN_REQUEST, data);
                    try {
                        serviceMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case R.id.logout_button:
                //stopMessagingService();
                msg = Message.obtain(null, MessageService.LOGOUT);
                try {
                    serviceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
