package com.themappinator.grouponcalandar.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.themappinator.grouponcalandar.R;
import com.themappinator.grouponcalandar.model.Room;
import com.themappinator.grouponcalandar.utils.CalendarUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.ViewHolder> {

    private List<Room> rooms;
    private Context context;
    private final RoomListType roomListType;
    private static Map<RoomListType, RoomClickListener> listenerByType = new HashMap<>();

    public interface RoomClickListener {
        void onRoomClick(int position);
    }

    public RoomListAdapter(Context context, List<Room> rooms, RoomListType roomListType) {
        this.context = context;
        this.rooms = rooms;
        this.roomListType = roomListType;
    }

    /**
     * Sets the listener associated with the rooms list type that is associated with this adapter
     */
    public void setRoomClickListener(RoomClickListener listener) {
        listenerByType.put(roomListType, listener);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_room, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView, roomListType);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Room room = rooms.get(position);
        // only check for busy if it is not a browse context
        if (roomListType == RoomListType.Browse || !room.isBusy(new DateTime(System.currentTimeMillis()))) {
            holder.tvRoomName.setVisibility(View.VISIBLE);
        } else {
            holder.tvRoomName.setVisibility(View.GONE);
        }
        String title = CalendarUtils.getResourceString(room.floor, context) + " " + room.name;
        if (!room.name.isEmpty()) {
            holder.tvRoomName.setText(title);
        } else {
            Log.e("RLA", "room:" + room.roomid + " is mis- configured");
            holder.tvRoomName.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvRoomName;
        private RoomListType roomListType;
        public ViewHolder(View itemView, RoomListType type) {
            super(itemView);
            roomListType = type;
            tvRoomName = (TextView) itemView.findViewById(R.id.tvRoomName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listenerByType.get(roomListType).onRoomClick(getLayoutPosition());
                }
            });
        }
    }
}
