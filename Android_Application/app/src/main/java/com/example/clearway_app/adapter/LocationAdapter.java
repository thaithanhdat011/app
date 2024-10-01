package com.example.clearway_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clearway_app.R;
import com.example.clearway_app.model.Address;

import java.util.ArrayList;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
    private ArrayList<Address> searches;
    private RecyclerViewClickListener listener;

    public LocationAdapter(ArrayList<Address> searches, RecyclerViewClickListener listener) {
        this.searches = searches;
        this.listener = listener;
    }

    public class LocationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView nameTxt;
        private TextView addressTxt;

        LocationViewHolder(final View view) {
            super(view);
            nameTxt = view.findViewById(R.id.location_name);
            addressTxt = view.findViewById(R.id.location_address);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onClick(view, getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false);
        return new LocationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Address address = searches.get(position);
        holder.nameTxt.setText(address.getName());
        holder.addressTxt.setText(address.getFormatted_address());
    }

    @Override
    public int getItemCount() {
        return searches.size();
    }

    public interface RecyclerViewClickListener {
        void onClick(View view, int position);
    }
}
