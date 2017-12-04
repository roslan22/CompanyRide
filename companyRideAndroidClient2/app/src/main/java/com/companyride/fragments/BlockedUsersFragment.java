package com.companyride.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.companyride.R;
import com.companyride.http.GetExecutor;
import com.companyride.http.ImageLoaderTask;
import com.companyride.http.PutExecutor;
import com.companyride.interfaces.SelectViewInterface;
import com.companyride.parameters.AppSharedData;
import com.companyride.parameters.Params;
import com.companyride.utils.MyJSONArray;
import com.companyride.utils.UtilityFunctions;
import org.json.JSONObject;
import java.util.ArrayList;

public class BlockedUsersFragment extends Fragment
{
    BlockedUsersListAdaptor blockedUsersAdaptor;
    private Button buttonSubmitUnblockUsers;
    private ListView listViewBlockedUsers;
    private Activity parentActivity;
    private TextView textViewMessage;
    private View rootView;
    private AppSharedData sharedData = AppSharedData.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();
        rootView = inflater.inflate(R.layout.page_blocked_users, container, false);

        initVariables();
        loadBlockedUsers();
        registerClickListenerOnButtons();

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.calendar_menu, menu);
    }

    private void loadUserProfile()
    {
        String path = Params.serverIP + "userProfile/full/" + sharedData.getUserProfileId();

        JSONObject res = UtilityFunctions.tryGetJsonFromServer(path);

        if (res != null)
        {
            JSONObject data = UtilityFunctions.tryGetJson(res, "data");
            // save blocked users in shared data
            BlockedUsersFragment.saveBlockedUser(data, sharedData);
        }
        else
        {
            System.out.println( "Failed to load user profile");
            UtilityFunctions.showMessageInToast(parentActivity, "Failed to load user profile");
        }
    }

    static public void saveBlockedUser(JSONObject userProfile, AppSharedData sharedData)
    {
        try {
            ArrayList<String> listdata = new ArrayList<>();
            MyJSONArray jArray = UtilityFunctions.tryGetMyJSONArray(userProfile, "blockedUsers");
            if (jArray != null)
            {
                for (int i = 0; i < jArray.length(); i++)
                {
                    listdata.add(jArray.get(i).toString());
                }

                sharedData.setBlockedUsers(listdata);
                sharedData.setBlockedUsersAsMyJSONArray(jArray);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                parentActivity.onBackPressed();
                break;
            case R.id.goto_user_profile:
                ((SelectViewInterface)parentActivity).selectView(getString(R.string.user_profile));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void registerClickListenerOnButtons()
    {
        buttonSubmitUnblockUsers.setOnClickListener
        (new View.OnClickListener()
         {
             @Override
             public void onClick(View view)
             {
                 ArrayList <String> blockedUsers = blockedUsersAdaptor.getBlockedUsers();
                 String path = Params.serverIP + "userProfile/" + sharedData.getUserProfileId() + "/unblock/";
                 if (blockedUsers.size() > 0)
                 {
                     for(int i = 0; i < blockedUsers.size(); i++)
                     {
                         JSONObject res = null;
                         try
                         {
                             res = new PutExecutor().execute(path + blockedUsers.get(i), new JSONObject().toString()).get();

                             if (res == null)
                             {
                                 UtilityFunctions.showMessageInToast(parentActivity.getBaseContext(), "Failed to send request to server. Try again later");
                             } else {
                                 if (res.getInt("code") == 200 && res.getString("status").equals("success")) // logged in successfully
                                 {
                                     String message = res.getString("message");
                                      UtilityFunctions.showMessageInToast(parentActivity.getBaseContext(), message);
                                     loadUserProfile();
                                     loadBlockedUsers();
                                 }
                                 else
                                 {
                                     String message = res.getString("message");
                                     System.out.println("Failed to send unblock to server of " +  blockedUsers.get(i) + message);
                                     UtilityFunctions.showMessageInToast(parentActivity, "Failed to send unblock to server: " + message);
                                 }
                             }
                         }
                         catch (Exception e)
                         {
                             e.printStackTrace();
                         }
                     }
                 }

             }
         });
    }

    private void initVariables()
    {
        buttonSubmitUnblockUsers = (Button)     rootView.findViewById(R.id.buttonSubmitUnblockUsers);
        listViewBlockedUsers = (ListView)   rootView.findViewById(R.id.listViewBlockedUsers);
        textViewMessage = (TextView)        rootView.findViewById(R.id.textViewMessage);
    }


    private void loadBlockedUsers()
    {
        try
        {
            ArrayList blockedUsers = sharedData.getBlockedUsers();

            if (blockedUsers != null)
            {
                if ((blockedUsers.size() == 1 && blockedUsers.get(0).equals(sharedData.getUserProfileId())) ||
                        (blockedUsers.size() == 0))
                {
                    textViewMessage.setText("You have no blocked users");
                    textViewMessage.setVisibility(View.VISIBLE);
                    blockedUsersAdaptor.updateUsersList(blockedUsers);
                    buttonSubmitUnblockUsers.setEnabled(false);
                    buttonSubmitUnblockUsers.setVisibility(View.GONE);
                }
                else
                {
                    if (blockedUsersAdaptor == null){
                        blockedUsersAdaptor = new BlockedUsersListAdaptor(parentActivity, blockedUsers);
                        listViewBlockedUsers.setAdapter(blockedUsersAdaptor);
                        listViewBlockedUsers.setOnItemClickListener(blockeUsersClickListener);
                    }
                    else
                        blockedUsersAdaptor.updateUsersList(blockedUsers);
                  }
            }
        }
        catch (Exception ex)
        {
            System.out.println("Failed to retrieve messages");
            ex.printStackTrace();
        }
    }

    private class BlockedUsersListAdaptor extends BaseAdapter {
        private Context context;
        private ArrayList<String> users;
        private MyJSONArray usersJSON = new MyJSONArray();
        private ArrayList<Boolean>  selectedArray = new ArrayList<>();

        public BlockedUsersListAdaptor(Context context, ArrayList<String> users) {
            this.context = context;
            this.users = users;
            loadUsersJSONs();
        }

        public void updateUsersList(ArrayList<String> users){
            this.users = users;
            loadUsersJSONs();
            this.notifyDataSetInvalidated();
        }

        public ArrayList<String> getBlockedUsers()
        {
            ArrayList<String> blockedUsers = new ArrayList<String> ();
            for (int i=0; i< users.size(); i++)
            {
                if (selectedArray.get(i) == true)
                {
                    blockedUsers.add(users.get(i));
                }
            }

            return blockedUsers;
        }

        private void loadUsersJSONs()
        {
            for (int i = 0; i < users.size(); i++)
            {
                loadJSONFromServerForId(users.get(i));
                selectedArray.add(false);
            }

            for (int i = 0; i < users.size(); i++)
            {
               if (users.get(i).equals(sharedData.getUserProfileId()))
               {
                   users.remove(i);
                   break;
               }
            }
        }

        public void setSelection(int position, boolean selection)
        {
            selectedArray.set(position, selection);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int position) {
            return UtilityFunctions.tryGetJsonObjectFromArray(usersJSON, position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        public String getUserProfileId(int position) {
            return users.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            JSONObject user = (JSONObject) getItem(position);
            String id = getUserProfileId(position);

            if (convertView == null)
            {
                LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.userinfo_part, null);
            }

            TextView fullName = (TextView) convertView.findViewById(R.id.textFullName);
            TextView occupation = (TextView) convertView.findViewById(R.id.textOccupation);
            ImageView profilePic = (ImageView) convertView.findViewById(R.id.imageProfile);

            try
            {
                loadOccupationText(user, occupation);
                loadFullNameText(user, fullName);
                loadProfilePicture(user, profilePic);

                if( selectedArray.get(position))
                {
                    convertView.setBackgroundColor(Color.LTGRAY);
                } else {
                    convertView.setBackgroundColor(Color.TRANSPARENT);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return convertView;
        }

        private void loadOccupationText(JSONObject userProfile, TextView occupation)  {
            try
            {
                occupation.setText(userProfile.getString("occupationTitle"));
            }
            catch (Exception ex)
            {
                System.out.println("Failed to retrieve occupation title");
                ex.printStackTrace();
                occupation.setText("");
            }
        }

        private void loadFullNameText(JSONObject userProfile, TextView fullName) {
            try
            {
                fullName.setText(userProfile.getString("fullName"));
            }
            catch (Exception ex)
            {
                System.out.println("Failed to retrieve full name");
                ex.printStackTrace();
                fullName.setText("");
            }
        }

        private void loadProfilePicture(JSONObject userProfile,ImageView profilePic) {
            try
            {
                String id  = userProfile.getString("_id");
                String url = UtilityFunctions.buildPictureProfileUrl(id);
                new ImageLoaderTask(profilePic, parentActivity, id).execute(url);
            }
            catch (Exception ex)
            {
                System.out.println("Failed to retrieve picture url");
                ex.printStackTrace();
            }
        }

        private void loadJSONFromServerForId(String userProfileId)
        {
            try
            {
                if (!sharedData.getUserProfileId().equals(userProfileId))
                {
                    String path = Params.serverIP + "userProfile/minimal/" + userProfileId;
                    JSONObject res = new GetExecutor().execute(path).get();

                    if (res == null) {
                        System.out.println("Failed to load ");
                        usersJSON.put(new JSONObject("{}"));
                    } else {
                        if (res.getInt("code") == 200 && res.getString("status").equals("success")) // logged in successfully
                        {
                            JSONObject data = UtilityFunctions.tryGetJson(res, "data");
                            usersJSON.put(data);
                        } else {
                            usersJSON.put(new JSONObject("{}"));
                            String message = res.getString("message");
                            System.out.println("Failed to load user profile id" + userProfileId + " message " + message);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private ListView.OnItemClickListener blockeUsersClickListener = new ListView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            view.setSelected(true);
            blockedUsersAdaptor.setSelection(position,listViewBlockedUsers.getCheckedItemPositions().get(position) );
            if (listViewBlockedUsers.getCheckedItemCount() > 0)
            {
                buttonSubmitUnblockUsers.setEnabled(true);
                buttonSubmitUnblockUsers.setVisibility(View.VISIBLE);
            }
            else{
                buttonSubmitUnblockUsers.setEnabled(false);
                buttonSubmitUnblockUsers.setVisibility(View.GONE);
            }

            rootView.invalidate();
        }
    };



}



