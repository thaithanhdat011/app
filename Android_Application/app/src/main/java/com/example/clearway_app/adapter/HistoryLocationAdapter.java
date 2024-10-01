package com.example.clearway_app.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clearway_app.R;
import com.example.clearway_app.model.HistoryLocationData;

import java.util.List;

public class HistoryLocationAdapter extends RecyclerView.Adapter<HistoryLocationAdapter.HistoryLocationViewHolder> {

    private List<HistoryLocationData> historyLocationList;
    private boolean isDarkMode; // Biến để lưu trạng thái Dark Mode

    public HistoryLocationAdapter(List<HistoryLocationData> historyLocationList) {
        this.historyLocationList = historyLocationList;
    }

    @NonNull
    @Override
    public HistoryLocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_location, parent, false);
        return new HistoryLocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryLocationViewHolder holder, int position) {
        HistoryLocationData historyLocation = historyLocationList.get(position);
        holder.addressTextView.setText(historyLocation.getAddress());
        holder.timeTextView.setText(historyLocation.getTime());

        // Cập nhật màu sắc dựa trên trạng thái Dark Mode
        if (isDarkMode) {
            holder.addressTextView.setTextColor(Color.WHITE);
            holder.timeTextView.setTextColor(Color.WHITE);
            holder.clockImageView.setColorFilter(Color.WHITE); // Đổi màu icon
            holder.ngangTextView.setBackgroundColor(Color.WHITE); // Đổi màu dòng kẻ ngang
        } else {
            holder.addressTextView.setTextColor(Color.BLACK);
            holder.timeTextView.setTextColor(Color.BLACK);
            holder.clockImageView.setColorFilter(null); // Trả lại màu mặc định cho icon
            holder.ngangTextView.setBackgroundColor(Color.parseColor("#dbdbde")); // Màu dòng kẻ ngang
        }
    }

    @Override
    public int getItemCount() {
        return historyLocationList.size();
    }

    // Phương thức để cập nhật giao diện dựa trên Dark Mode
    public void updateTheme(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        notifyDataSetChanged(); // Cập nhật lại toàn bộ danh sách
    }

    public static class HistoryLocationViewHolder extends RecyclerView.ViewHolder {

        TextView addressTextView;
        TextView timeTextView;
        ImageView clockImageView;
        TextView ngangTextView;

        public HistoryLocationViewHolder(@NonNull View itemView) {
            super(itemView);
            addressTextView = itemView.findViewById(R.id.address);
            timeTextView = itemView.findViewById(R.id.txt_time);
            clockImageView = itemView.findViewById(R.id.ic_clock);
            ngangTextView = itemView.findViewById(R.id.txt_Ngang);
        }
    }

    // Phương thức để cập nhật danh sách sau khi lọc
    public void filterList(List<HistoryLocationData> filteredList) {
        this.historyLocationList = filteredList;
        notifyDataSetChanged(); // Cập nhật lại RecyclerView với dữ liệu mới
    }
}
