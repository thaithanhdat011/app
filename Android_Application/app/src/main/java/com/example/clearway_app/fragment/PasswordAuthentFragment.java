package com.example.clearway_app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.clearway_app.R;
import com.example.clearway_app.activity.LoginActivity;
import com.example.clearway_app.databinding.FragmentPasswordAuthentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PasswordAuthentFragment extends Fragment {

    private FragmentPasswordAuthentBinding binding;
    private String email;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private boolean isPasswordVisible1 = false;
    private boolean isPasswordVisible2 = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPasswordAuthentBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        if (getArguments() != null) {
            email = getArguments().getString("email");
        }


            binding.imageLookBlack1.setOnClickListener(v -> {
                if (isPasswordVisible1) {
                    binding.txtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    binding.imageLookBlack1.setImageResource(R.drawable.ic_lookblack);
                } else {
                    binding.txtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    binding.imageLookBlack1.setImageResource(R.drawable.ic_lookwhite);
                }
                isPasswordVisible1 = !isPasswordVisible1;
                // Move cursor to the end of the text
                binding.txtPassword.setSelection(binding.txtPassword.length());
            });


            binding.imageLookBlack2.setOnClickListener(v -> {
                if (isPasswordVisible2) {
                    binding.txtResentPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    binding.imageLookBlack2.setImageResource(R.drawable.ic_lookblack);
                } else {
                    binding.txtResentPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    binding.imageLookBlack2.setImageResource(R.drawable.ic_lookwhite);
                }
                isPasswordVisible2 = !isPasswordVisible2;
                // Move cursor to the end of the text
                binding.txtResentPassword.setSelection(binding.txtResentPassword.length());
            });

        binding.btnSignUp.setOnClickListener(v -> {
            String password = binding.txtPassword.getText().toString().trim();
            String confirmPassword = binding.txtResentPassword.getText().toString().trim();

            if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(getActivity(), "Vui lòng nhập đầy đủ mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            updatePassword(confirmPassword);
        });

        return binding.getRoot();
    }

    private void updatePassword(String newPassword) {
        String currentPassword = binding.txtPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, currentPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            mDatabase.child(user.getUid()).child("password").setValue(newPassword)
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            // Ensure logout is complete
                                            mAuth.signOut();

                                            // Delay before navigating to login
                                            new Handler().postDelayed(() -> {
                                                Toast.makeText(getActivity(), "Mật khẩu đã được cập nhật thành công!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                                startActivity(intent);
                                                getActivity().finish();
                                            }, 1000); // 1 second delay
                                        } else {
                                            Toast.makeText(getActivity(), "Lỗi khi cập nhật mật khẩu trong cơ sở dữ liệu!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(getActivity(), "Lỗi khi cập nhật mật khẩu!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "Người dùng không hợp lệ!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Lỗi khi đăng nhập bằng mật khẩu hiện tại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

