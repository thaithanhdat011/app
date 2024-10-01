package com.example.clearway_app.fragment;

import static android.content.Context.MODE_PRIVATE;
import static java.util.Locale.filter;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clearway_app.R;
import com.example.clearway_app.adapter.HistoryLocationAdapter;
import com.example.clearway_app.databinding.FragmentStoresBinding;
import com.example.clearway_app.model.HistoryLocationData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class StoresFragment extends Fragment {

    private FragmentStoresBinding binding;
    private HistoryLocationAdapter adapter;
    private List<HistoryLocationData> historyLocationDataList;
    private boolean isSortedDescending = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout using View Binding
        binding = FragmentStoresBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Setup RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        historyLocationDataList = new ArrayList<>();
        historyLocationDataList.add(new HistoryLocationData("321 Destiny Road, Hamletville, Nationland", "22/2/2023"));
        historyLocationDataList.add(new HistoryLocationData("456 Serendipity Avenue, Townsville, Countryside", "23/2/2023"));
        historyLocationDataList.add(new HistoryLocationData("789 Fortune Street, Metrocity, Urbania", "24/2/2023"));
        historyLocationDataList.add(new HistoryLocationData("101112 Luck Boulevard, Capitol City, Cosmopolis", "25/2/2023"));
        historyLocationDataList.add(new HistoryLocationData("131415 Providence Way, Downtown, Metropolis", "26/2/2023"));

        adapter = new HistoryLocationAdapter(historyLocationDataList);
        binding.recyclerView.setAdapter(adapter);

        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text is changed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter the adapter's list based on the input
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed after text is changed
            }
        });

        // Click listener for filter icon
        binding.iconFilter.setOnClickListener(v -> {
            sortListByDate();
            adapter.notifyDataSetChanged(); // Refresh RecyclerView
        });


        // Lấy trạng thái Dark Mode từ SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);

        // Cập nhật giao diện dựa trên chế độ hiện tại
        updateTheme(isDarkMode);

        return view;
    }

    private void updateTheme(boolean isDarkMode) {
        if (isDarkMode) {
            // Đổi màu nền thành màu đen
            binding.getRoot().setBackgroundColor(Color.BLACK);

            // Đổi màu TextView text_LSDC sang màu trắng
            binding.textLSDC.setTextColor(Color.WHITE);

            // Đổi màu EditText editTextSearch sang màu đen (text và hint)
            binding.editTextSearch.setTextColor(Color.BLACK);
            binding.editTextSearch.setHintTextColor(Color.GRAY);

            // Đổi màu ImageView ic_search và iconFilter sang màu đen
            binding.icSearch.setColorFilter(Color.BLACK);
            binding.iconFilter.setColorFilter(Color.BLACK);

            // Đổi màu của RelativeLayout layout_search sang màu trắng
            binding.layoutSearch.setBackgroundColor(Color.WHITE);

            // Đổi màu nền của RecyclerView sang màu đen
            binding.recyclerView.setBackgroundColor(Color.BLACK);

            // Cập nhật màu của các thành phần trong từng item của RecyclerView
            adapter.updateTheme(isDarkMode);

        } else {
            // Trạng thái Light Mode (hoặc chế độ mặc định)

            // Đổi màu nền thành màu trắng (hoặc bất kỳ màu nền mặc định nào)
            binding.getRoot().setBackgroundColor(Color.WHITE);

            // Đổi màu TextView text_LSDC trở lại màu đen
            binding.textLSDC.setTextColor(Color.BLACK);

            // Đổi màu EditText editTextSearch trở lại màu đen (text) và xám (hint)
            binding.editTextSearch.setTextColor(Color.BLACK);
            binding.editTextSearch.setHintTextColor(Color.GRAY);

            // Đổi màu ImageView ic_search và iconFilter trở lại màu ban đầu
            binding.icSearch.setColorFilter(null); // Bỏ bộ lọc màu (trở lại màu gốc)
            binding.iconFilter.setColorFilter(null); // Bỏ bộ lọc màu

            // Đổi màu của RelativeLayout layout_search trở lại màu ban đầu
            binding.layoutSearch.setBackgroundResource(R.drawable.selected_item_background);

            // Đổi màu nền của RecyclerView trở lại màu trắng (hoặc bất kỳ màu nền mặc định nào)
            binding.recyclerView.setBackgroundColor(Color.WHITE);

            // Cập nhật màu của các thành phần trong từng item của RecyclerView
            adapter.updateTheme(isDarkMode);
        }
    }


    private void sortListByDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Collections.sort(historyLocationDataList, new Comparator<HistoryLocationData>() {
            @Override
            public int compare(HistoryLocationData o1, HistoryLocationData o2) {
                try {
                    Date date1 = dateFormat.parse(o1.getTime());
                    Date date2 = dateFormat.parse(o2.getTime());

                    // Sắp xếp theo trạng thái isSortedDescending
                    if (isSortedDescending) {
                        return date2.compareTo(date1); // Giảm dần (ngày gần nhất trước)
                    } else {
                        return date1.compareTo(date2); // Tăng dần (ngày xa nhất trước)
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });

        // Đổi trạng thái sắp xếp sau mỗi lần nhấn
        isSortedDescending = !isSortedDescending;
    }
    private void filter(String text) {
        List<HistoryLocationData> filteredList = new ArrayList<>();
        for (HistoryLocationData item : historyLocationDataList ) {
            if (item.getAddress().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.filterList(filteredList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
