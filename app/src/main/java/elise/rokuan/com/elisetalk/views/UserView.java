package elise.rokuan.com.elisetalk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import elise.rokuan.com.elisetalk.R;
import elise.rokuan.com.elisetalk.data.User;

/**
 * Created by LEBEAU Christophe on 04/02/2015.
 */
public class UserView extends LinearLayout {
    private User user;
    private ImageView statusImage;
    private TextView userNameView;

    public UserView(Context context, User u) {
        super(context);
        user = u;
        initUserView();
    }

    private void initUserView(){
        if(user != null){
            LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.view_user, this);

            statusImage = (ImageView)this.findViewById(R.id.view_user_status);
            userNameView = (TextView)this.findViewById(R.id.view_user_name);

            try {
                statusImage.setImageResource(User.statusDrawables[user.getStatus()]);
            }catch(Exception e){
                // TOCHECK:
            }
            userNameView.setText(user.getName());
        }
    }
}
