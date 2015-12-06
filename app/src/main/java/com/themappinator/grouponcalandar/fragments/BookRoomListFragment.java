package com.themappinator.grouponcalandar.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.themappinator.grouponcalandar.R;
import com.themappinator.grouponcalandar.activities.BookEventActivity;
import com.themappinator.grouponcalandar.adapters.RoomListAdapter;
import com.themappinator.grouponcalandar.adapters.RoomListType;
import com.themappinator.grouponcalandar.model.Room;
import com.themappinator.grouponcalandar.network.GoogleCalendarApiClient;
import com.themappinator.grouponcalandar.ui.MainUITraits;
import com.themappinator.grouponcalandar.utils.GooglePlayServicesManager;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by ayegorov on 12/4/15.
 */
public class BookRoomListFragment extends RoomListFragment {

    enum RefreshMode {
        None,
        Progress,
        PullToRefresh
    };

    private ProgressDialog mProgress;
    private GoogleCalendarApiClient client;

    private RefreshMode refreshMode = RefreshMode.None;

    private SwipeRefreshLayout swipeRefreshLayout;


    public static BookRoomListFragment newInstance() {
        Bundle args = new Bundle();
        args.putInt("Type", RoomListType.Book.ordinal());
        BookRoomListFragment fragment = new BookRoomListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgress = new ProgressDialog(getActivity());

        client = GoogleCalendarApiClient.getInstance();
    }

    @Override
    protected RoomListType getType() {
        return RoomListType.Book;
    }

    @Override
    protected RoomListAdapter.RoomClickListener getRoomClickListener() {
        return new RoomListAdapter.RoomClickListener() {
            @Override
            public void onRoomClick(int position) {
                Room room = rooms.get(position);
                Context context = getContext();
                Intent i = new Intent(context, BookEventActivity.class);
                // prevent back returning to try and book the room again
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.putExtra(Room.TAG, room);
                context.startActivity(i);
            }
        };
    }

    @Override
    protected int getFragmentResourceId() {
        return R.layout.fragment_book_room_list;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = super.onCreateView(inflater, container, savedInstanceState);

        //
        // Swipe to refresh
        //
        swipeRefreshLayout = (SwipeRefreshLayout) parentView.findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshResultsInMode(RefreshMode.PullToRefresh);
            }
        });

        // Configure the refreshing colors
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        if (savedInstanceState == null) {
            refreshResultsInMode(RefreshMode.Progress);
        }

        //
        // Range slider
        //

        long now = System.currentTimeMillis();
        Date minDate = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(minDate);
        calendar.add(Calendar.HOUR, 8);
        Date maxDate = calendar.getTime();

        calendar.setTime(minDate);
        calendar.add(Calendar.MINUTE, 45);
        Date selectedMaxDate = calendar.getTime();

        // Setup the new range seek bar
        RangeSeekBar<Long> rangeSeekBar = new RangeSeekBar<Long>(getContext());
        // Set the range
        rangeSeekBar.setRangeValues(minDate.getTime(), maxDate.getTime());
        rangeSeekBar.setTextAboveThumbsColor(MainUITraits.MAIN_THEME_COLOR);

        rangeSeekBar.setSelectedMinValue(minDate.getTime());
        rangeSeekBar.setSelectedMaxValue(selectedMaxDate.getTime());

        // Add to layout
        LinearLayout layout = (LinearLayout) parentView.findViewById(R.id.range_slider_placeholder);
        layout.addView(rangeSeekBar);


        return parentView;
    }

    /**
     * Attempt to get a set of data from the Google Calendar API to display. If the
     * email address isn't known yet, then call chooseAccount() method so the
     * user can pick an account.
     */
    private void refreshResultsInMode(RefreshMode refreshMode) {

        if (GooglePlayServicesManager.instance().isGooglePlayServicesAvailable()) {

            this.refreshMode = refreshMode;

            if (!client.isAccountSelected()) {
                GooglePlayServicesManager.instance().chooseAccount();
            } else {
                if (isDeviceOnline()) {
                    MakeRequestTask requestTask = new MakeRequestTask(client);

                    requestTask.execute(rooms.toArray(new Room[rooms.size()]));
                } else {
                    Log.d("room list fragment", "No network connection available.");
                }
            }
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void showProgress() {
        if (refreshMode == RefreshMode.Progress) {
            mProgress.show();
        }
    }

    private void hideProgress() {
        if (refreshMode == RefreshMode.Progress) {
            mProgress.hide();
        } else if (refreshMode == RefreshMode.PullToRefresh) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Room, Void, List<Room>> {
        GoogleCalendarApiClient client;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleCalendarApiClient client) {
            this.client = client;
        }

        /**
         * Background task to call Google Calendar API.
         */
        @Override
        protected List<Room> doInBackground(Room... params) {
            try {
                return client.getFreeBusy(Arrays.asList(params));
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            Log.d("request task", "No network connection available.");
            showProgress();
        }

        @Override
        protected void onPostExecute(List<Room> output) {
            hideProgress();
            if (output == null || output.size() == 0) {
                Log.d("request task", "No results returned.");
            } else {
                aRooms.notifyDataSetChanged();
            }
        }

        @Override
        protected void onCancelled() {
            hideProgress();
            GooglePlayServicesManager.instance().handleCancelException(mLastError);
        }
    }
}
