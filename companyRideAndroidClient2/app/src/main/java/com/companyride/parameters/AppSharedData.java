package com.companyride.parameters;

import android.app.Application;
import com.companyride.utils.MyJSONArray;

import java.util.ArrayList;

/**
 * Created by Ruslan on 30-Mar-15.
 * Singelton
 */
public class AppSharedData
{
    private static String userId;
    public String getUserId() {return userId;}
    public void setUserId(String id)  {this.userId = id; }
    private static String userProfileId;
    public String getUserProfileId() {return userProfileId;}
    public void setUserProfileId(String id)  {this.userProfileId = id; }
    public ArrayList<String> blockedUsers;
    public MyJSONArray blockedUsersAsMyJSONArray;
    public ArrayList<String>  getBlockedUsers() {return blockedUsers;}
    public void setBlockedUsers(ArrayList<String>  blockedUsers)  {this.blockedUsers = blockedUsers; }
    public void setBlockedUsersAsMyJSONArray(MyJSONArray  blockedUsersAsMyJSONArray)  {this.blockedUsersAsMyJSONArray = blockedUsersAsMyJSONArray; }
    public MyJSONArray getBlockedUsersAsMyJSONArray() {return blockedUsersAsMyJSONArray;}


    private AppSharedData(){}

    public static AppSharedData getInstance()
    {
        if (instance == null)
        {
            instance = new AppSharedData();
        }
        return  instance;
    }

    private static AppSharedData instance = null;

}
