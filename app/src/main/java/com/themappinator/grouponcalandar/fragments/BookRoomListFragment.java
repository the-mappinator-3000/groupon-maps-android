package com.themappinator.grouponcalandar.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.themappinator.grouponcalandar.R;
import com.themappinator.grouponcalandar.adapters.RoomListAdapter;
import com.themappinator.grouponcalandar.adapters.RoomListType;
import com.themappinator.grouponcalandar.adapters.SimpleSectionedRecyclerViewAdapter;
import com.themappinator.grouponcalandar.model.Room;
import com.themappinator.grouponcalandar.network.GoogleCalendarApiClient;
import com.themappinator.grouponcalandar.utils.CalendarUtils;
import com.themappinator.grouponcalandar.utils.GooglePlayServicesManager;
import com.themappinator.grouponcalandar.utils.RoomSectionPresenter;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by ayegorov on 12/4/15.
 */
public class BookRoomListFragment extends RoomListFragment {

    private static final int MEETING_TIME_IN_MINUTES = 45;
    private static final int TIME_PERIOD_INTERVAL_IN_HOURS = 8;
    private static final String SELECTED_START_DATE = "selectedStartDate";
    private static final String SELECTED_END_DATE = "selectedEndDate";
    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";

    enum RefreshMode {
        None,
        Progress,
        PullToRefresh
    };

    private ProgressDialog mProgress;
    private GoogleCalendarApiClient client;

    private RefreshMode refreshMode = RefreshMode.None;

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView timeTextView;
    private RangeSeekBar<Long> rangeSeekBar;

    private Date selectedStartDate;
    private Date selectedEndDate;
    private Date startDate;
    private Date endDate;

    private SimpleSectionedRecyclerViewAdapter mSectionedAdapter;
    private RoomSectionPresenter roomSectionPresenter;

    private Long previousMinValue; // Save here to check if we dragging the min value


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
        //mProgress.setTitle(getResources().getString(R.string.loading_title));
        mProgress.setMessage(getResources().getString(R.string.loading_message));

        client = GoogleCalendarApiClient.getInstance();

        if (savedInstanceState == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(System.currentTimeMillis()));
            calendar.add(Calendar.MINUTE, MEETING_TIME_IN_MINUTES);
            updateTimeIntervalWithStartTime(System.currentTimeMillis(), calendar.getTime().getTime());

            startDate = selectedStartDate;
            calendar.setTime(startDate);
            calendar.add(Calendar.HOUR, TIME_PERIOD_INTERVAL_IN_HOURS);
            endDate = calendar.getTime();
        } else {
            selectedStartDate = (Date) savedInstanceState.getSerializable(SELECTED_START_DATE);
            selectedEndDate = (Date) savedInstanceState.getSerializable(SELECTED_END_DATE);
            startDate = (Date) savedInstanceState.getSerializable(START_DATE);
            endDate = (Date) savedInstanceState.getSerializable(END_DATE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SELECTED_START_DATE, selectedStartDate);
        outState.putSerializable(SELECTED_END_DATE, selectedEndDate);
        outState.putSerializable(START_DATE, startDate);
        outState.putSerializable(END_DATE, endDate);
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
                Room room = rooms.get(mSectionedAdapter.sectionedPositionToPosition(position));
                FragmentManager fragmentManager = getFragmentManager();
                BookEventDialog dialog = BookEventDialog.newInstance(room, selectedStartDate, selectedEndDate);
                dialog.show(fragmentManager, "book_dialog");
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

        // Update room's adapter with start and end dates
        aRooms.setTimePeriod(selectedStartDate, selectedEndDate);

        //
        // Define sections
        //

        roomSectionPresenter = new RoomSectionPresenter(getContext(), rooms);

        // Add your adapter to the sectionAdapter
        mSectionedAdapter = new
                SimpleSectionedRecyclerViewAdapter(getContext(), R.layout.floor_section, R.id.section_text, aRooms);

        updateSections();

        // Apply this adapter to the RecyclerView
        rvRooms.setAdapter(mSectionedAdapter);


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

        // Setup the new range seek bar
        rangeSeekBar = (RangeSeekBar<Long>)parentView.findViewById(R.id.timeSeekBar);
        // Set the range
        rangeSeekBar.setRangeValues(startDate.getTime(), endDate.getTime());
        rangeSeekBar.setNotifyWhileDragging(true);

        rangeSeekBar.setSelectedMinValue(selectedStartDate.getTime());
        rangeSeekBar.setSelectedMaxValue(selectedEndDate.getTime());

        timeTextView = (TextView)parentView.findViewById(R.id.timeTextView);

        updateTimeTextView();

        previousMinValue = rangeSeekBar.getSelectedMinValue();

        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Long>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Long minValue, Long maxValue) {
                adjustMinMaxValueFor(minValue, maxValue);
                updateTimeIntervalWithStartTime(minValue, maxValue);
                updateTimeTextView();
            }
        });

        return parentView;
    }

    private void adjustMinMaxValueFor(Long minValue, Long maxValue) {
        Long delta = minValue - previousMinValue;
        previousMinValue = minValue;

        if (delta != 0) {
            rangeSeekBar.setSelectedMaxValue(maxValue + delta);
        }
    }

    private void updateTimeIntervalWithStartTime(Long startTime, Long endTime) {
        selectedStartDate = new Date(startTime);
        selectedStartDate = CalendarUtils.trimSeconds(selectedStartDate);
        selectedEndDate = new Date(endTime);
        selectedEndDate = CalendarUtils.trimSeconds(selectedEndDate);

        if (aRooms != null) {
            aRooms.setTimePeriod(selectedStartDate, selectedEndDate);
        }
    }

    private void updateSections() {
        List<SimpleSectionedRecyclerViewAdapter.Section> sections = roomSectionPresenter.getSections();
        SimpleSectionedRecyclerViewAdapter.Section[] dummy = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        mSectionedAdapter.setSections(sections.toArray(dummy));
    }

    private void updateTimeTextView() {
        String dateRangeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(selectedStartDate);
        dateRangeText += " - ";
        dateRangeText += DateFormat.getTimeInstance(DateFormat.SHORT).format(selectedEndDate);

        timeTextView.setText(dateRangeText);
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
                // save to database
                for (Room room : rooms) {
                    room.save();
                }
            }
        }

        @Override
        protected void onCancelled() {
            hideProgress();
            GooglePlayServicesManager.instance().handleCancelException(mLastError);
        }
    }
}
