package com.example.clearway_app.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import com.example.clearway_app.R;
import com.example.clearway_app.databinding.FragmentAuthenticationPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.database.FirebaseDatabase;

public class AuthenticationPassword extends Fragment {
    FragmentAuthenticationPasswordBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    ProgressDialog progressDialog;
    boolean isPasswordVisible = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAuthenticationPasswordBinding.inflate(inflater, container, false);

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
            binding.txtMatKhau.setTextColor(Color.WHITE);
            binding.buttonNext.setTextColor(Color.WHITE);
            binding.buttonNext.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
            binding.txtMatKhauCu.setHintTextColor(Color.GRAY);
            binding.txtMatKhauCu.setTextColor(Color.BLACK);
        } else {

            binding.getRoot().setBackgroundColor(Color.WHITE);
            binding.txtMatKhau.setTextColor(Color.BLACK);
            binding.buttonNext.setTextColor(Color.BLACK);
            binding.buttonNext.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Xác thực");
        progressDialog.setMessage("Vui lòng đợi trong khi chúng tôi xác minh mật khẩu của bạn.");

        binding.imagelookblack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Đang hiển thị mật khẩu -> Ẩn mật khẩu
                    binding.txtMatKhauCu.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    binding.imagelookblack.setImageResource(R.drawable.ic_lookblack); // Đổi icon về ic_lookblack
                    isPasswordVisible = false;
                } else {
                    // Đang ẩn mật khẩu -> Hiển thị mật khẩu
                    binding.txtMatKhauCu.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    binding.imagelookblack.setImageResource(R.drawable.ic_lookwhite); // Đổi icon sang ic_lookwhite
                    isPasswordVisible = true;
                }
                // Đặt con trỏ tại cuối văn bản
                binding.txtMatKhauCu.setSelection(binding.txtMatKhauCu.getText().length());
            }
        });

        binding.buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = binding.txtMatKhauCu.getText().toString().trim();

                if (oldPassword.isEmpty()) {
                    binding.txtMatKhauCu.setError("Vui lòng nhập mật khẩu của bạn!");
                    return;
                }

                progressDialog.show();
                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                                transaction.replace(R.id.frameLayout, new NewPassword());
                                transaction.addToBackStack(null);
                                transaction.commit();
                            } else {
                                // Password is incorrect, prompt to re-enter
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(getActivity(), "Mật khẩu không đúng, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                                    binding.txtMatKhauCu.setError("Mật khẩu không đúng!");
                                    binding.txtMatKhauCu.requestFocus();
                                } else {
                                    Toast.makeText(getActivity(), "Xác thực không thành công. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
