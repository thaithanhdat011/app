package com.example.clearway_app.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.clearway_app.R;
import com.example.clearway_app.databinding.FragmentOtpAuthentBinding;

public class OtpAuthentFragment extends Fragment {

    private FragmentOtpAuthentBinding binding;
    private String generatedOTP;
    private String email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOtpAuthentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if (getArguments() != null) {
            generatedOTP = getArguments().getString("generatedOTP");
            email = getArguments().getString("email");  // Nhận email từ Bundle
        }

        setupOtpInputs();

        binding.btnNewPass.setOnClickListener(v -> {
            String enteredOtp = binding.otp1.getText().toString().trim() +
                    binding.otp2.getText().toString().trim() +
                    binding.otp3.getText().toString().trim() +
                    binding.otp4.getText().toString().trim();

            if (enteredOtp.equals(generatedOTP)) {
                // Replace with PassFragment or any other fragment as needed
                Bundle bundle = new Bundle();
                bundle.putString("email", email);  // Thêm email vào Bundle

                PasswordAuthentFragment passwordFragment = new PasswordAuthentFragment();
                passwordFragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, passwordFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(getActivity(), "Mã OTP không đúng!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void setupOtpInputs() {
        binding.otp1.addTextChangedListener(new GenericTextWatcher(binding.otp1, binding.otp2));
        binding.otp2.addTextChangedListener(new GenericTextWatcher(binding.otp2, binding.otp3));
        binding.otp3.addTextChangedListener(new GenericTextWatcher(binding.otp3, binding.otp4));
        binding.otp4.addTextChangedListener(new GenericTextWatcher(binding.otp4, null));
    }

    private class GenericTextWatcher implements TextWatcher {

        private View currentView;
        private View nextView;

        public GenericTextWatcher(View currentView, View nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

