package com.companyride.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.companyride.R;
import com.companyride.activities.MainScreen;
import com.companyride.activities.RideMainScreen;
import com.companyride.activities.RideRequestMainScreen;
import com.companyride.http.ImageLoaderTask;
import com.companyride.http.PutExecutor;
import com.companyride.interfaces.SelectViewInterface;
import com.companyride.parameters.AppSharedData;
import com.companyride.parameters.Params;
import com.companyride.utils.MyJSONArray;
import com.companyride.utils.UtilityFunctions;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserProfileFragment extends Fragment{
    private static final int MODE_STRANGER = 1;
    private static final int MODE_USER = 2;

    Button proposeRide;
    Button findRide;
    TextView fullName;
    TextView occupation;
    TextView hitcherNum;
    TextView driverNum;
    TextView ratingNum;
    ImageView profilePic;
//    LinearLayout aboutContainer;
    LinearLayout buttons;
    LinearLayout specialRequestsContainer;
    LinearLayout badgesZone;
    ListView messagesList;
    AppSharedData sharedData = AppSharedData.getInstance();
    MessageListAdaptor messageAdaptor;
    private Activity parentActivity;
    private View rootView;
    private String userProfileId;

    int mode;
    private Menu optionsMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();

        rootView = inflater.inflate(R.layout.page_user_profile, container, false);

//        initUserInfoView();
        initVariables();

        registerClickListenerOnButtons();

        loadUserProfileAndSetMode();

        setHasOptionsMenu(true);
        // Gesture detection
        //gestureDetector = new GestureDetector(this, new MyGestureDetector());
        return rootView;
    }

    @Override
    public void onResume() {
        loadUserProfileAndSetMode();
        customizeView();
        super.onResume();
    }


    private void customizeView() {
        if (mode == MODE_STRANGER)
            if (optionsMenu != null)
                optionsMenu.findItem(R.id.gotocalendar).setVisible(false);
    }


    private void loadUserProfileAndSetMode()
    {
        String path;

        Bundle bundle = getArguments();
        userProfileId = (bundle != null) ? bundle.getString(Params.extraUserProfileId): null;

        if (userProfileId != null)
        {
            path = Params.serverIP + "userProfile/short/" + userProfileId;
            mode = MODE_STRANGER;
        }
        else
        {
            userProfileId = sharedData.getUserProfileId();
            path = Params.serverIP + "userProfile/full/" + userProfileId;
            mode = MODE_USER;
            buttons.setVisibility(View.VISIBLE);
        }

        JSONObject res = UtilityFunctions.tryGetJsonFromServer(path);

        if (res != null)
        {
            JSONObject data = UtilityFunctions.tryGetJson(res, "data");
            loadUserProfileDetails(data);
        }
        else
        {
            System.out.println( "Failed to load user profile");
            UtilityFunctions.showMessageInToast(parentActivity, "Failed to load user profile");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        MenuInflater newinflater = parentActivity.getMenuInflater();
        newinflater.inflate(R.menu.user_profile_menu, menu);
        optionsMenu = menu;
        customizeView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                ((SelectViewInterface) parentActivity).goBackView();
                break;
            case R.id.gotocalendar:
                ((SelectViewInterface)parentActivity).selectView(getString(R.string.calendar));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void registerClickListenerOnButtons()
    {
        proposeRide.setOnClickListener
         (
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        try{
                            Class target = RideRequestMainScreen.class;
                            Intent intent = new Intent(parentActivity, target);
                            intent.putExtra( Params.extraRideType,  Params.extraRideTypeDriver );
                            startActivity(intent);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
        );
        findRide.setOnClickListener
        (
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        try{
                            Intent intent = new Intent(parentActivity, RideRequestMainScreen.class);
                            intent.putExtra( Params.extraRideType,  Params.extraRideTypeHitcher );
                            startActivity(intent);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    private void initVariables()
    {
        proposeRide = (Button)      rootView.findViewById(R.id.buttonProposeRide);
        findRide    = (Button)      rootView.findViewById(R.id.buttonFindRide);
        fullName    = (TextView)    rootView.findViewById(R.id.textFullName);
        occupation  = (TextView)    rootView.findViewById(R.id.textOccupation);
        hitcherNum  = (TextView)    rootView.findViewById(R.id.textHitcherNum);
        driverNum   = (TextView)    rootView.findViewById(R.id.textDriverNum);
        ratingNum   = (TextView)    rootView.findViewById(R.id.textRatingNum);
        profilePic  = (ImageView)   rootView.findViewById(R.id.imageProfile);
        specialRequestsContainer = (LinearLayout) rootView.findViewById(R.id.specialRequests);
        badgesZone = (LinearLayout) rootView.findViewById(R.id.badgesZone);
        buttons = (LinearLayout) rootView.findViewById(R.id.buttons);
        messagesList = (ListView) rootView.findViewById(R.id.listViewMessages);
    }

//    private void initUserInfoView()
//    {
//        aboutContainer = (LinearLayout) rootView.findViewById(R.id.about);
//        View view = LayoutInflater.from(parentActivity).inflate(R.layout.userinfo_part, null);
//        aboutContainer.addView(view);
//    }

    private void loadUserProfileDetails(JSONObject userProfile)
    {
        BlockedUsersFragment.saveBlockedUser(userProfile, sharedData);
        loadOccupationText(userProfile);
        loadFullNameText(userProfile);
        loadNumbers(userProfile);
        loadProfilePicture(userProfileId);
        loadSpecialRequestsPictures(userProfile);
        loadBadges(userProfile);
        loadMessages(userProfile);
    }

    private void loadMessages(JSONObject userProfile)
    {
        if(userProfile==null || !userProfile.has("messages") ){
            Log.d("loadMessages", "No messages to be loaded");
            return;
        }
        try
        {
            MyJSONArray messages = UtilityFunctions.tryGetMyJSONArray(userProfile, "messages");

            if (messages != null)
            {
                messageAdaptor = new MessageListAdaptor(parentActivity, messages);
                messagesList.setAdapter(messageAdaptor);
                messagesList.setOnItemClickListener(messagesClickListener);
            }
        }
        catch (Exception ex)
        {
            System.out.println("Failed to retrieve messages");
            ex.printStackTrace();
        }
    }

    private void loadOccupationText(JSONObject userProfile)  {
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

    private void loadSpecialRequestsPictures(JSONObject userProfile)
    {
        try
        {
           MyJSONArray reqNumbers = UtilityFunctions.tryGetMyJSONArray(userProfile, "specialRequests");
            for (int i = 0 ; i < reqNumbers.length(); i++)
            {
               ImageView newPic = new ImageView(parentActivity);
               Integer number = reqNumbers.getInt(i);
               switch (number)
               {
                   case 1: { newPic.setImageResource(R.drawable.special_req_1); break;}
                   case 2: {newPic.setImageResource(R.drawable.special_req_2);break;}
                   case 3: { newPic.setImageResource(R.drawable.special_req_3);break;}
                   case 4: { newPic.setImageResource(R.drawable.special_req_4);break;}
               }

                specialRequestsContainer.addView(newPic);
            }
        }
        catch (Exception ex)
        {
            System.out.println("Failed to add special requests pictures");
            ex.printStackTrace();
        }
    }

    private void loadBadges(JSONObject userProfile)
    {
        try
        {
            MyJSONArray badges = UtilityFunctions.tryGetMyJSONArray(userProfile, "badges");
            for (int i = 0 ; i < badges.length(); i++)
            {
                ImageView newPic = new ImageView(parentActivity);
                Integer number = badges.getInt(i);
                switch (number)
                {
                    case 1: { newPic.setImageResource(R.drawable.badge_1); break;}
                    case 2: { newPic.setImageResource(R.drawable.badge_2);break;}
                    case 5: { newPic.setImageResource(R.drawable.badge_5);break;}
                }

                badgesZone.addView(newPic);
            }
        }
        catch (Exception ex)
        {
            System.out.println("Failed to add special requests pictures");
            ex.printStackTrace();
        }
    }

    private void loadFullNameText(JSONObject userProfile) {
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

    private void loadProfilePicture(String profileId) {
        try
        {
            //String url = userProfile.getString("pictureUrl");
            String url;
            AppSharedData shrData = AppSharedData.getInstance();
            url = UtilityFunctions.buildPictureProfileUrl(profileId);

            new ImageLoaderTask(profilePic, parentActivity, profileId).execute(url);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to retrieve picture url");
            ex.printStackTrace();
        }
    }

    private void loadNumbers(JSONObject userProfile)
    {
        try {
            String numOfHitcherRides = userProfile.getString("numOfRidesAsHitcher");
            String numOfDriverRides = userProfile.getString("numOfRidesAsDriver");
            String rating = userProfile.getString("rating");

            ratingNum.setText(rating);
            hitcherNum.setText(numOfHitcherRides);
            driverNum.setText(numOfDriverRides);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to retrieve json data");
            ex.printStackTrace();
            ratingNum.setText("");
            hitcherNum.setText("");
            driverNum.setText("");
        }
    }

    private class MessageListAdaptor extends BaseAdapter{
        private Context context;
        private MyJSONArray messages;

        public MessageListAdaptor(Context context, MyJSONArray messages){
            this.context = context;
            this.messages = messages;
        }

        @Override
        public int getCount() {
            return messages.length();
        }

        @Override
        public Object getItem(int position) {
            return UtilityFunctions.tryGetJsonObjectFromArray(messages, position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public String getType(int position)
        {
            JSONObject message = (JSONObject)getItem(position);
            String type = UtilityFunctions.tryGetStringFromJson(message,"type");
            return type;
        }

        public void delete(int position)
        {
            messages.remove(position);
            notifyDataSetChanged();
        }

        public String getMessageRideId(int position)
        {
            JSONObject message = (JSONObject)getItem(position);
            return UtilityFunctions.tryGetStringFromJson(message,"rideId");
        }

        public String getMessage(int position)
        {
            JSONObject message = (JSONObject)getItem(position);
            return UtilityFunctions.tryGetStringFromJson(message,"message");
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            JSONObject message = (JSONObject)getItem(position);
            if (convertView == null)
            {
                 LayoutInflater mInflater = (LayoutInflater)
                        context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.messagelayout, null);
            }

           TextView textMessage = (TextView)convertView.findViewById(R.id.textViewMessage);

            try
            {
                String messageText = message.getString("message");
                textMessage.setText(messageText);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return convertView;
        }
    }

    private ListView.OnItemClickListener messagesClickListener = new ListView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            String message = messageAdaptor.getMessage(position);
            String rideId = messageAdaptor.getMessageRideId(position);
            String type = messageAdaptor.getType(position);
            sendRemoveMessageToServer(rideId, type, message);
            messageAdaptor.delete(position);

            switch (type)
            {
                case "thanks":
                {
                    sendThanksToServer(rideId);
                    break;
                }
                case "newRide":
                {
                    goToRideActivity(rideId);
                    break;
                }
                default:
                    loadUserProfileAndSetMode();
            }


        }
    };

    private void sendRemoveMessageToServer(String rideId,String type, String messageStr )
    {
        try
        {
            JSONObject messageToRemove = new JSONObject();
            messageToRemove.put("message", messageStr);
            messageToRemove.put("type", type);
            messageToRemove.put("rideId", rideId);

            String path = Params.serverIP + "userProfile/" + sharedData.getUserProfileId() + "/message/delete";
            JSONObject res = new PutExecutor().execute(path, messageToRemove.toString()).get();

            if (res == null)
            {
                UtilityFunctions.showMessageInToast(parentActivity,"Failed to send request to server. Try again later");
            }
            else
            {
                if (res.getInt("code") == 200 && res.getString("status").equals("success")) // logged in successfully
                {
                    System.out.println("Message was removed from server");
                }
                else
                {
                    String message = res.getString("message");
                    UtilityFunctions.showMessageInToast(parentActivity,message);
                }
            }

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void sendThanksToServer(String rideId)
    {
        try
        {
            String path = Params.serverIP +  sharedData.getUserProfileId() + "/" + rideId + "/thanks";
            JSONObject res = new PutExecutor().execute(path, "").get();

            if (res == null)
            {
                UtilityFunctions.showMessageInToast(parentActivity,"Failed to send request to server. Try again later");
            }
            else
            {
                if (res.getInt("code") == 200 && res.getString("status").equals("success")) // logged in successfully
                {
                    UtilityFunctions.showMessageInToast(parentActivity, "Thank you for thanking driver");
                }
                else
                {
                    String message = res.getString("message");
                    UtilityFunctions.showMessageInToast(parentActivity,message);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void goToRideActivity(String rideId)
    {
        try {
            Intent intent = new Intent(parentActivity, RideMainScreen.class);
            intent.putExtra(Params.extraID,  rideId );
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


