package com.example.clearway_app.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.clearway_app.MainActivity;
import com.example.clearway_app.R;
import com.example.clearway_app.databinding.ActivityLoginBinding;
import com.example.clearway_app.fragment.EmailAuthentFragment;
import com.example.clearway_app.model.Users;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    ProgressDialog progressDialog;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Please Wait,\nValidation in Progress.");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("626313365027-le5m4po4er4pfk2p6m721fq5kcsvgpqd.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Password visibility toggle
        binding.imageLookBlack.setOnClickListener(new View.OnClickListener() {
            private boolean isPasswordVisible = false;

            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Hide the password and change the icon to 'ic_lookblack'
                    binding.txtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    binding.imageLookBlack.setImageResource(R.drawable.ic_lookblack);
                } else {
                    // Show the password and change the icon to 'ic_lookwhite'
                    binding.txtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    binding.imageLookBlack.setImageResource(R.drawable.ic_lookwhite);
                }
                // Move the cursor to the end of the text
                binding.txtPassword.setSelection(binding.txtPassword.getText().length());
                isPasswordVisible = !isPasswordVisible;
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

        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.txtEmail.getText().toString().trim();
                String password = binding.txtPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Hãy nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.txtEmail.setError("Email không hợp lệ!");
                    return;
                }

                progressDialog.show();
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    redirectToAppropriateActivity(user);
                                } else {
                                    Toast.makeText(LoginActivity.this, "Tài khoản hoặc mật khẩu không hợp lệ!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        binding.btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        binding.txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EmailAuthentFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

    }

    public static final int RC_SIGN_IN = 65;

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void redirectToAppropriateActivity(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            Users users = new Users();
                            users.setUserId((user.getUid()));
                            users.setUserName((user.getDisplayName()));
                            users.setProfilePic(user.getPhotoUrl().toString());
                            firebaseDatabase.getReference().child("Users").child(user.getUid()).setValue(users);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);

                            Toast.makeText(LoginActivity.this, "Sign in with Google", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }
}