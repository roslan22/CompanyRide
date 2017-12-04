package com.companyride.fragments.inital;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.companyride.R;
import com.companyride.http.FilePostExecutor;
import com.companyride.http.GetExecutor;
import com.companyride.http.ImageLoaderTask;
import com.companyride.http.PutExecutor;
import com.companyride.interfaces.SelectViewInterface;
import com.companyride.parameters.AppSharedData;
import com.companyride.parameters.Params;
import com.companyride.utils.UtilityFunctions;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class AccountFragment extends Fragment{
    private static final String LAST_PATH = "LAST_PATH";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_CROP = 2;
    private static final int REQUEST_IMAGE_GALLERY = 3;
    private AppCompatActivity parentActivity;
    private View rootView;

    private TextView fullName;
    private TextView occupation;
    private ImageView profilePic;
    private Button  savePicBtn;
    private LinearLayout aboutContainer;
    private TextView tvMessageUser;
    private JSONObject userDataJSON;
    private String mLastTakenPhotoPath;
    private String status;
    private Menu actionBarMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (AppCompatActivity) getActivity();
        parentActivity.setTitle(getString(R.string.account));
        rootView = inflater.inflate(R.layout.page_account, container, false);

        initVariables();
        retrieveUserMessage();
        loadUserDetails();
        setHasOptionsMenu(true);
        // Customize the action bar
        ActionBar actionBar = parentActivity.getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        getUserInfoFromServerAndLoad();
        super.onAttach(activity);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
            mLastTakenPhotoPath = savedInstanceState.getString(LAST_PATH);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(LAST_PATH, mLastTakenPhotoPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.account_menu, menu);
        actionBarMenu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        customizeUserInfoView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                parentActivity.onBackPressed();
                break;
            case R.id.editMenuItem:
                goToEditUserDetailsScreen();
                break;
            case R.id.chgPicMenuItem:
                startGalleryActivity();
                break;
            case R.id.chgPicCamMenuItem:
                startCameraActivity();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void showPicButton() {
        savePicBtn.setVisibility(View.VISIBLE);
    }

    private void startCameraActivity() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(parentActivity.getPackageManager()) != null) {
            // Create the File where the photo should go
            String imageName = null;
            try {
                imageName = userDataJSON.getString("userProfileId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            File imageFile=null;
            try {
                imageFile = ImageLoaderTask.createImageFile(imageName, parentActivity);
                mLastTakenPhotoPath =  imageFile.getAbsolutePath();
            } catch (IOException e) {
                UtilityFunctions.showMessageInToast(parentActivity, "Failed to create an image file in external storage!");
                e.printStackTrace();
                return;
            }

            // Continue only if the File was successfully created
            if (imageFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(imageFile));

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void startGalleryActivity() {
        Intent choosePictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        choosePictureIntent.setType("image/*");
        // Create the File where the photo should go
        String imageName = null;
        try {
            imageName = userDataJSON.getString("userProfileId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        File imageFile=null;
        try {
            imageFile = ImageLoaderTask.createImageFile(imageName, parentActivity);
            mLastTakenPhotoPath =  imageFile.getAbsolutePath();
        } catch (IOException e) {
            UtilityFunctions.showMessageInToast(parentActivity, "Failed to create an image file in external storage!");
            e.printStackTrace();
            return;
        }

        // Continue only if the File was successfully created
        if (imageFile != null) {
            choosePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(imageFile));
            startActivityForResult(Intent.createChooser(choosePictureIntent,
                    "Complete action using"), REQUEST_IMAGE_GALLERY);
        }
    }

    private void startImageCropActivity(Uri picUri) {
        //take care of exceptions
        try {
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            cropIntent.putExtra("scale", true);
            //retrieve data on return
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
            cropIntent.putExtra("output", picUri);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
        }
        //respond to users whose devices do not support the crop action
        catch(ActivityNotFoundException e){
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            UtilityFunctions.showMessageInToast(parentActivity, errorMessage);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == parentActivity.RESULT_OK) {
            // reload picture
            if (mLastTakenPhotoPath == null)
                return;
            startImageCropActivity(Uri.fromFile(new File(mLastTakenPhotoPath)));
        }
        if (requestCode == REQUEST_IMAGE_CROP && resultCode == parentActivity.RESULT_OK) {
            // reload picture
            profilePic.setImageBitmap(ImageLoaderTask.getScaledBitmapFromFile(mLastTakenPhotoPath, profilePic));
            showPicButton();
        }
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == parentActivity.RESULT_OK) {
            File ourFile = new File(mLastTakenPhotoPath);
            try {
                copyFile(new File(getRealPathFromURI(data.getData())), ourFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // reload picture
            startImageCropActivity(Uri.fromFile(ourFile));
        }
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }

        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }


    }

    private String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = parentActivity.getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    private void goToEditUserDetailsScreen() {
        Bundle args = new Bundle();
        args.putString(Params.argsJSON, userDataJSON.toString());
        ((SelectViewInterface) parentActivity).selectView(getString(R.string.sign_up), args);
    }

    private void getUserInfoFromServerAndLoad()
    {
        try
        {
            String path = Params.serverIP + "user/" + AppSharedData.getInstance().getUserId();
            JSONObject res = new GetExecutor().execute(path).get();

            if (res == null)
            {
                printMessageInLabel("Failed to send request to server. Try again later");
            }
            else
            {
                if (res.getInt("code") == 200 && res.getString("status").equals("success")) // logged in successfully
                {
                    userDataJSON = res.getJSONObject("data");
                    loadUserDetails();
                }
                else
                {
                    String message = res.getString("message");
                    printMessageInLabel(message);
                }
            }
        }
        catch (Exception ex)
        {
            System.out.println("Unexpected exception: ");
            ex.printStackTrace();
        }
    }

    private void printMessageInLabel(String message)
    {
        tvMessageUser.setText(message);
    }

    private void retrieveUserMessage()
    {
        Bundle args = getArguments();
        if (args != null) {
            String message = args.getString(Params.argsMessage);
            if (message != null)
                printMessageInLabel(message);
        }
    }

    private void initVariables()
    {
        mLastTakenPhotoPath = null;
        fullName = (TextView)     rootView.findViewById(R.id.textFullName);
        occupation = (TextView)   rootView.findViewById(R.id.textOccupation);
        profilePic = (ImageView)  rootView.findViewById(R.id.imageProfile);
        tvMessageUser = (TextView)rootView.findViewById(R.id.tvMessageUser);
        savePicBtn    =  (Button) rootView.findViewById(R.id.updatePic);
        savePicBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendPictureToServer();
                    }
                }
        );
    }

    private void sendPictureToServer() {
        String path = Params.serverIP + "user/picture/";
        try {
            path = path + userDataJSON.getString("_id");
//            For later encoding of the picture
//            String base64Pic = encodePicture(mLastTakenPhotoPath);
            try {
//                String jsonString = "{\"picture\" : \"" + base64Pic + "\"}";
//                Log.d("Some", jsonString);
                JSONObject res = new FilePostExecutor().execute(path, mLastTakenPhotoPath).get();
                if (res == null)
                {
                    UtilityFunctions.showMessageInToast(parentActivity, "Failed to contact server!");
                }
                else
                {
                    if (res.getInt("code") == 200 && res.getString("status").equals("success")) // logged in successfully
                    {
                        UtilityFunctions.showMessageInToast(parentActivity,"Picture saved on server!");
                        hidePicButton();
                    }
                    else
                    {
                        String message = res.getString("message");
                        UtilityFunctions.showMessageInToast(parentActivity, message);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            //todo: handle exception
            UtilityFunctions.showMessageInToast(parentActivity,"Error in sending picture to server");
            e.printStackTrace();
        }
    }

    private void hidePicButton() {
        savePicBtn.setVisibility(View.GONE);
    }

    private String encodePicture(String fileName) {
        byte[] bytes = ImageLoaderTask.convertFileImageToByteArray(fileName);
        String encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);
        return encodedString.replace("\n", "");
    }

    private void customizeUserInfoView()
    {
        aboutContainer = (LinearLayout) rootView.findViewById(R.id.userInfoContainer);
        status = UtilityFunctions.tryGetStringFromJson(userDataJSON, "status");
        if(status.equals(Params.STATUS_PASSIVE)){
            MenuItem item = actionBarMenu.findItem(R.id.chgPicCamMenuItem);
            item.setVisible(false);
            item = actionBarMenu.findItem(R.id.chgPicMenuItem);
            item.setVisible(false);
        }
    }

    private void loadUserDetails()
    {
        loadOccupationText();
        loadFullNameText();
        loadProfilePicture();
    }

    private void loadOccupationText()  {
        StringBuilder occupationSb = new StringBuilder();
        String occupationStr = UtilityFunctions.tryGetStringFromJson(userDataJSON, "occupation");
        String companyName = UtilityFunctions.tryGetStringFromJson(userDataJSON, "companyName");

        if (occupationStr != null) {
            occupationSb.append(occupationStr);
            occupationSb.append(" at ");
        }
        if (companyName != null)
            occupationSb.append(companyName);

        if (occupationSb.length() > 0)
            occupation.setText(occupationSb.toString());
    }

    private void loadFullNameText() {
        StringBuilder nameSb = new StringBuilder();
        String firstName = UtilityFunctions.tryGetStringFromJson(userDataJSON, "firstName");
        String lastName = UtilityFunctions.tryGetStringFromJson(userDataJSON, "lastName");

        if( firstName != null) {
            nameSb.append(firstName);
            nameSb.append(" ");
        }
        if (lastName != null)
            nameSb.append(lastName);

        if (nameSb.length() > 0)
            fullName.setText(nameSb.toString());
    }

    private void loadProfilePicture() {
        String id  = UtilityFunctions.tryGetStringFromJson(userDataJSON, "userProfileId");
        if( id != null) {
            String url = UtilityFunctions.buildPictureProfileUrl(id);
            new ImageLoaderTask(profilePic, parentActivity, id).execute(url);
        }
    }

}
