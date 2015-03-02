package elise.rokuan.com.elisetalk.data;

import elise.rokuan.com.elisetalk.R;

/**
 * Class to store a contact's name or status who is identified by an unique ID
 */
public class User {
    private Long id;
    private String name;
    private int status;

    public static final int STATUS_ONLINE = 0;
    public static final int STATUS_BUSY = 1;
    public static final int STATUS_ABSENT = 2;

    public static final int[] statusDrawables = new int[]{
            R.drawable.status_online,
            R.drawable.status_busy,
            R.drawable.status_absent
    };

    public User(long userId, String userName){
        id = userId;
        name = userName;
        status = STATUS_ONLINE;
    }

    public User(long userId, String userName, String strStatus){
        id = userId;
        name = userName;
        status = statusFromString(strStatus);
    }

    public static int statusFromString(String strStatus){
        if(strStatus.equals("ONLINE")){
            return STATUS_ONLINE;
        } else if(strStatus.equals("BUSY")){
            return STATUS_BUSY;
        } else if(strStatus.equals("ABSENT")){
            return STATUS_ABSENT;
        }

        return STATUS_ONLINE;
    }

    public static String statusToString(int status){
        switch(status){
            case STATUS_ONLINE:
                return "ONLINE";
            case STATUS_BUSY:
                return "BUSY";
            case STATUS_ABSENT:
            default:
                return "ABSENT";
        }
    }

    @Override
    public boolean equals(Object o){
        return (o == this) || ((o instanceof User) && (((User)o).id == this.id));
    }

    public Long getId(){
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setStatus(String status){
        this.status = statusFromString(status);
    }
}
