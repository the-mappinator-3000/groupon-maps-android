package com.themappinator.grouponcalandar.utils;

import android.content.Context;

import com.themappinator.grouponcalandar.R;
import com.themappinator.grouponcalandar.adapters.SimpleSectionedRecyclerViewAdapter;
import com.themappinator.grouponcalandar.model.Room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ayegorov on 12/7/15.
 */
public class RoomSectionPresenter {

    private HashMap<String, ArrayList<Room> > roomsByFloor = new HashMap<>();
    private String[] floorIds = null;
    private HashMap<String, String> floorsByName = new HashMap<>();

    public RoomSectionPresenter(Context context, ArrayList<Room> rooms) {

        for (Room room : rooms) {
            ArrayList<Room> visibleRooms = roomsByFloor.get(room.floor);
            if (visibleRooms == null) {
                visibleRooms = new ArrayList<>();
                roomsByFloor.put(room.floor, visibleRooms);
            }
            visibleRooms.add(room);
        }

        floorIds = context.getResources().getStringArray(R.array.floors);
        String[] floorNames = context.getResources().getStringArray(R.array.floors_pretty);
        for (int i = 0; i < floorNames.length; ++i) {
            floorsByName.put(floorIds[i], floorNames[i]);
        }
    }

    public List<SimpleSectionedRecyclerViewAdapter.Section> getSections() {
        // This is the code to provide a sectioned list
        List<SimpleSectionedRecyclerViewAdapter.Section> sections =
                new ArrayList<SimpleSectionedRecyclerViewAdapter.Section>();

        // Sections
        int sectionFirstPosition = 0;
        for (int i = 0; i < floorIds.length; ++i) {
            String floorId = floorIds[i];
            ArrayList<Room> rooms = roomsByFloor.get(floorId);
            if (rooms.size() > 0) {
                sections.add(new SimpleSectionedRecyclerViewAdapter.Section(sectionFirstPosition, floorsByName.get(floorId)));
                sectionFirstPosition += roomsByFloor.get(floorId).size();
            }
        }

        return sections;
    }
}
