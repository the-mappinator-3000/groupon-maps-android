package com.themappinator.grouponcalandar.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.themappinator.grouponcalandar.R;
import com.themappinator.grouponcalandar.adapters.RoomListAdapter;
import com.themappinator.grouponcalandar.adapters.RoomListType;
import com.themappinator.grouponcalandar.model.Room;
import com.themappinator.grouponcalandar.utils.CalendarUtils;

import java.util.ArrayList;

/**
 * Created by ayegorov on 12/4/15.
 */
public abstract class RoomListFragment extends Fragment {

    protected static final String PRETTY_SUFFIX = "_pretty";
    private static final String ROOMS_LIST_KEY = "rooms_list";

    protected RecyclerView rvRooms;
    protected RoomListAdapter aRooms;
    protected ArrayList<Room> rooms;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //
        // Rooms list
        //
        View parentView = inflater.inflate(getFragmentResourceId(), container, false);
        rvRooms = (RecyclerView) parentView.findViewById(R.id.rvRooms);
        rvRooms.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
//        rvRooms.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        aRooms = new RoomListAdapter(getActivity(), rooms, getType());
        aRooms.setRoomClickListener(getRoomClickListener());
        rvRooms.setAdapter(aRooms);

        return parentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            rooms = new ArrayList<>();
            String[] floors = getResources().getStringArray(R.array.floors);
            for (String floor : floors) {
                String[] roomIds = getResources().getStringArray(getResources().getIdentifier(floor, "array", getActivity().getApplicationInfo().packageName));
                for (String roomId : roomIds) {
                    Room room = Room.findRoomById(roomId);
                    if (room == null) {
                        String googleResource = CalendarUtils.getResourceString(roomId, getActivity());
                        String name = CalendarUtils.getResourceString(roomId + PRETTY_SUFFIX, getActivity());
                        room = new Room();
                        room.floor = floor;
                        room.roomid = roomId;
                        room.name = name;
                        room.googleResourceId = googleResource;
                    }
                    rooms.add(room);
                }
            }
        } else {
            // we are recreating the fragment so just pull the rooms from the saved state
            rooms = savedInstanceState.getParcelableArrayList(ROOMS_LIST_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ROOMS_LIST_KEY, rooms);
    }

    protected abstract RoomListType getType();

    protected abstract RoomListAdapter.RoomClickListener getRoomClickListener();

    protected abstract int getFragmentResourceId();
}
