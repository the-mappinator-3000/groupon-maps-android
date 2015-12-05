package com.themappinator.grouponcalandar.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.themappinator.grouponcalandar.fragments.BookRoomListFragment;
import com.themappinator.grouponcalandar.fragments.BrowseRoomListFragment;
import com.themappinator.grouponcalandar.fragments.RoomListFragment;

import java.util.HashMap;

;

/**
 * Created by ayegorov on 12/4/15.
 */
public class RoomListPagerAdapter extends FragmentPagerAdapter {
    private String tabTitles[] = {"Book", "Browse"};
    private HashMap<RoomListType, RoomListFragment> roomListFragments = new HashMap<>();

    public RoomListPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {
        RoomListFragment fragment = roomListFragments.get(position);
        if (fragment == null) {
            RoomListType roomListType = RoomListType.values()[position + 1];

           if (roomListType == RoomListType.Book) {
               fragment = BookRoomListFragment.newInstance();
           } else if (roomListType == RoomListType.Browse) {
               fragment = BrowseRoomListFragment.newInstance();
           }

            roomListFragments.put(roomListType, fragment);
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    public RoomListFragment getItemByRoomListType(RoomListType roomListType) {
        if (roomListType != RoomListType.Undefined) {
            return roomListFragments.get(roomListType.ordinal() - 1);
        } else {
            return null;
        }
    }

}
