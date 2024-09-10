package com.example.clearway_app.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clearway_app.R;
import com.example.clearway_app.adapter.HistoryLocationAdapter;
import com.example.clearway_app.databinding.FragmentStoresBinding;
import com.example.clearway_app.model.HistoryLocationData;
import java.util.ArrayList;
import java.util.List;

public class StoresFragment extends Fragment {

    private FragmentStoresBinding binding;
    private HistoryLocationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout using View Binding
        binding = FragmentStoresBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Setup RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<HistoryLocationData> historyLocationDataList = new ArrayList<>();
        historyLocationDataList.add(new HistoryLocationData("321 Destiny Road, Hamletville, Nationland", "22/2/2023"));
        historyLocationDataList.add(new HistoryLocationData("456 Serendipity Avenue, Townsville, Countryside", "23/2/2023"));
        historyLocationDataList.add(new HistoryLocationData("789 Fortune Street, Metrocity, Urbania", "24/2/2023"));
        historyLocationDataList.add(new HistoryLocationData("101112 Luck Boulevard, Capitol City, Cosmopolis", "25/2/2023"));
        historyLocationDataList.add(new HistoryLocationData("131415 Providence Way, Downtown, Metropolis", "26/2/2023"));

        adapter = new HistoryLocationAdapter(historyLocationDataList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
        binding.icSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // Check if the key event was the Enter key and the action is ACTION_DOWN
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    String query = String.valueOf(binding.icSearch.getTextDirection());
                    adapter.filter(query);
                    return true; // Consume the event
                }
                return false; // Pass the event to other listeners
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
    