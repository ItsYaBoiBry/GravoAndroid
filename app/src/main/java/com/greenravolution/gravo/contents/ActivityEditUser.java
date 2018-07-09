package com.greenravolution.gravo.contents;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.greenravolution.gravo.R;
import com.greenravolution.gravo.functions.HttpReq;
import com.greenravolution.gravo.functions.Utility;
import com.greenravolution.gravo.login.RegisterActivity;
import com.greenravolution.gravo.objects.API;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityEditUser extends AppCompatActivity {
    Toolbar toolbar;
    LinearLayout progressbar;
    public static final String SESSION = "login_status";
    public static final String SESSION_ID = "session";
    SharedPreferences sessionManager;
    EditText newFirstName, newLastName, newAddress, newPhone;
    TextView newEmail;
    CircleImageView newProfile;
    Button cancel, save, uploadImage;
    String getName, getImage, getAddress, getEmail, getFirstName, getLastName, getPhone, getImageString;
    int getId;
    private String userChosenTask;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 1962018;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_edit_user);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressbar = findViewById(R.id.progressbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());


        newFirstName = findViewById(R.id.first_name);
        newLastName = findViewById(R.id.last_name);
        newEmail = findViewById(R.id.newEmail);
        newAddress = findViewById(R.id.newAddress);
        newProfile = findViewById(R.id.newImage);
        newPhone = findViewById(R.id.newPhone);

        cancel = findViewById(R.id.cancel);
        save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditUser editUser = new EditUser();
                ShowProgress();
                editUser.execute();
            }
        });
        uploadImage = findViewById(R.id.uploadImage);
        cancel.setOnClickListener(v -> finish());
        uploadImage.setOnClickListener(v -> selectImage());

        sessionManager = getSharedPreferences(SESSION, Context.MODE_PRIVATE);
        getId = sessionManager.getInt("user_id", -1);
        getName = sessionManager.getString("user_name", "");
        getFirstName = sessionManager.getString("user_first_name", "");
        getLastName = sessionManager.getString("user_last_name", "");
        getEmail = sessionManager.getString("user_email", "");
        getAddress = sessionManager.getString("user_address", "");
        getImage = sessionManager.getString("user_image", "");
        getPhone = sessionManager.getString("user_contact", "");

        newFirstName.setText(getFirstName);
        newLastName.setText(getLastName);
        newEmail.setText(getEmail);
        newAddress.setText(getAddress);
        newPhone.setText(getPhone);
        HideProgress();
        if (getImage.equals("")) {
            newProfile.setImageDrawable(getDrawable(R.drawable.gravo_logo_black));
        } else {
            Glide.with(ActivityEditUser.this).load(getImage).into(newProfile);
        }

    }

    public void selectImage() {

        final CharSequence[] items = {"Take a photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityEditUser.this);
        builder.setTitle("Pick Profile Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(ActivityEditUser.this);
                if (items[item].equals("Take a photo")) {
                    userChosenTask = "Take a photo";
                    if (result) {
                        cameraIntent();
                    }
                } else if (items[item].equals("Choose from Library")) {
                    userChosenTask = "Choose from Library";
                    if (result) {
                        galleryIntent();
                    }
                } else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void cameraIntent() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
            if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                Log.i("Test 19/6", "Requesting permissions");
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    public void galleryIntent() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int hasCameraPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                Log.i("Test", "Requesting permissions");
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select An Image"), SELECT_FILE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Log.e("camera intent:", data.toString());
            super.onActivityResult(requestCode, resultCode, data);
            Log.e("BULK: result Code", resultCode + "");
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == SELECT_FILE) {
                    Log.e("BULK: request code", requestCode + "");
                    onSelectFromGalleryResult(data);
                } else if (requestCode == REQUEST_CAMERA) {
                    Log.e("BULK: ", requestCode + "");
                    onCaptureImageResult(data);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;

        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(ActivityEditUser.this).getContentResolver(), data.getData());
                Log.e("BULK: bitmap:", bm.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }
            getImageString = UploadPhoto(bm);
            newProfile.setImageBitmap(bm);
        }
//                bulk_image.setImageBitmap(cameraImage);
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assert thumbnail != null;
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("BULK: bitmap:", thumbnail.toString());

        getImageString = UploadPhoto(thumbnail);
        newProfile.setImageBitmap(thumbnail);
//        bulk_image.setImageBitmap(thumbnail);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                Log.i("permissions", permissions[0]);
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    if (permissions[0].equalsIgnoreCase("android.permission.CAMERA")) {
                        cameraIntent();
                    } else if (permissions[0].equalsIgnoreCase("android.permission.READ_EXTERNAL_STORAGE")) {
                        galleryIntent();
                    }
                } else {
                    // Permission Denied
                    Toast.makeText(getBaseContext(), "Permissions Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public class EditUser extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            HttpReq req = new HttpReq();
            API api = new API();

            return req.PostRequest(api.getEditUser()
                    , "userid=" + getId
                            + "&firstname=" + newFirstName.getText().toString()
                            + "&lastname=" + newLastName.getText().toString()
                            + "&email=" + newEmail.getText().toString()
                            + "&contactnumber=" + newPhone.getText().toString()
                            + "&address=" + newAddress.getText().toString());
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            HideProgress();
            Log.i("RETURN CODE = ", s);
            try {
                JSONObject result = new JSONObject(s);
                int status = result.getInt("status");
                if (status == 200) {
                    ShowProgress();
                    EditProfilePic editProfilePic = new EditProfilePic();
                    editProfilePic.execute();
                } else {
                    Toast.makeText(ActivityEditUser.this, "Unable to update details!", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public class EditProfilePic extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            sessionManager = getSharedPreferences(SESSION, Context.MODE_PRIVATE);
            int userid = sessionManager.getInt("user_id", -1);
            API api = new API();
            HttpReq req = new HttpReq();
            return req.PostRequest(api.getUpdateImage(), "userid=" + userid + "&encoded_string=" + getImageString);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            HideProgress();
            try {
                JSONObject result = new JSONObject(s);
                int status = result.getInt("status");
                Log.e("EDIT PHOTO STATUS", result.getString("status") + "");
                if (status == 200) {
                    JSONArray users = result.getJSONArray("result");
                    JSONObject user = users.getJSONObject(0);
                    sessionManager = getSharedPreferences(SESSION, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sessionManager.edit();
                    editor.putString("user_image", user.getString("photo"));
                    editor.putString("user_first_name", user.getString("first_name"));
                    editor.putString("user_last_name", user.getString("last_name"));
                    editor.putString("user_name", user.getString("first_name") + " " + user.getString("last_name"));
                    editor.putString("user_full_name", user.getString("full_name"));
                    editor.putString("user_email", user.getString("email"));
                    editor.putString("user_contact", user.getString("contact_number"));
                    editor.putString("user_address", user.getString("address"));
                    editor.apply();
                    Toast.makeText(ActivityEditUser.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent();
//                    intent.putExtra("image bitmap", getImageString);
//                    setResult(1, intent);
                    finish();

                } else {
                    Toast.makeText(ActivityEditUser.this, "Unable to update details!", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public String UploadPhoto(Bitmap image) {
        Bitmap icon = image;
        String encodedimage = bitmapToBase64(icon);
        Log.e("BITMAP: ", encodedimage);
        return encodedimage;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        String encodedImage = "";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        try {
            encodedImage += URLEncoder.encode(Base64.encodeToString(byteArray, Base64.DEFAULT), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodedImage;
    }

    public void HideProgress() {
        save.setEnabled(true);
        cancel.setEnabled(true);
        newFirstName.setEnabled(true);
        newLastName.setEnabled(true);
        newAddress.setEnabled(true);
        newPhone.setEnabled(true);
        uploadImage.setEnabled(true);
        progressbar.setVisibility(View.GONE);
    }

    public void ShowProgress() {
        save.setEnabled(false);
        cancel.setEnabled(false);
        newFirstName.setEnabled(false);
        newLastName.setEnabled(false);
        newAddress.setEnabled(false);
        newPhone.setEnabled(false);
        uploadImage.setEnabled(false);
        progressbar.setVisibility(View.VISIBLE);
    }
}
