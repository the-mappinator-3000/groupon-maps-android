package com.themappinator.grouponcalandar.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.themappinator.grouponcalandar.R;
import com.themappinator.grouponcalandar.activities.MapActivity;
import com.themappinator.grouponcalandar.model.Room;

import java.util.List;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.ViewHolder> {

    private List<Room> rooms;
    Context context;

    public RoomListAdapter(Context context, List<Room> rooms) {
        this.context = context;
        this.rooms = rooms;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_room, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Room room = rooms.get(position);
        DateTime now = new DateTime(System.currentTimeMillis());
        if (room.isBusy(now)) {
            holder.tvRoomName.setVisibility(View.GONE);
        } else {
            holder.tvRoomName.setVisibility(View.VISIBLE);
        }
        holder.tvRoomName.setText(room.id);
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvRoomName;
        public ViewHolder(View itemView) {
            super(itemView);
            tvRoomName = (TextView) itemView.findViewById(R.id.tvRoomName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RoomListAdapter.this.context, MapActivity.class);
                    Room room = RoomListAdapter.this.rooms.get(getLayoutPosition());
                    intent.putExtra("room", room);
                    context.startActivity(intent);
                }
            });
        }
    }
}
