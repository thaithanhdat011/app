package com.example.clearway_app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.clearway_app.R;
import com.example.clearway_app.activity.LoginActivity;
import com.example.clearway_app.databinding.FragmentEmailAuthentBinding;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailAuthentFragment extends Fragment {
    private FragmentEmailAuthentBinding binding;
    private String generatedOTP;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEmailAuthentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnGetOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.txtEmail.getText().toString().trim();

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.txtEmail.setError("Email không hợp lệ!");
                    return;
                }

                generatedOTP = generateOTP();
                sendEmailWithOTP(email, generatedOTP);
            }
        });

        binding.linkDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        });
    }

    private String generateOTP() {
        int randomPin = (int) (Math.random() * 9000) + 1000;
        return String.valueOf(randomPin);
    }

    private void sendEmailWithOTP(String email, String otp) {
        final String senderEmail = "katokingvx@gmail.com";
        final String senderPassword = "sspn lcal bfgi sksj";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("OTP của bạn");
            message.setText("Mã OTP của bạn là: " + otp);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(message);
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(), "OTP đã được gửi đến email!", Toast.LENGTH_SHORT).show();
                            Bundle bundle = new Bundle();
                            bundle.putString("email", email);
                            bundle.putString("generatedOTP", generatedOTP);

                            OtpAuthentFragment otpFragment = new OtpAuthentFragment();
                            otpFragment.setArguments(bundle);

                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, otpFragment)
                                    .addToBackStack(null)
                                    .commit();
                        });
                    } catch (MessagingException e) {
                        e.printStackTrace();
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(), "Gửi OTP thất bại!", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }).start();

        } catch (MessagingException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Có lỗi xảy ra khi gửi email!", Toast.LENGTH_SHORT).show();
        }
    }
}
