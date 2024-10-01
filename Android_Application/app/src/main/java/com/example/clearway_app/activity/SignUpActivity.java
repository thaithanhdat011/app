package com.example.clearway_app.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clearway_app.R;
import com.example.clearway_app.databinding.ActivitySignUpBinding;
import com.example.clearway_app.model.Users;
import com.example.clearway_app.utils.AuthService;
import com.example.clearway_app.utils.RetrofitClientAuth;
import com.example.clearway_app.utils.SignUpRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import retrofit2.Call;
import retrofit2.Retrofit;

public class SignUpActivity extends AppCompatActivity {
    ActivitySignUpBinding binding;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    private String generatedOTP;
    private String enteredOTP;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(SignUpActivity.this);
        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("We're creating your account. ");


        binding.imageLookBlack1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    binding.txtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    binding.imageLookBlack1.setImageResource(R.drawable.ic_lookblack);
                } else {
                    binding.txtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    binding.imageLookBlack1.setImageResource(R.drawable.ic_lookwhite);
                }
                binding.txtPassword.setSelection(binding.txtPassword.getText().length());
                isPasswordVisible = !isPasswordVisible;
            }
        });

        binding.txtAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        binding.btnSendOtp.setOnClickListener(new View.OnClickListener() {
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

        binding.txtPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(binding.txtPassword.getWindowToken(), 0);
                }
                binding.txtPassword.clearFocus();
                return true;
            }
            return false;
        });

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.txtUserName.getText().toString().trim();
                String email = binding.txtEmail.getText().toString().trim();
                String password = binding.txtPassword.getText().toString().trim();

                enteredOTP = binding.txtOtp.getText().toString().trim();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty() || enteredOTP.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Hãy nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!enteredOTP.equals(generatedOTP)) {
                    binding.txtOtp.setError("Mã OTP không đúng!");
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.txtEmail.setError("Email không hợp lệ!");
                    return;
                }

                if (password.length() < 6) {
                    binding.txtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự!");
                    return;
                }
                progressDialog.show();
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    Users user = new Users(username, email, password);
                                    String id = task.getResult().getUser().getUid();
                                    database.getReference().child("Users").child(id).setValue(user);
                                    mAuth.signOut();

                                    signUpUserWithAPI(username, email, password);

                                    Toast.makeText(SignUpActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(SignUpActivity.this, "Thông tin không hợp lệ hoặc tài khoản đã tồn tại!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        binding.txtAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void signUpUserWithAPI(String username, String email, String password) {
        Retrofit retrofit = RetrofitClientAuth.getRetrofitInstance();
        AuthService authService = retrofit.create(AuthService.class);

        SignUpRequest signUpRequest = new SignUpRequest(username, email, password);

        Call<Void> call = authService.signUp(signUpRequest);
        call.enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this, "API đăng ký thành công", Toast.LENGTH_SHORT).show();
                    Log.d("apiauth", "API đăng ký thành công ");
                } else {
                    Toast.makeText(SignUpActivity.this, "API đăng ký thất bại", Toast.LENGTH_SHORT).show();
                    Log.d("apiauth", "API đăng ký thất bại ");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SignUpActivity.this, "Lỗi khi gọi API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
            // Tạo một email mới
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("OTP của bạn");
            message.setText("Mã OTP của bạn là: " + otp);

            // Gửi email
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(message);
                        runOnUiThread(() -> {
                            Toast.makeText(SignUpActivity.this, "OTP đã được gửi đến email!", Toast.LENGTH_SHORT).show();
                        });
                    } catch (MessagingException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(SignUpActivity.this, "Gửi OTP thất bại!", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }).start();

        } catch (MessagingException e) {
            e.printStackTrace();
            Toast.makeText(SignUpActivity.this, "Có lỗi xảy ra khi gửi email!", Toast.LENGTH_SHORT).show();
        }
    }
}
