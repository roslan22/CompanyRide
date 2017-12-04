package com.companyride.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.companyride.R;
import com.companyride.fragments.AboutFragment;
import com.companyride.fragments.BlockedUsersFragment;
import com.companyride.fragments.ExitConfirmationDialog;
import com.companyride.fragments.FeedbackFragment;
import com.companyride.fragments.UserProfileFragment;
import com.companyride.fragments.calendar.CalendarFragment;
import com.companyride.fragments.inital.AccountFragment;
import com.companyride.fragments.inital.SignUpFragment;
import com.companyride.interfaces.SelectViewInterface;
import com.companyride.services.notifications.RegistrationIntentService;
import com.companyride.parameters.AppSharedData;
import com.companyride.parameters.Params;
import com.companyride.utils.UtilityFunctions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

public class MainScreen extends AppCompatActivity implements SelectViewInterface {
    //    private GestureDetector gestureDetector;
//    private String[] mDrawerMenuTitles;
    private DrawerLayout mDrawerLayout;
//    private ListView mDrawerList;
//    private DrawerItemsListAdaptor mDrawerListAdaptor;
//    private TypedArray mNavMenuIcons;
    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // bypass login if previously logged in
       loginPreferences = getSharedPreferences(Params.PREF_FILE, MODE_PRIVATE);
       loginPrefsEditor = loginPreferences.edit();

        Intent intent = getIntent();
        if (intent.getBooleanExtra(getString(R.string.exit), false)) {
            logoutApp(true);
            return;
        }
        if (intent.getBooleanExtra(getString(R.string.logout), false)) {
            logoutApp(false);
            return;
        }

        setContentView(R.layout.main_screen);

        mTitle = getTitle();

        initDrawerLayout();
        if (savedInstanceState == null) {
            // on first time display view for first nav item
            selectView(getResources().getString(R.string.user_profile));
        }
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "onMenuOpened...unable to set icons for overflow menu", e);
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mTitle.equals(getString(R.string.user_profile)))
            exitApp();
        else
            goBackView();
    }

    public void goBackView(){
        FragmentManager.BackStackEntry backStackEntry = getFragmentManager().getBackStackEntryAt(0);
        getFragmentManager().popBackStack();
        //setTitle(backStackEntry.getName());
        selectView(backStackEntry.getName());
    }

    public void exitApp() {
        //show confirmation dialog
        ExitConfirmationDialog dialog = ExitConfirmationDialog.getInstance(true);
        dialog.show(getFragmentManager(), getString(R.string.exit_confirmation));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        enableNavigation();

        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

    private void disableNavigation(){
        mDrawerToggle.setDrawerIndicatorEnabled(false);
    }

    private void enableNavigation(){
        mDrawerToggle.setDrawerIndicatorEnabled(true);
    }

    private void initDrawerLayout() {
//        mDrawerMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
//        mNavMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_drawer) ;

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                selectView(menuItem.getTitle().toString());
                return false;
            }

        });

//        mDrawerList = (ListView) findViewById(R.id.left_drawer);

//        ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<NavDrawerItem>();
//        navDrawerItems.add(new NavDrawerItem(getString(R.string.navigation), -1));
//        for(int i=0; i < mDrawerMenuTitles.length; i++) {
//            String title =  mDrawerMenuTitles[i];
//            if( title.equals(getResources().getString(R.string.account) )){
//                navDrawerItems.add(new NavDrawerItem(getString(R.string.settings), -1));
//            }
//            else if ( title.equals(getResources().getString(R.string.feedback) )){
//                navDrawerItems.add(new NavDrawerItem(getString(R.string.other), -1));
//            }
//            else if( title.equals(getResources().getString(R.string.logout) )){
//                navDrawerItems.add(new NavDrawerItem("", -1));
//            }
//            navDrawerItems.add(new NavDrawerItem(title, mNavMenuIcons.getResourceId(i,-1)));
//        }
//
//        // Recycle the typed array
//        mNavMenuIcons.recycle();

//        mDrawerListAdaptor = new DrawerItemsListAdaptor(getSupportActionBar().getThemedContext(), navDrawerItems);
        // Set the adapter for the list view
//        mDrawerList.setAdapter(mDrawerListAdaptor);
        // Set the list's click listener
//        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ){
            public void onDrawerClosed(View view) {
                //getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

//    private class DrawerItemClickListener implements ListView.OnItemClickListener {
//        @Override
//        public void onItemClick(AdapterView parent, View view, int position, long id) {
//            selectView(((NavDrawerItem)mDrawerListAdaptor.getItem(position)).getTitle());
//        }
//    }

    /** Swaps fragments in the main content view */
    public void selectView(String viewName, Bundle args) {
        Fragment fragment = null;
        getSupportActionBar().setCustomView(null);
        enableNavigation();
        if( viewName.equals(getResources().getString(R.string.user_profile))) {
            fragment = new UserProfileFragment();
        }
        else if (viewName.equals(getResources().getString(R.string.calendar))) {
            fragment = new CalendarFragment();
        }
        else if (viewName.equals(getResources().getString(R.string.account))) {
            fragment = new AccountFragment();
        }
        else if (viewName.equals(getResources().getString(R.string.feedback))) {
            fragment = new FeedbackFragment();
        }
        else if (viewName.equals(getResources().getString(R.string.about))) {
            fragment = new AboutFragment();
        }
        else if (viewName.equals(getResources().getString(R.string.blockedUsers))) {
            fragment = new BlockedUsersFragment();
        }
        // Logout pressed
        else if (viewName.equals(getResources().getString(R.string.logout))) {
            logoutApp(false);
            return;
        }
        else if (viewName.equals(getResources().getString(R.string.sign_up))) {
            disableNavigation();
            fragment = new SignUpFragment();
        }
        else{
            return;
        }

        if(args != null) fragment.setArguments(args);
        // Insert the fragment by replacing any existing fragment
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment);
        if(!viewName.equals(getString(R.string.login))){
            transaction.addToBackStack(viewName);
        }
        UtilityFunctions.removeSoftInputFromCurrentView(this);
        transaction.commit();

        // Highlight the selected item, update the title, and close the drawer
//        mDrawerList.setItemChecked(Arrays.asList(mDrawerMenuTitles).indexOf(viewName), true);
//        mDrawerLayout.closeDrawer(mDrawerList);
        setTitle(viewName);
    }

    private void logoutApp(boolean exit) {
        if (exit)
            loginPrefsEditor.putBoolean(Params.SAVE_LOGIN, true);
        else
            loginPrefsEditor.putBoolean(Params.SAVE_LOGIN, false);
//        loginPrefsEditor.remove(Params.USER_ID);
        loginPrefsEditor.commit();
        AppSharedData.getInstance().setUserId(null);
        AppSharedData.getInstance().setUserProfileId(null);
        Intent intent = new Intent(getApplicationContext(), InitialScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if(exit) intent.putExtra(getString(R.string.exit), true);
        finish();
        startActivity(intent);
        registerToUsersTopics();
    }

    private void registerToUsersTopics() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        this.startService(intent);
    }

    public void selectView(String viewName){
        selectView(viewName, null);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    private class NavDrawerItem{
        private String title;
        private int icon;

        public NavDrawerItem(String title, int icon){
            this.title = title;
            this.icon  = icon;
        }

        public int getIcon() {
            return icon;
        }

        public String getTitle() {
            return title;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean isSeparator(){
            return (icon == -1);
        }
    }

    private class DrawerItemsListAdaptor extends BaseAdapter{
        private Context context;
        private ArrayList<NavDrawerItem> navDrawerItems;

        public DrawerItemsListAdaptor(Context context, ArrayList<NavDrawerItem> navDrawerItems){
            this.context = context;
            this.navDrawerItems = navDrawerItems;
        }

        @Override
        public int getCount() {
            return navDrawerItems.size();
        }

        @Override
        public Object getItem(int position) {
            return navDrawerItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NavDrawerItem item = navDrawerItems.get(position);
            if (convertView == null) {
                LayoutInflater mInflater = (LayoutInflater)
                        context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                if( item.isSeparator())
                    convertView = mInflater.inflate(R.layout.divider_with_title, null);
                else
                    convertView = mInflater.inflate(R.layout.drawer_item, null);
            }
            // it is a separator
            if( item.isSeparator()){
                TextView separator = (TextView) convertView.findViewById(R.id.divider);
                separator.setText(item.getTitle());
            }
            // it is a simple item
            else {
                ImageView imgIcon = (ImageView) convertView.findViewById(R.id.drawer_item_icon);
                TextView txtTitle = (TextView) convertView.findViewById(R.id.drawer_item_title);
                imgIcon.setImageResource(item.getIcon());
                txtTitle.setText(item.getTitle());
            }

            return convertView;
        }

    }
}
