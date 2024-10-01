package com.example.clearway_app.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.clearway_app.R;
import com.example.clearway_app.activity.LoginActivity;
import com.example.clearway_app.databinding.FragmentProfileBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SharedPreferences sharedPreferences;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = requireActivity().getSharedPreferences("app_settings", MODE_PRIVATE);
        // Áp dụng ngôn ngữ đã lưu
        applySavedLanguage();
        // Áp dụng chủ đề đã lưu (nếu có)
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ?
                AppCompatDelegate.MODE_NIGHT_YES :
                AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Khởi tạo liên kết chế độ xem
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Lắng nghe sự kiện click cho TextView và ImageView
        binding.textNgonngu.setOnClickListener(v -> showLanguageDialog());
        binding.iconRightButton3.setOnClickListener(v -> showLanguageDialog());

        // Đặt công tắc sang chế độ chủ đề hiện tại
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        binding.switchDarkmore.setChecked(isDarkMode);

        // Cập nhật màu UI dựa trên chủ đề hiện tại
        updateTheme(isDarkMode);

        // Đặt trình nghe nhấp chuột bằng cách sử dụng View Binding
        binding.textHoSo.setOnClickListener(v -> navigateToUpdateProfileFragment());
        binding.iconRightButton2.setOnClickListener(v -> navigateToUpdateProfileFragment());
        binding.textMatkhau.setOnClickListener(v -> navigateToAuthenticationPassword());
        binding.iconRightButton2.setOnClickListener(v -> navigateToAuthenticationPassword());

        // Xử lý chuyển đổi chế độ tối
        binding.switchDarkmore.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Lưu lại trạng thái chế độ Dark Mode vào SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();

            // Update UI colors based on the current theme
            updateTheme(isChecked);
        });
        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("626313365027-le5m4po4er4pfk2p6m721fq5kcsvgpqd.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                mGoogleSignInClient.signOut();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    private void updateTheme(boolean isDarkMode) {
        if (isDarkMode) {
            // Đổi nền thành màu đen
            binding.getRoot().setBackgroundColor(Color.BLACK);

            // Đổi màu chữ thành màu trắng
            binding.textHoSo.setTextColor(Color.WHITE);
            binding.textMatkhau.setTextColor(Color.WHITE);
            binding.textNgonngu.setTextColor(Color.WHITE);


            // Đổi màu biểu tượng ImageView thành màu trắng
            binding.iconHoSo.setColorFilter(Color.WHITE);
            binding.iconMatkhau.setColorFilter(Color.WHITE);
            binding.iconNgonngu.setColorFilter(Color.WHITE);
            binding.iconDarkmode.setColorFilter(Color.WHITE);

            // Đổi nền BottomNavigationView thành màu đen
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavView);
            bottomNav.setBackgroundColor(Color.rgb (25,25,112));

            // Thay đổi biểu tượng BottomNavigationView và màu chữ thành màu trắngs
            bottomNav.setItemIconTintList(ColorStateList.valueOf(Color.WHITE));
            bottomNav.setItemTextColor(ColorStateList.valueOf(Color.WHITE));

        } else {
            //Đổi nền sang màu mặc định (nền trắng hoặc chế độ sáng)
            binding.getRoot().setBackgroundColor(Color.WHITE);

            // Đổi màu chữ thành mặc định (màu đen)
            binding.textHoSo.setTextColor(Color.BLACK);
            binding.textMatkhau.setTextColor(Color.BLACK);
            binding.textNgonngu.setTextColor(Color.BLACK);

            // Đặt lại biểu tượng ImageView về màu mặc định
            binding.iconHoSo.setColorFilter(null);
            binding.iconMatkhau.setColorFilter(null);
            binding.iconNgonngu.setColorFilter(null);
            binding.iconDarkmode.setColorFilter(null);

            // Thay đổi nền BottomNavigationView thành mặc định (màu trắng)
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavView);
            bottomNav.setBackgroundColor(Color.WHITE);

            // Thay đổi biểu tượng BottomNavigationView và màu chữ thành mặc định (màu gốc)
            bottomNav.setItemIconTintList(getResources().getColorStateList(R.color.nav_item_color));
            bottomNav.setItemTextColor(getResources().getColorStateList(R.color.nav_item_color));
        }
    }

    private void showLanguageDialog() {
        String[] languages = {getString(R.string.language_english), getString(R.string.language_vietnamese)};
        int checkedItem = sharedPreferences.getString("language", "vi").equals("vi") ? 1 : 0;

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.txt_NgonNgu)
                .setSingleChoiceItems(languages, checkedItem, null)
                .setPositiveButton(R.string.btn_Ok, (dialogInterface, whichButton) -> {
                    int selectedPosition = ((AlertDialog) dialogInterface).getListView().getCheckedItemPosition();
                    String selectedLanguage = selectedPosition == 0 ? "en" : "vi";
                    updateLanguage(selectedLanguage);
                    Toast.makeText(getContext(), getString(R.string.msg_language_selected, languages[selectedPosition]), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.btn_Cancel, (dialogInterface, which) -> {
                    Toast.makeText(getContext(), R.string.msg_no_change, Toast.LENGTH_SHORT).show();
                })
                .create();

        dialog.show();

        // Đổi màu nút btn_Ok và btn_Cancel thành màu đen
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
    }


    private void updateLanguage(String languageCode) {
        // Lưu ngôn ngữ đã chọn vào SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("language", languageCode);
        editor.apply();

        // Cập nhật cấu hình ngôn ngữ của ứng dụng
        Locale newLocale = new Locale(languageCode);
        Locale.setDefault(newLocale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(newLocale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Tải lại giao diện để áp dụng ngôn ngữ mới
        getActivity().recreate();
    }

    private void applySavedLanguage() {
        // Lấy ngôn ngữ đã lưu trong SharedPreferences, mặc định là tiếng Việt
        String languageCode = sharedPreferences.getString("language", "vi");
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }

    private void navigateToUpdateProfileFragment() {
        Fragment updateProfileFragment = new UpdateProfileFrament(); // Replace with your fragment class name
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, updateProfileFragment); // Replace R.id.fragment_container with your container id
        transaction.addToBackStack(null); // Optional: Adds the transaction to the back stack
        transaction.commit();
    }
    private void navigateToAuthenticationPassword() {
        Fragment authenticationPasswordFragment = new AuthenticationPassword(); // Replace with your fragment class name
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, authenticationPasswordFragment); // Replace R.id.fragment_container with your container id
        transaction.addToBackStack(null); // Optional: Adds the transaction to the back stack
        transaction.commit();
    }

}

