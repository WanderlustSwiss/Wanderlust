package eu.wise_iot.wanderlust.views;

import android.Manifest;
import android.accounts.NetworkErrorException;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import eu.wise_iot.wanderlust.R;
import eu.wise_iot.wanderlust.constants.Constants;
import eu.wise_iot.wanderlust.models.DatabaseModel.LoginUser;
import eu.wise_iot.wanderlust.models.DatabaseModel.MyObjectBox;
import eu.wise_iot.wanderlust.models.DatabaseModel.Poi;
import eu.wise_iot.wanderlust.models.DatabaseModel.User;
import eu.wise_iot.wanderlust.models.DatabaseObject.PoiDao;
import eu.wise_iot.wanderlust.models.DatabaseObject.UserDao;
import eu.wise_iot.wanderlust.services.AddCookiesInterceptor;
import eu.wise_iot.wanderlust.services.LoginService;
import eu.wise_iot.wanderlust.services.ReceivedCookiesInterceptor;
import eu.wise_iot.wanderlust.services.ServiceGenerator;
import eu.wise_iot.wanderlust.services.UserService;
import io.objectbox.BoxStore;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * MainActivity:
 * @author Fabian Schwander
 * @license MIT
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private String[] mMenuItems;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    public static BoxStore boxStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);
        setupNavigation();
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);


        //TODO where to put this?
        boxStore = MyObjectBox.builder().androidContext(getApplicationContext()).build();

        // check if app is opened for the first time
        if (preferences.getBoolean("firstTimeOpened", true) || true) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("firstTimeOpened", false); // save that app has been opened
            editor.apply();

            ProfileFragment profileFragment = new ProfileFragment();
            getFragmentManager().beginTransaction()
                                .add(R.id.content_frame, profileFragment)
                                .commit();
/*
            // start welcome screen
            RegistrationFragment welcomeFragment = new RegistrationFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.content_frame, welcomeFragment)
                    .commit();*/
            // else start the map screen
        } else {
            MapFragment mapFragment = MapFragment.newInstance();
            getFragmentManager().beginTransaction()
                    .add(R.id.content_frame, mapFragment, Constants.MAP_FRAGMENT)
                    .commit();
        }
        //login();
    }


    //TODO move to login view
    private void login(){
        LoginService loginService = ServiceGenerator.createService(LoginService.class);

        LoginUser testUser = new LoginUser("zumsel128", "Ha11loW3lt");
        Call<LoginUser> call = loginService.basicLogin(testUser);
        call.enqueue(new Callback<LoginUser>() {
            @Override
            public void onResponse(Call<LoginUser> call, Response<LoginUser> response) {
                if (response.isSuccessful()) {

                    Headers headerResponse = response.headers();
                    //convert header to Map
                    Map<String, List<String>> headerMapList = headerResponse.toMultimap();
                    LoginUser.setCookies((ArrayList<String>) headerMapList.get("Set-Cookie"));
                    Toast.makeText(MainActivity.this, "Cookie saved!", Toast.LENGTH_SHORT).show();

                    testCookieAuth();
                } else {
                    Toast.makeText(MainActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginUser> call, Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.v(TAG, t.getMessage());
            }
        });
    }

    //TODO move to login view
    private void testCookieAuth(){
        LoginService loginService = ServiceGenerator.createService(LoginService.class);
        Call<LoginUser> cookieCall = loginService.cookieTest();
        cookieCall.enqueue(new Callback<LoginUser>() {
            @Override
            public void onResponse(Call<LoginUser> call, Response<LoginUser> response) {
                Toast.makeText(MainActivity.this, "cookie auth: " + response.message(), Toast.LENGTH_SHORT).show();
                testSavePoi();
            }

            @Override
            public void onFailure(Call<LoginUser> call, Throwable t) {
                Toast.makeText(MainActivity.this, "cookie auth failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void testSavePoi(){
        Poi testPoi = new Poi(0, "testPoi", "des", "path/whatever",
                5.2f, 6.2f, 6, 5, false);
        PoiDao testPoiDao = new PoiDao(boxStore, MainActivity.this);
        testPoiDao.create(testPoi);

//        UserDao testUserDao = new UserDao(boxStore);
//        User testUser = new User(0, "derp", "pipu@popo.miau", "secret",
//                1, false, false, "lastLogin", "acc type");
//        testUserDao.update(testUser, MainActivity.this);

    }

    public void makeToast(String s){
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= 23) checkPermissions();
    }

    private void setupNavigation() {
        mDrawerList = (ListView) findViewById(R.id.navDrawerList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        addDrawerItems();
        setupDrawer();
    }

    private void addDrawerItems() {
        mMenuItems = getResources().getStringArray(R.array.drawer_menu_items);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mMenuItems);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void selectItem(int position) {
        Fragment fragment = null;
        String fragmentTag = null;

        switch (position) {
            case 0:
                fragment = MapFragment.newInstance();
                fragmentTag = Constants.MAP_FRAGMENT;
                break;
//            case 1: // FIXME: UNCOMMENTED FOR RELEASE 0.1
//                fragment = SearchFragment.newInstance();
//                break;
            case 1:
                fragment = new ManualFragment();
                fragmentTag = Constants.MANUAL_FRAGMENT;
                break;
            case 2:
                fragment = new DisclaimerFragment();
                fragmentTag = Constants.DISCLAIMER_FRAGMENT;
                break;
            default:
                Toast.makeText(this, R.string.msg_page_not_existing, Toast.LENGTH_SHORT).show();
        }
        if (fragment != null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment, fragmentTag)
                    .addToBackStack(null)
                    .commit();
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(mMenuItems[position]);

            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            Log.e(TAG, "Error in creating fragment");
        }
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                   // host Activity
                mDrawerLayout,          // DrawerLayout object
                R.string.drawer_open,
                R.string.drawer_close
        ) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true); // has to be false when adding custom drawer menu icon

        //TODO: Change drawer menu icon to custom icon
//        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.drawer_menu_icon, this.getTheme());
//        mDrawerToggle.setHomeAsUpIndicator(drawable);
//        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
//                    mDrawerLayout.closeDrawer(GravityCompat.START);
//                } else {
//                    mDrawerLayout.openDrawer(GravityCompat.START);
//                }
//            }
//        });
        mDrawerLayout.addDrawerListener(mDrawerToggle);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions() {
        List<String> permissionsList = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (!permissionsList.isEmpty()) {
            String[] params = permissionsList.toArray(new String[permissionsList.size()]);
            requestPermissions(params, Constants.REQUEST_FOR_MULTIPLE_PERMISSIONS);
        }
    }

    @Override
    public void onBackPressed() {
        // preventing that activity gets destroyed when back button is pressed on empty back stack
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            moveTaskToBack(true);
        } else super.onBackPressed();
    }
}
