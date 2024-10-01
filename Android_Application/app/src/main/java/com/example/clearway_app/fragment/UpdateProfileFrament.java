package com.example.clearway_app.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.clearway_app.R;
import com.example.clearway_app.databinding.ActivityLoginBinding;
import com.example.clearway_app.databinding.FragmentUpdateProfileBinding;
import com.example.clearway_app.model.Users;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;


public class UpdateProfileFrament extends Fragment {

    FragmentUpdateProfileBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage storage;

    DatePickerDialog.OnDateSetListener dateSetListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUpdateProfileBinding.inflate(inflater, container, false);
        // Lấy trạng thái Dark Mode từ SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);

        // Cập nhật giao diện dựa trên chế độ hiện tại
        updateTheme(isDarkMode);
        return binding.getRoot();
    }

    private void updateTheme(boolean isDarkMode) {
        if (isDarkMode) {
            // Dark Mode
            binding.getRoot().setBackgroundColor(Color.BLACK);

            // Set TextView colors to white
            binding.textName.setTextColor(Color.WHITE);
            binding.txtUserName.setTextColor(Color.WHITE);
            binding.txtPhone.setTextColor(Color.WHITE);
            binding.editTextNamSinh.setTextColor(Color.WHITE);
            binding.autoSex.setTextColor(Color.WHITE);
            binding.txtName.setTextColor(Color.WHITE);
            binding.txtSDT.setTextColor(Color.WHITE);
            binding.txtNTNS.setTextColor(Color.WHITE);
            binding.txtGT.setTextColor(Color.WHITE);

            // Set EditText colors (text) to black and hint color to gray
            binding.txtUserName.setTextColor(Color.BLACK);
            binding.txtPhone.setTextColor(Color.BLACK);
            binding.editTextNamSinh.setTextColor(Color.BLACK);
            binding.txtUserName.setHintTextColor(Color.GRAY);
            binding.txtPhone.setHintTextColor(Color.GRAY);
            binding.editTextNamSinh.setHintTextColor(Color.GRAY);

            // Set AutoCompleteTextView colors
            binding.autoSex.setTextColor(Color.BLACK);

            binding.btnSave.setTextColor(Color.WHITE);
            binding.btnSave.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));

            binding.txtGioiTinh.setBoxBackgroundColor(getResources().getColor(R.color.white));
            // Set background color for any other elements if needed
            binding.plus.setColorFilter(Color.WHITE);
            binding.imagedate.setColorFilter(Color.WHITE);
            binding.plus.clearColorFilter();
        } else {
            // Light Mode
            binding.getRoot().setBackgroundColor(Color.WHITE);

            // Set TextView colors to black
            binding.textName.setTextColor(Color.BLACK);
            binding.txtUserName.setTextColor(Color.BLACK);
            binding.txtPhone.setTextColor(Color.BLACK);
            binding.editTextNamSinh.setTextColor(Color.BLACK);
            binding.autoSex.setTextColor(Color.BLACK);

            // Set EditText colors (text) to black and hint color to default
            binding.txtUserName.setTextColor(Color.BLACK);
            binding.txtPhone.setTextColor(Color.BLACK);
            binding.editTextNamSinh.setTextColor(Color.BLACK);
            binding.txtUserName.setHintTextColor(Color.GRAY);
            binding.txtPhone.setHintTextColor(Color.GRAY);
            binding.editTextNamSinh.setHintTextColor(Color.GRAY);

            // Set AutoCompleteTextView colors
            binding.autoSex.setTextColor(Color.BLACK);

            // Set background color for any other elements if needed
            binding.plus.setColorFilter(null); // Reset color filter
            binding.imagedate.setColorFilter(null); // Reset color filter
            binding.btnSave.setTextColor(Color.BLACK);
            binding.btnSave.setBackgroundTintList(getResources().getColorStateList(R.color.blue));
        }
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        String[] genders = new String[]{"Nam", "Nữ"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, genders);
        binding.autoSex.setAdapter(adapter);

        // Load user profile image from Firebase
        firebaseDatabase.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {


                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users users = snapshot.getValue(Users.class);
                        if (users != null && users.getProfilePic() != null && !users.getProfilePic().isEmpty()) {
                            Picasso.get().load(users.getProfilePic())
                                    .placeholder(R.drawable.ic_avatar) // Default placeholder image
                                    .into(binding.imageProfile);
                        }
                        if (users.getUserName() != null && !users.getUserName().isEmpty()) {
                            binding.txtUserName.setText(users.getUserName());
                        }
                        if (users.getUserName() != null && !users.getUserName().isEmpty()) {
                            binding.textName.setText(users.getUserName());
                        }
                        if (users.getGender() != null && !users.getGender().isEmpty()) {
                            binding.autoSex.setText(users.getGender(), false);
                        }
                        if (users != null) {
                            if (users.getDateOfBirth() != null && !users.getDateOfBirth().isEmpty()) {
                                binding.editTextNamSinh.setText(users.getDateOfBirth());
                            }
                        }
                        if (users.getPhone() != null && !users.getPhone().isEmpty()) {
                            binding.txtPhone.setText(users.getPhone());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle any errors that occur during the loading process
                    }
                });


        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = dayOfMonth + "/" + month + "/" + year;
                binding.editTextNamSinh.setText(date);

                // Save date to Firebase
                firebaseDatabase.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                        .child("dateOfBirth").setValue(date);
            }
        };

        // Handle click on the EditText to show the DatePickerDialog
        binding.editTextNamSinh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                        dateSetListener, year, month, day);
                datePickerDialog.show();
            }
        });

        // Handle click on the ImageView to show the DatePickerDialog
        binding.imagedate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth, dateSetListener, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = binding.txtPhone.getText().toString();

                if (!binding.txtUserName.getText().toString().isEmpty()) {
                    String username = binding.txtUserName.getText().toString();
                    String gender = binding.autoSex.getText().toString();
                    String dateOfBirth = binding.editTextNamSinh.getText().toString();

                    // Regular expression for validating Vietnamese phone numbers
                    String phonePattern = "^((09\\d{8})|(01[2689]\\d{7})|(03\\d{8})|(05\\d{8})|(07\\d{8})|(08\\d{8}))$";

                    if (phone.isEmpty()) {
                        // Trường hợp người dùng chưa nhập số điện thoại
                        Toast.makeText(getContext(), "Bạn chưa điền số điện thoại!", Toast.LENGTH_SHORT).show();
                    } else if (phone.matches(phonePattern)) {
                        // Trường hợp số điện thoại hợp lệ
                        HashMap<String, Object> obj = new HashMap<>();
                        obj.put("userName", username);
                        obj.put("gender", gender);
                        obj.put("dateOfBirth", dateOfBirth);
                        obj.put("phone", phone);

                        firebaseDatabase.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).updateChildren(obj);

                        Toast.makeText(getContext(), "Hồ sơ đã cập nhật.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Số điện thoại của bạn không đúng định dạng!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Please enter text.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        binding.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 25);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null) {
            Uri sFile = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), sFile);
                Bitmap circularBitmap = getCircularBitmap(bitmap);
                binding.imageProfile.setImageBitmap(circularBitmap);

                final StorageReference reference = storage.getReference().child("profile_pic")
                        .child(FirebaseAuth.getInstance().getUid());

                reference.putFile(sFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                firebaseDatabase.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                                        .child("profilePic").setValue(uri.toString());
                            }
                        });
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int diameter = Math.min(width, height);

        Bitmap circularBitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circularBitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        Rect rect = new Rect(0, 0, diameter, diameter);
        canvas.drawOval(new RectF(rect), paint);

        return circularBitmap;
    }

}

