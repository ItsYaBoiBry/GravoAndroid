package com.greenravolution.gravodriver.loginsignup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.greenravolution.gravodriver.MainActivity;
import com.greenravolution.gravodriver.R;
import com.greenravolution.gravodriver.functions.HttpReq;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {
    public static final String SESSION = "login_status";
    public static final String SESSION_ID = "session";

    Toolbar toolbar;
    EditText ete, etp;
    Button bl;
    CheckBox ctnc;
    TextView re;
    SharedPreferences sessionManager;
    LinearLayout llProgress;
    ImageView progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);
        toolbar = findViewById(R.id.toolbar);
        ete = findViewById(R.id.getEmail);
        etp = findViewById(R.id.getPassword);
        ctnc = findViewById(R.id.rbmMe);
        bl = findViewById(R.id.login);
        re = findViewById(R.id.resultError);
        re.setTextColor(getResources().getColor(R.color.brand_pink));
        re.setTextSize(15);
        llProgress = findViewById(R.id.avi);
        llProgress.setVisibility(View.GONE);
        progressBar = findViewById(R.id.progressBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        boolean networkState = checkNetwork();
        if (!networkState) {
            Toast.makeText(this, "Please Switch your data on", Toast.LENGTH_SHORT).show();
        }
        toolbar.setNavigationOnClickListener(v -> {
            Intent ib = new Intent();
            ib.putExtra("type", "0");
            setResult(1, ib);
            finish();

        });
        bl.setOnClickListener(v -> {
            boolean networkState1 = checkNetwork();
            if (!networkState1) {
                Toast.makeText(LoginActivity.this, "Please Switch your data on", Toast.LENGTH_SHORT).show();
            } else {
                bl.setEnabled(false);
                if (ete.getText().toString().isEmpty() || etp.getText().toString().isEmpty()) {
                    bl.setEnabled(true);
                    re.setText(R.string.invalid_login);
                } else {
                    llProgress.setVisibility(View.VISIBLE);
                    AnimationDrawable progressDrawable = (AnimationDrawable) progressBar.getDrawable();
                    progressDrawable.start();
                    Login login = new Login();
                    login.execute("http://bryanlowsk.com/UHoo/API/login.php");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent ib = new Intent();
        ib.putExtra("type", "0");
        setResult(1, ib);
        finish();
    }

    public boolean checkNetwork() {
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            // notify user you are online
            return true;

        } else if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) {

            // notify user you are not online
            Toast.makeText(this, "Please Switch your data on", Toast.LENGTH_SHORT).show();
            return false;
        }
        return false;
    }

    private class Login extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            HttpReq request = new HttpReq();
            return request.PostRequest("http://greenravolution.com/API/login.php", "getEmail=" + ete.getText().toString() + "&getPassword=" + etp.getText().toString());
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            llProgress.setVisibility(View.GONE);
            AnimationDrawable progressDrawable = (AnimationDrawable) progressBar.getDrawable();
            progressDrawable.stop();
            if (ctnc.isChecked()) {
                try {
                    String role = "";
                    String userName = "";
                    String userEmail = "";
                    String userNumber = "";
                    String userAddress = "";
                    JSONObject loginDetails = new JSONObject(s);
                    int status = loginDetails.getInt("status");
                    if (status == 200) {
                        JSONArray getUser = loginDetails.getJSONArray("user");
                        for (int i = 0; i < getUser.length(); i++) {
                            JSONObject user = getUser.getJSONObject(i);
                            userName = user.getString("first_name");
                            userEmail = user.getString("email");
                            if (user.getString("address") != null) {
                                userAddress = user.getString("address");
                            } else {
                                userAddress = null;
                            }
                            userNumber = user.getString("number");
                            role = user.getString("role_id");

                        }
                        if (role.equals("2")) {
                            Log.e("User Details", "Name: " + userName + "\nRole: " + role + "\nEmail: " + userEmail + "\nNumber: " + userNumber + "\nAddress: " + userAddress);
                            sessionManager = getSharedPreferences(SESSION, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sessionManager.edit();
                            editor.putString(SESSION_ID, String.valueOf(status));
                            editor.putString("name", userName);
                            editor.putString("role", role);
                            editor.putString("email", userEmail);
                            editor.putString("number", userNumber);
                            editor.putString("address", userAddress);

                            editor.apply();
                            Intent itmchk = new Intent(LoginActivity.this, MainActivity.class);
                            itmchk.putExtra("message", "Welcome Back " + userName + "!");
                            Intent ib = new Intent();
                            ib.putExtra("type", "1");
                            setResult(1, ib);
                            finish();
                            startActivity(itmchk);
                            bl.setEnabled(true);
                        } else {
                            bl.setEnabled(true);
                            re.setText("This is the Collector's App\nPlease use the Recycler's App\nAlternatively, you can join\nus as a collector!");
                        }
                    } else {
                        re.setText(R.string.invalid_login);
                        bl.setEnabled(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {

                try {
                    String userName = "";
                    String userEmail = "";
                    String userNumber = "";
                    String role = "";
                    String userAddress = "";
                    JSONObject loginDetails = new JSONObject(s);
                    int status = loginDetails.getInt("status");
                    if (status == 200) {
                        JSONArray getUser = loginDetails.getJSONArray("user");
                        for (int i = 0; i < getUser.length(); i++) {
                            JSONObject user = getUser.getJSONObject(i);
                            userName = user.getString("first_name");
                            userEmail = user.getString("email");
                            userNumber = user.getString("number");
                            if (user.getString("address") != null) {
                                userAddress = user.getString("address");
                            } else {
                                userAddress = null;
                            }
                            role = user.getString("role_id");

                        }
                        if (role.equals("2")) {
                            Log.e("User Details", "Name: " + userName + "\nRole: " + role + "\nEmail: " + userEmail + "\nNumber: " + userNumber + "\nAddress: " + userAddress);
                            Intent itmnochk = new Intent(LoginActivity.this, MainActivity.class);
                            itmnochk.putExtra("message", "Welcome Back!");
                            Intent ib = new Intent();
                            ib.putExtra("type", "1");
                            setResult(1, ib);
                            finish();
                            startActivity(itmnochk);
                        } else {
                            bl.setEnabled(true);
                            re.setText("This is the Collector's App\nPlease use the Recycler's App\nAlternatively, you can join\nus as a collector!");
                        }
                    } else {
                        bl.setEnabled(true);
                        re.setText(R.string.invalid_login);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
