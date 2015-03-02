package elise.rokuan.com.elisetalk.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * The service that acts as an intermediary between the node.js server and the Android activity
 */
public class MessageService extends Service {
    public static final int LOGIN_REQUEST = 0;
    public static final int LOGIN_RESPONSE = 1;
    public static final int ONLINE_USERS_REQUEST = 2;
    public static final int ONLINE_USERS_RESPONSE = 3;
    public static final int USER_STATUS_CHANGE = 4;
    public static final int STATUS_CHANGE = 5;
    public static final int USER_CONNECT = 6;
    public static final int USER_DISCONNECT = 7;
    public static final int NEW_USER_MESSAGE = 8;
    public static final int NEW_GROUP_MESSAGE = 9;
    public static final int SEND_MESSAGE = 10;
    public static final int SEND_GROUP_MESSAGE = 11;
    public static final int MESSAGE_NOT_DELIVERED = 12;
    public static final int GROUP_MESSAGE_NOT_DELIVERED = 13;
    public static final int LOGOUT = 14;

    private static final String MESSAGE_SERVICE_TAG = "EliseMessageService";
    private static final String SERVER_ADDRESS = "http://focused.azurewebsites.net/";

    private Messenger activityMessenger;
    private Messenger messenger = new Messenger(new IMHandler());
    private NotificationManager notificationManager;

    private Socket socket;

    class IMHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            //JSONObject data;

            switch (msg.what) {
                case LOGIN_REQUEST:
                    Log.i("MessageService", "LOGIN_REQUEST");
                    activityMessenger = msg.replyTo;
                    socket.emit("login_request", msg.obj);
                    break;

                case ONLINE_USERS_REQUEST:
                    //Log.i("MessageService", "ONLINE_USERS_REQUEST");
                    activityMessenger = msg.replyTo;
                    socket.emit("online_users_request", msg.obj);
                    break;

                case STATUS_CHANGE:
                    //data = new JSONObject();
                    break;

                case SEND_MESSAGE:
                    socket.emit("send_message_request", msg.obj);
                    break;

                case SEND_GROUP_MESSAGE:
                    break;

                case LOGOUT:
                    //socket.disconnect();
                    Log.i("MessageService", "LOGOUT_REQUEST");
                    socket.emit("logout_request", msg.obj);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public void onCreate(){
        try {
            socket = IO.socket(SERVER_ADDRESS);

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i("Socket", "connected");
                }

            }).on("login_response", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i("Socket", "login_response");

                    sendEliseMessage(LOGIN_RESPONSE, args[0]);
                }

            }).on("online_users_response", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i("Socket", "online_users_response");

                    sendEliseMessage(ONLINE_USERS_RESPONSE, args[0]);
                }

            }).on("user_status_change", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i("Socket", "user_status_change");

                    sendEliseMessage(USER_STATUS_CHANGE, args[0]);
                }

            }).on("send_message_response", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i("Socket", "disconnected");
                }

            }).on("new_message", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i("Socket", "new_message");

                    sendEliseMessage(NEW_USER_MESSAGE, args[0]);
                }

            }).on("new_user", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    sendEliseMessage(USER_CONNECT, args[0]);
                }

            }).on("user_disconnect", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    sendEliseMessage(USER_DISCONNECT, args[0]);
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

        /*
        //notificationManager = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.service_started);
        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.icon, text, System.currentTimeMillis());
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.service_label), text, contentIntent);
        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        nm.notify(R.string.service_started, notification);
        */
        // TODO:
    }

    private void sendEliseMessage(int messageCode, Object content){
        Message msg = Message.obtain(null, messageCode, content);
        try {
            activityMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    /*@Override
    public void onDestroy(){
        if(socket != null && socket.connected()){
            socket.disconnect();
        }
    }*/
}
