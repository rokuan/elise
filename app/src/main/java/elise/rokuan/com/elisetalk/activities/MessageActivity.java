package elise.rokuan.com.elisetalk.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import elise.rokuan.com.elisetalk.R;
import elise.rokuan.com.elisetalk.data.EliseMessage;
import elise.rokuan.com.elisetalk.data.User;
import elise.rokuan.com.elisetalk.service.MessageService;

/**
 * Activity to manage contacts and messages
 */
public class MessageActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /*private ImageButton disconnectButton;

    private EditText loginText;*/
    private boolean bound;

    private User thisUser;

    //private Messenger serviceMessenger = new Messenger(new IMHandler());
    private Messenger activityMessenger = new Messenger(new IMHandler());
    private Messenger serviceMessenger;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceMessenger = new Messenger(service);
            bound = true;

            Message msg = Message.obtain(null, MessageService.ONLINE_USERS_REQUEST, new JSONObject());
            msg.replyTo = activityMessenger;
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceMessenger = null;
            bound = false;
        }
    };

    private HashMap<Long, User> allUsers = new HashMap<>();
    private HashMap<Long, ConversationFragment> allConversations = new HashMap<Long, ConversationFragment>();

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
            JSONObject data;

            switch(msg.what){
                case MessageService.LOGIN_RESPONSE:
                    data = (JSONObject)msg.obj;

                    try {
                        if(data.getBoolean("result")){
                            Log.i("Login result", "SUCCESS");
                            // TODO: creer l'utilisateur
                        } else {
                            Log.i("Login result", "ERROR (" + data.getString("errorMessage") + ")");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case MessageService.ONLINE_USERS_RESPONSE:
                    data = (JSONObject)msg.obj;
                    try {
                        if(data.getBoolean("result")) {
                            JSONArray users = data.getJSONArray("users");
                            //System.out.println(users);
                            List<User> onlineUsers = new ArrayList<>(users.length());

                            for (int i = 0; i < users.length(); i++) {
                                //mNavigationDrawerFragment.onUserConnected();
                                JSONObject userElement = users.getJSONObject(i);
                                User u = new User(userElement.getLong("id"), userElement.getString("username"), userElement.getString("status"));
                                onlineUsers.add(u);
                                allUsers.put(u.getId(), u);
                            }

                            mNavigationDrawerFragment.setOnlineUsers(onlineUsers);
                        } else {
                            // TODO: afficher une erreur
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case MessageService.USER_CONNECT:
                    data = (JSONObject)msg.obj;
                    try {
                        User u = new User(data.getLong("id"), data.getString("username"), data.getString("status"));
                        allUsers.put(u.getId(), u);
                        mNavigationDrawerFragment.onUserConnected(u);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case MessageService.USER_STATUS_CHANGE:
                    data = (JSONObject)msg.obj;
                    try {
                        allUsers.get(data.getLong("id")).setStatus(data.getString("status"));
                        mNavigationDrawerFragment.onUserStatusChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case MessageService.USER_DISCONNECT:
                    data = (JSONObject)msg.obj;
                    try {
                        User u = new User(data.getLong("id"), data.getString("username"));
                        allUsers.remove(u.getId());
                        mNavigationDrawerFragment.onUserDisconnected(u);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case MessageService.NEW_USER_MESSAGE:
                    data = (JSONObject)msg.obj;

                    try {
                        User sender = allUsers.get(data.getLong("from"));
                        EliseMessage message = new EliseMessage(false, sender.getName(), data.getString("message"));
                        newUserMessage(sender, message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //setupCustomActionBar();


        Bundle extras = this.getIntent().getExtras();

        thisUser = new User(extras.getLong("id"), extras.getString("name"));
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle(thisUser.getName());

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        //mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        /*loginText = (EditText)findViewById(R.id.login_field);
        findViewById(R.id.login_button).setOnClickListener(this);
        findViewById(R.id.logout_button).setOnClickListener(this);*/

        //startMessagingService();
    }

    @Override
    public void onBackPressed () {
        this.moveTaskToBack(true);
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

    /*private void startMessagingService(){
        startService(new Intent(MessageActivity.this, MessageService.class));
    }

    private void stopMessagingService(){
        stopService(new Intent(MessageActivity.this, MessageService.class));
    }*/

    /*private void setupCustomActionBar(){
        ActionBar bar = this.getSupportActionBar();

        View customBar = ((LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.chat_action_bar, null);
        customBar.findViewById(R.id.chat_disconnect_button);
        bar.setCustomView(customBar);
    }*/

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // TODO: ouvrir la conversation correspondante
        // update the main content by replacing fragments
        /*FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();*/
    }

    public void onSectionAttached(int number) {
        /*switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }*/
        // TODO:
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        //actionBar.setTitle(mTitle);
    }

    /**
     * Sends a new message to the specified user
     * @param user the target
     * @param emsg the message to be sent
     */
    public void sendMessage(User user, EliseMessage emsg){
        JSONObject data = new JSONObject();

        try {
            //data.put("from", thisUser.getId());
            data.put("to", user.getId());
            data.put("message", emsg.getContent());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        Message msg = Message.obtain(null, MessageService.SEND_MESSAGE, data);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when a new message was sent from another user
     * @param user the sender
     * @param message the message
     */
    private void newUserMessage(User user, EliseMessage message){
        if(!allConversations.containsKey(user.getId())){
            createConversation(user);
        }

        allConversations.get(user.getId()).appendMessage(message);
    }

    /**
     * Open the conversation associated with the user. Creates the conversation if there is none
     * @param user the contact
     */
    public void openUserConversation(User user){
        if(!allConversations.containsKey(user.getId())){
            createConversation(user);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, allConversations.get(user.getId()))
                .commit();
    }

    /**
     * Creates a new conversation fragment for the specified user
     * @param user
     */
    private void createConversation(User user){
        ConversationFragment conv = ConversationFragment.newInstance(user);
        allConversations.put(user.getId(), conv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.message, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        switch(id){
            case R.id.action_disconnect:
                Message msg = Message.obtain(null, MessageService.LOGOUT, new JSONObject());
                msg.replyTo = activityMessenger;

                // TODO: afficher une dialog qui demande confirmation

                try {
                    serviceMessenger.send(msg);
                } catch (RemoteException e) {
                    Log.e("EliseTalk - Disconnect", e.getMessage());
                }

                this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    public void connectAs(String username){
        Message msg;
        JSONObject data = new JSONObject();

        try {
            data.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        msg = Message.obtain(null, MessageService.LOGIN_REQUEST, data);
        msg.replyTo = activityMessenger;

        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }*/

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_message, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MessageActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
