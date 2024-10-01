package com.example.clearway_app.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.clearway_app.R;
import com.example.clearway_app.adapter.LocationAdapter;
import com.example.clearway_app.databinding.FragmentHomeBinding;
import com.example.clearway_app.helper.SearchesResponse;
import com.example.clearway_app.model.Address;
import com.example.clearway_app.utils.LdgoGoogleMapsApi;
import com.example.clearway_app.utils.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private final LatLng defaultLocation = new LatLng(-34, 151); // Sydney, Australia as default location
    private static final int DEFAULT_ZOOM = 15;
    private FragmentHomeBinding binding;
    private LdgoGoogleMapsApi googleMapsApi;
    private ArrayList<Address> fetchedSeaches = new ArrayList<>();
    private LocationAdapter adapter;
    private LocationAdapter.RecyclerViewClickListener listener;
    private ArrayList<Marker> markers = new ArrayList<>();
    private Polyline currentPolyline;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);

        updateTheme(isDarkMode);

        // Khởi tạo FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Khởi tạo SupportMapFragment và thiết lập callback
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Khởi tạo API cho Google Maps
        googleMapsApi = RetrofitClient.getRetrofitInstance2().create(LdgoGoogleMapsApi.class);

        // Thiết lập sự kiện onClickListener cho icLocation
        binding.icLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });

        // Hiển thị lại con trỏ và bàn phím khi người dùng nhấn vào EditText
        binding.txtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.txtSearch.setCursorVisible(true);
                binding.txtSearch.requestFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(binding.txtSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        binding.icMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });

        // Thiết lập sự kiện thay đổi text cho EditText
        binding.txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    binding.tabSearch.setVisibility(View.VISIBLE); // Hiển thị tab_search
                    // Ẩn map fragment
                    FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                    ft.hide(mapFragment);
                    ft.commit();

                    searchForPlaceOnTheMap(s.toString());
                } else {
                    binding.tabSearch.setVisibility(View.GONE);  // Ẩn tab_search khi không có text
                    // Hiển thị lại map fragment
                    FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                    ft.show(mapFragment);
                    ft.commit();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}


        });


        // Hiển thị lại con trỏ và bàn phím khi người dùng nhấn vào EditText
        binding.txtSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.txtSearch.setCursorVisible(true);
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(binding.txtSearch, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        return rootView;
    }

    private void updateTheme(boolean isDarkMode) {
        if (isDarkMode) {
            // Set background to black
            binding.getRoot().setBackgroundColor(Color.BLACK);

            // Update layout_search background
            binding.layoutSearch.setBackgroundResource(R.drawable.selected_item_background);

            // Set colors for icons and text
            binding.icSearch.setColorFilter(Color.WHITE);
            binding.txtSearch.setTextColor(Color.WHITE);
            binding.txtSearch.setHintTextColor(Color.WHITE);
            binding.icMic.setColorFilter(Color.WHITE);
        } else {
            // Set background to default (white)
            binding.getRoot().setBackgroundColor(Color.WHITE);


            // Set colors for icons and text
            binding.icSearch.setColorFilter(Color.BLACK);
            binding.txtSearch.setTextColor(Color.BLACK);
            binding.txtSearch.setHintTextColor(Color.BLACK);
            binding.icMic.setColorFilter(Color.BLACK);
        }
    }




    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Xin hãy nói gì đó...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Thiết bị của bạn không hỗ trợ nhận diện giọng nói", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == getActivity().RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && result.size() > 0) {
                binding.txtSearch.setText(result.get(0));
            }
        }
    }

    private void searchForPlaceOnTheMap(String input) {
        Call<SearchesResponse> call = googleMapsApi.searchForPlace(input);
        call.enqueue(new Callback<SearchesResponse>() {
            @Override
            public void onResponse(Call<SearchesResponse> call, Response<SearchesResponse> response) {
                if(response.isSuccessful()){
                    fetchedSeaches = response.body().getResults();
                    setOnClickListener();

                    for (Marker marker : markers) {
                        marker.remove();
                    }
                    markers.clear();

                    adapter = new LocationAdapter(fetchedSeaches, listener);
                    RecyclerView.LayoutManager lm = new LinearLayoutManager(getContext());
                    binding.recyclerViewSearches.setLayoutManager(lm);
                    binding.recyclerViewSearches.setItemAnimator(new DefaultItemAnimator());
                    binding.recyclerViewSearches.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<SearchesResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Search failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Phương thức để vẽ đường trên bản đồ
    private void drawRoute(LatLng origin, LatLng destination) {
        String url = getDirectionsUrl(origin, destination);
        new Thread(() -> {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                Scanner scanner = new Scanner(inputStream);
                StringBuilder result = new StringBuilder();
                while (scanner.hasNext()) {
                    result.append(scanner.nextLine());
                }

                Log.d("DirectionsAPI", result.toString());
                parseAndDrawPath(result.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Tạo URL cho Google Directions API
    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=driving";
        String key = "key=AIzabcdefghijklmnopqrstuvwxyz";
        String parameters = str_origin + "&" + str_dest + "&" + mode + "&" + key;
        return "https://maps.googleapis.com/maps/api/directions/json?" + parameters;
    }

    // Phân tích dữ liệu JSON từ API và vẽ đường đi
    private void parseAndDrawPath(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray routes = jsonObject.getJSONArray("routes");

            if (routes.length() == 0) {
                // Hiển thị thông báo nếu không có tuyến đường nào trả về
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Không tìm thấy đường đi", Toast.LENGTH_SHORT).show();
                });
                return;
            }
            if (routes.length() > 0) {
                JSONObject route = routes.getJSONObject(0);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String encodedPoints = overviewPolyline.getString("points");

                List<LatLng> pointsList = decodePoly(encodedPoints);

                requireActivity().runOnUiThread(() -> {
                    if (currentPolyline != null) {
                        currentPolyline.remove();
                    }
                    currentPolyline = mMap.addPolyline(new PolylineOptions().addAll(pointsList).clickable(false));
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Giải mã polyline (Google API trả về polyline dưới dạng mã hóa)
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(((lat / 1E5)), ((lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private void setOnClickListener() {
        listener = new LocationAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                Address selectedPlace = fetchedSeaches.get(position);
                binding.tabSearch.setVisibility(View.GONE);

                // Ẩn con trỏ và bàn phím khi chọn địa điểm
                binding.txtSearch.setCursorVisible(false);
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.txtSearch.getWindowToken(), 0);

                // Hiển thị lại bản đồ
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                ft.show(mapFragment);
                ft.commit();

                LatLng selectedLatLng = new LatLng(Double.parseDouble(selectedPlace.getLatitudes()), Double.parseDouble(selectedPlace.getLongitudes()));
                Marker marker = mMap.addMarker(new MarkerOptions().position(selectedLatLng).title(selectedPlace.getName()));
                markers.add(marker); // Thêm marker vào danh sách
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, DEFAULT_ZOOM));

                // Lấy vị trí hiện tại và vẽ đường đi
                if (lastKnownLocation != null) {
                    LatLng currentLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    drawRoute(currentLatLng, selectedLatLng);
                }
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Kiểm tra quyền truy cập vị trí
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Yêu cầu quyền truy cập vị trí
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        // Lấy vị trí hiện tại của thiết bị và di chuyển camera đến vị trí đó
        getDeviceLocation();
    }

    private void getDeviceLocation() {
        try {
            Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(requireActivity(), new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        lastKnownLocation = task.getResult();
                        LatLng currentLocation = new LatLng(lastKnownLocation.getLatitude(),
                                lastKnownLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));
                    } else {
                        // Sử dụng vị trí mặc định nếu không lấy được vị trí hiện tại
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Giải phóng binding khi view bị hủy
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocation();
            }
        }
    }
}
