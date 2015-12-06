package com.themappinator.grouponcalandar.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.themappinator.grouponcalandar.R;
import com.themappinator.grouponcalandar.activities.MapActivity;
import com.themappinator.grouponcalandar.adapters.RoomListAdapter;
import com.themappinator.grouponcalandar.adapters.RoomListType;
import com.themappinator.grouponcalandar.model.Room;
import com.themappinator.grouponcalandar.utils.CalendarUtils;

import java.util.Collections;
import java.util.Comparator;

/**
 * Created by ayegorov on 12/4/15.
 */
public class BrowseRoomListFragment extends RoomListFragment {

    private final static String EXTRA_SUFFIX = "_extra";

    public static BrowseRoomListFragment newInstance() {
        Bundle args = new Bundle();
        args.putInt("Type", RoomListType.Browse.ordinal());
        BrowseRoomListFragment fragment = new BrowseRoomListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = super.onCreateView(inflater, container, savedInstanceState);
        return parentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] floors = getResources().getStringArray(R.array.floors);
        for (String floor : floors) {
            int resourceId = getResources().getIdentifier(floor + EXTRA_SUFFIX, "array", getActivity().getApplicationInfo().packageName);
            if (resourceId > 0) {
                String[] roomIds = getResources().getStringArray(resourceId);
                for (String roomId : roomIds) {
                    String googleResource = CalendarUtils.getResourceString(roomId, getActivity());
                    String name = CalendarUtils.getResourceString(roomId + PRETTY_SUFFIX, getActivity());
                    Room room = new Room();
                    room.floor = floor;
                    room.id = roomId;
                    room.name = name;
                    room.googleResourceId = googleResource;
                    rooms.add(room);
                }
            }
        }

        // Sort all rooms alphabetically by name
        Collections.sort(rooms, new Comparator<Room>() {
            @Override
            public int compare(Room r1, Room r2) {
                return r1.name.compareToIgnoreCase(r2.name);
            }
        });
    }

    @Override
    protected RoomListType getType() {
        return RoomListType.Browse;
    }

    @Override
    protected RoomListAdapter.RoomClickListener getRoomClickListener() {
        return new RoomListAdapter.RoomClickListener() {
            @Override
            public void onRoomClick(int position) {
                Room room = rooms.get(position);
                Context context = getContext();
                Intent intent = new Intent(context, MapActivity.class);
                intent.putExtra(Room.TAG, room);
                context.startActivity(intent);
            }
        };
    }

    @Override
    protected int getFragmentResourceId() {
        return R.layout.fragment_browse_room_list;
    }
}
