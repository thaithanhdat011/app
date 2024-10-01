package com.example.clearway_app;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.clearway_app.databinding.ActivityMainBinding;
import com.example.clearway_app.fragment.HomeFragment;
import com.example.clearway_app.fragment.ProfileFragment;
import com.example.clearway_app.fragment.StatisticFragment;
import com.example.clearway_app.fragment.StoresFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    ActivityMainBinding binding;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Thiết lập view binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("626313365027-le5m4po4er4pfk2p6m721fq5kcsvgpqd.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

//        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mAuth.signOut();
//                mGoogleSignInClient.signOut();
//                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//                startActivity(intent);
//                finish();
//            }

        loafFragment(new HomeFragment(), false);

        binding.bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();

                if (itemId == R.id.navHome){
                    loafFragment(new HomeFragment(), false);
                }else if (itemId == R.id.navStatistic){
                    loafFragment(new StatisticFragment(), false);
                }else if (itemId == R.id.navStores){
                    loafFragment(new StoresFragment(), false);
                }else { //nav Profile
                    loafFragment(new ProfileFragment(), false);
                }
                return true;
            }
        });
    }

    private void loafFragment(Fragment fragment, boolean isAppInitialized){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(isAppInitialized){
            fragmentTransaction.add(R.id.frameLayout, fragment);
        }else {
            fragmentTransaction.replace(R.id.frameLayout, fragment);
        }
        fragmentTransaction.commit();
    }
}
