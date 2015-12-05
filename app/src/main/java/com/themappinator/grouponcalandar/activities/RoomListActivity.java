package com.themappinator.grouponcalandar.activities;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.astuetz.PagerSlidingTabStrip;
import com.themappinator.grouponcalandar.R;
import com.themappinator.grouponcalandar.adapters.RoomListPagerAdapter;
import com.themappinator.grouponcalandar.network.GoogleCalendarApiClient;
import com.themappinator.grouponcalandar.utils.GooglePlayServicesManager;

public class RoomListActivity extends AppCompatActivity {
    private static final String PREF_ACCOUNT_NAME = "accountName";

    private GoogleCalendarApiClient client;
    private RoomListPagerAdapter roomListPagerAdapter;

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        client = GoogleCalendarApiClient.getInstance(getApplicationContext(),
                settings.getString(PREF_ACCOUNT_NAME, null));

        ViewPager viewPager = (ViewPager)findViewById(R.id.viewpager);
        roomListPagerAdapter = new RoomListPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(roomListPagerAdapter);

        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip)findViewById(R.id.tabs);
        tabStrip.setViewPager(viewPager);

        GooglePlayServicesManager.instance().setClient(client);
        GooglePlayServicesManager.instance().setActivity(this);
    }


    /**
     * Called whenever this activity is pushed to the foreground, such as after
     * a call to onCreate().
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case GooglePlayServicesManager.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    GooglePlayServicesManager.instance().isGooglePlayServicesAvailable();
                }
                break;
            case GooglePlayServicesManager.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        client.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    //mOutputText.setText("Account unspecified.");
                }
                break;
            case GooglePlayServicesManager.REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    GooglePlayServicesManager.instance().chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}