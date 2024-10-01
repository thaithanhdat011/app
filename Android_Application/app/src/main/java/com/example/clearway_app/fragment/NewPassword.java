package com.example.clearway_app.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.clearway_app.R;
import com.example.clearway_app.databinding.FragmentNewPasswordBinding;
import com.example.clearway_app.model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NewPassword extends Fragment {
    FragmentNewPasswordBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    ProgressDialog progressDialog;
    String currentPassword;
    boolean isPasswordVisible1 = false;
    boolean isPasswordVisible2 = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Đổi mật khẩu");
        progressDialog.setMessage("Vui lòng đợi trong khi chúng tôi thay đổi mật khẩu của bạn.");

        // Lấy mật khẩu hiện tại từ Cơ sở dữ liệu thời gian thực
        fetchCurrentPassword();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNewPasswordBinding.inflate(inflater, container, false);
        // Lấy trạng thái Dark Mode từ SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);

        // Cập nhật giao diện dựa trên chế độ hiện tại
        updateTheme(isDarkMode);
        return binding.getRoot();
    }
    private void updateTheme(boolean isDarkMode) {
        if (isDarkMode) {
            // Đổi màu nền thành màu đen
            binding.getRoot().setBackgroundColor(Color.BLACK);
            binding.txtMKMoi.setTextColor(Color.WHITE);
            binding.txtNLMKMoi.setTextColor(Color.WHITE);

            binding.buttonDoiMK.setTextColor(Color.WHITE);
            binding.buttonDoiMK.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
            binding.ButtonMatKhauMoi.setHintTextColor(Color.GRAY);
            binding.ButtonMatKhauMoi.setTextColor(Color.BLACK);
            binding.txtLaiMatKhauMoi.setHintTextColor(Color.GRAY);
            binding.txtLaiMatKhauMoi.setTextColor(Color.BLACK);
        } else {

            binding.getRoot().setBackgroundColor(Color.WHITE);
            binding.txtMKMoi.setTextColor(Color.BLACK);
            binding.txtNLMKMoi.setTextColor(Color.BLACK);

            binding.buttonDoiMK.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Xử lý sự kiện ẩn/hiện mật khẩu mới
        binding.imageLookBlack1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible1) {
                    // Đang hiển thị mật khẩu -> Ẩn mật khẩu
                    binding.ButtonMatKhauMoi.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    binding.imageLookBlack1.setImageResource(R.drawable.ic_lookblack); // Đổi icon về ic_lookblack
                    isPasswordVisible1 = false;
                } else {
                    // Đang ẩn mật khẩu -> Hiển thị mật khẩu
                    binding.ButtonMatKhauMoi.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    binding.imageLookBlack1.setImageResource(R.drawable.ic_lookwhite); // Đổi icon sang ic_lookwhite
                    isPasswordVisible1 = true;
                }
                // Đặt con trỏ tại cuối văn bản
                binding.ButtonMatKhauMoi.setSelection(binding.ButtonMatKhauMoi.getText().length());
            }
        });

        // Xử lý sự kiện ẩn/hiện mật khẩu xác nhận
        binding.imageLookBlack2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible2) {
                    // Đang hiển thị mật khẩu -> Ẩn mật khẩu
                    binding.txtLaiMatKhauMoi.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    binding.imageLookBlack2.setImageResource(R.drawable.ic_lookblack); // Đổi icon về ic_lookblack
                    isPasswordVisible2 = false;
                } else {
                    // Đang ẩn mật khẩu -> Hiển thị mật khẩu
                    binding.txtLaiMatKhauMoi.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    binding.imageLookBlack2.setImageResource(R.drawable.ic_lookwhite); // Đổi icon sang ic_lookwhite
                    isPasswordVisible2 = true;
                }
                // Đặt con trỏ tại cuối văn bản
                binding.txtLaiMatKhauMoi.setSelection(binding.txtLaiMatKhauMoi.getText().length());
            }
        });


        binding.buttonDoiMK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = binding.ButtonMatKhauMoi.getText().toString().trim();
                String confirmPassword = binding.txtLaiMatKhauMoi.getText().toString().trim();

                if (newPassword.isEmpty()) {
                    binding.ButtonMatKhauMoi.setError("Vui lòng nhập mật khẩu mới!");
                    return;
                }

                if (confirmPassword.isEmpty()) {
                    binding.txtLaiMatKhauMoi.setError("Vui lòng xác nhận mật khẩu mới!");
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    binding.txtLaiMatKhauMoi.setError("Mật khẩu không khớp!");
                    binding.txtLaiMatKhauMoi.requestFocus();
                    return;
                }
                if (newPassword.equals(currentPassword)) {
                    binding.ButtonMatKhauMoi.setError("Mật khẩu mới không được giống với mật khẩu cũ!");
                    binding.ButtonMatKhauMoi.requestFocus();
                    return;
                }
                if (newPassword.length() < 8) {
                    binding.ButtonMatKhauMoi.setError("Mật khẩu phải có ít nhất 8 ký tự!");
                    return;
                }

                progressDialog.show();

                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    user.updatePassword(newPassword).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update password in Realtime Database
                            updatePasswordInDatabase(user.getUid(), newPassword);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Thay đổi mật khẩu không thành công. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchCurrentPassword() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = firebaseDatabase.getReference().child("Users").child(user.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        currentPassword = snapshot.child("password").getValue(String.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Không thể lấy thông tin mật khẩu hiện tại.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updatePasswordInDatabase(String userId, String newPassword) {
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users").child(userId);

        // Update the password field
        userRef.child("password").setValue(newPassword).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(getActivity(), "Mật khẩu đã được thay đổi thành công!", Toast.LENGTH_SHORT).show();
                redirectToProfileFragment();
            } else {
                Toast.makeText(getActivity(), "Không thể cập nhật mật khẩu trong cơ sở dữ liệu. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToProfileFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, new ProfileFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
