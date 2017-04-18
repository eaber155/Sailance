package com.evivian.admin.sailance;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import activity.OpenClubAccount;
import clubactivity.AboutUsActivity;
import clubactivity.BoatHandicapActivity;
import clubactivity.NewRaceActivity;
import clubactivity.PersonalHandicapsActivity;
import clubactivity.SettingsActivity;
import clubactivity.ViewPreviousRaces;
import fragment.AddAdminFragment;
import fragment.AddNewMembersFragment;
import fragment.MemberDetailsFragment;
import helper.SQLiteHandler;
import helper.SessionManager;

public class ClubAccountHome extends AppCompatActivity implements AddNewMembersFragment.OnFragmentInteractionListener, MemberDetailsFragment.OnFragmentInteractionListener,
        AddAdminFragment.OnFragmentInteractionListener{
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private ImageView imgNavHeaderBg, imgProfile;
    private TextView getClubName, getClubHandle;
    private Toolbar toolbar;
    private FloatingActionButton fab;

    // urls to load navigation header background image
    // and profile image
    private static final String urlNavHeaderBg = "http://api.androidhive.info/images/nav-menu-header-bg.jpg";
    private static final String urlProfileImg = "https://lh3.googleusercontent.com/eCtE_G34M9ygdkmOpYvCag1vBARCmZwnVS6rS5t4JLzJ6QgQSBquM0nuTsCpLhYbKljoyS-txg";

    // index to identify current nav menu item
    public static int navItemIndex = 0;

    // tags used to attach the fragments
    private static final String TAG_NEW_MEMBERS = "add_new_members";
    private static final String TAG_MEMBER_DETAILS = "manage_member_details";
    private static final String TAG_ADD_ADMIN = "add_admin";
    public static String CURRENT_TAG = TAG_NEW_MEMBERS;

    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;

    @Override
    public void onFragmentInteraction(Uri uri){

    }

    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;

    private SQLiteHandler db;
    String clubName, clubHandle;
    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_account_home);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
        getClubName = (TextView) navHeader.findViewById(R.id.name);
        getClubHandle = (TextView) navHeader.findViewById(R.id.clubHandleHolder);
        imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
        imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

        db = new SQLiteHandler(getApplicationContext());

        HashMap<String, String> clubInformation = db.getClubDetails();
        clubName = clubInformation.get("club_name");
        clubHandle = clubInformation.get("club_handle");

        //db.onUpgrade(openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null), 1, 2);

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();**/
                Intent intent = new Intent(ClubAccountHome.this, NewRaceActivity.class);
                startActivity(intent);
            }
        });

        // load nav menu header data
        loadNavHeader();

        // initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_NEW_MEMBERS;
            loadHomeFragment();
        }

        db = new SQLiteHandler(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());


        if(!sessionManager.isLoggedIn()){
            LogoutClub();
        }
    }

    public void LogoutClub(){
        sessionManager.setLogin(false);
        db.deleteClubs();

        Intent intent = new Intent(ClubAccountHome.this, OpenClubAccount.class);
        startActivity(intent);
    }

    /***
     * Load navigation menu header information
     * like background image, profile image
     * name, website, notifications action view (dot)
     */
    private void loadNavHeader() {
        // name, website
        getClubName.setText(clubName);
        getClubHandle.setText(clubHandle);

        imgNavHeaderBg.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.nav_header_background_two));
        imgProfile.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.nav_profile_two));


        /**loading header background image
         Glide.with(this).load(urlNavHeaderBg)
         .crossFade()
         .diskCacheStrategy(DiskCacheStrategy.ALL)
         .into(imgNavHeaderBg);

         // Loading profile image
         Glide.with(this).load(urlProfileImg)
         .crossFade()
         .thumbnail(0.5f)
         .bitmapTransform(new CircleTransform(this))
         .diskCacheStrategy(DiskCacheStrategy.ALL)
         .into(imgProfile);

         //showing dot next to notifications label
         navigationView.getMenu().getItem(3).setActionView(R.layout.menu_dot);**/
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();

            // show or hide the fab button
            toggleFab();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        // show or hide the fab button
        toggleFab();

        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
                // add  member
                return new AddNewMembersFragment();
            case 1:
                // manage members
                return new MemberDetailsFragment();
            case 2:
                // add admin
                return new AddAdminFragment();
            default:
                return new AddNewMembersFragment();
        }
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_add_new_members:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_NEW_MEMBERS;
                        break;
                    case R.id.nav_manage_member_details:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_MEMBER_DETAILS;
                        break;
                    case R.id.nav_add_admin:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_ADD_ADMIN;
                        break;
                    case R.id.view_personal_handicaps:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(ClubAccountHome.this, PersonalHandicapsActivity.class));
                        drawer.closeDrawers();
                        return true;
                    case R.id.view_boat_handicap:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(ClubAccountHome.this, BoatHandicapActivity.class));
                        drawer.closeDrawers();
                        return true;
                    case R.id.view_previous_races:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(ClubAccountHome.this, ViewPreviousRaces.class));
                        drawer.closeDrawers();
                        return true;
                    case R.id.start_new_race:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(ClubAccountHome.this, NewRaceActivity.class));
                        drawer.closeDrawers();
                        return true;
                    case R.id.settings:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(ClubAccountHome.this, SettingsActivity.class));
                        drawer.closeDrawers();
                        return true;
                    case R.id.about_us:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(ClubAccountHome.this, AboutUsActivity.class));
                        drawer.closeDrawers();
                        return true;
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.addDrawerListener(actionBarDrawerToggle);

        //drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than the firsy
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_NEW_MEMBERS;
                loadHomeFragment();
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // show menu only when home fragment is selected
        if (navItemIndex == 0) {
            getMenuInflater().inflate(R.menu.club, menu);
        }

        /** when fragment is notifications, load the menu created for notifications
        if (navItemIndex == 3) {
            getMenuInflater().inflate(R.menu.personalhandicaps, menu);
        }**/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            LogoutClub();
            Toast.makeText(getApplicationContext(), "Logged Out!", Toast.LENGTH_LONG).show();
            return true;
        }

        // download personal handicaps
        if (id == R.id.action_download_record) {
            Toast.makeText(getApplicationContext(), "Download Spreadsheet!", Toast.LENGTH_LONG).show();
        }

        // add handicaps for guest sailors
        if (id == R.id.action_add_new_handicap) {
            Toast.makeText(getApplicationContext(), "Add Guest Personal Handicap!", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    // show or hide the fab
    /**private void toggleFab() {
        if (navItemIndex == 0)
            fab.show();
        else if (navItemIndex==1){
            fab.show();
        }
        else
            fab.hide();
    }**/

    private void toggleFab() {
        if (navItemIndex == 0)
            fab.show();
        else
            fab.hide();
    }
}
