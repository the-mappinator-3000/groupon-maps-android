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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.ViewHolder> {

    private List<Room> rooms;
    private Context context;
    private final RoomListType roomListType;
    private static Map<RoomListType, RoomClickListener> listenerByType = new HashMap<>();
    private Date startDate;
    private Date endDate;

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

    public void setTimePeriod(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;

        notifyDataSetChanged();
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
        DateTime startTime = null;
        if (startDate != null) {
            startTime = new DateTime(startDate);
        }
        DateTime endTime = null;
        if (endDate != null) {
            endTime = new DateTime(endDate);
        }

        if (roomListType == RoomListType.Browse || !room.isBusy(startTime, endTime)) {
            holder.tvRoomName.setVisibility(View.VISIBLE);
        } else {
            holder.tvRoomName.setVisibility(View.GONE);
        }
        String title = CalendarUtils.getResourceString(room.floor, context) + " " + room.name;
        assert(!room.name.isEmpty());
        if (room.name.isEmpty()) {
            Log.e("Debug", "room with id '" + room.roomid + "' is empty");
        }

        holder.tvRoomName.setText(title);
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
