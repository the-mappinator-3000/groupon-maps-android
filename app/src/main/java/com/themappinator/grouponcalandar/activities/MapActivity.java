package com.themappinator.grouponcalandar.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.themappinator.grouponcalandar.R;
import com.themappinator.grouponcalandar.model.Room;
import com.themappinator.grouponcalandar.ui.MapImageView;

public class MapActivity extends AppCompatActivity {

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private Room selectedRoom;


    private MapImageView mapImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        selectedRoom = intent.getParcelableExtra(Room.TAG);

        setTitle(selectedRoom.name);

        mapImageView = (MapImageView)findViewById(R.id.mapImageView);
        mapImageView.selectRoom(selectedRoom);
    }
}
