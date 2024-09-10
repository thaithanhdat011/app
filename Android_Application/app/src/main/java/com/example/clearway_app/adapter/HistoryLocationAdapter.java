package com.example.clearway_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clearway_app.R;
import com.example.clearway_app.model.HistoryLocationData;
import java.util.ArrayList;
import java.util.List;

public class HistoryLocationAdapter extends RecyclerView.Adapter<HistoryLocationAdapter.HistoryLocationViewHolder> {

    private List<HistoryLocationData> historyLocationList;
    private List<HistoryLocationData> historyLocationListFull;


    public HistoryLocationAdapter(List<HistoryLocationData> historyLocationList) {
        this.historyLocationList = historyLocationList;
        this.historyLocationListFull = new ArrayList<>(historyLocationList);
    }

    @NonNull
    @Override
    public HistoryLocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list__address, parent, false);
        return new HistoryLocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryLocationViewHolder holder, int position) {
        HistoryLocationData historyLocation = historyLocationList.get(position);
        if (historyLocation == null) {
            return;
        }
        holder.addressTextView.setText(historyLocation.getAddress());
        holder.timeTextView.setText(historyLocation.getTime());
    }

    @Override
    public int getItemCount() {
        return historyLocationList.size();
    }
    public void filter(String query) {
        if (query.isEmpty()) {
            historyLocationList.clear();
            historyLocationList.addAll(historyLocationListFull); // Reset to full list
        } else {
            List<HistoryLocationData> filteredList = new ArrayList<>();
            for (HistoryLocationData data : historyLocationListFull) {
                if (data.getAddress().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(data);
                }
            }
            historyLocationList.clear();
            historyLocationList.addAll(filteredList); // Update with the filtered list
        }
        notifyDataSetChanged();
    }

    public static class HistoryLocationViewHolder extends RecyclerView.ViewHolder {

        TextView addressTextView;
        TextView timeTextView;
        ImageView clockImageView;

        public HistoryLocationViewHolder(@NonNull View itemView) {
            super(itemView);
            addressTextView = itemView.findViewById(R.id.tv_address);
            timeTextView = itemView.findViewById(R.id.txt_time);
            clockImageView = itemView.findViewById(R.id.ic_clock);
        }
    }
}

