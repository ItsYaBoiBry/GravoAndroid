package com.greenravolution.gravoapp.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.greenravolution.gravoapp.Firebase.FirebaseInstanceIDService;
import com.greenravolution.gravoapp.MainActivity;
import com.greenravolution.gravoapp.R;
import com.greenravolution.gravoapp.functions.HttpReq;
import com.greenravolution.gravoapp.objects.API;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    public static final String SESSION = "login_status";
    public static final String SESSION_ID = "session";
    CallbackManager callbackManager;
    SharedPreferences sessionManager;
    LinearLayout progressbar;
    Button login, forgot_password;
    EditText email, password;
    Toolbar toolbar;
    String fbemail, fbname, fbpic, fbid ;
    HttpReq loginRequest = new HttpReq();
    API getlinkrequest = new API();
    LoginButton facebook_login;
    Button custom_facebook_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);
        callbackManager = CallbackManager.Factory.create();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        login = findViewById(R.id.login);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        progressbar = findViewById(R.id.progressbar);
        facebook_login = findViewById(R.id.facebook_login);
        custom_facebook_login = findViewById(R.id.custom_facebook_login);
        facebook_login.setVisibility(View.GONE);

        forgot_password = findViewById(R.id.forgot_password);
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email.getText().toString().equals("")) {
                    Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_LONG).show();
                } else {
                    //send forgot password email
                    ForgetPassword forgetPassword = new ForgetPassword();
                    forgetPassword.execute();
                }
            }
        });

        custom_facebook_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowProgress();
                facebook_login.performClick();
            }
        });
        // Callback registration
        facebook_login.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                getUserDetails(loginResult);
            }

            @Override
            public void onCancel() {
                // App code
                HideProgress();
            }

            @Override
            public void onError(FacebookException exception) {
                HideProgress();
                // App code
            }
        });
        HideProgress();

        login.setOnClickListener(v -> {
            if (checkNetworks()) {
                ShowProgress();
                Login login = new Login();
                login.execute(getlinkrequest.getLogin(), email.getText().toString(), password.getText().toString());
            } else {
                Toast.makeText(LoginActivity.this, "You are not connected to the internet", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("StaticFieldLeak")
    public class Login extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String results = loginRequest.PostRequest(strings[0], "email=" + strings[1] + "&password=" + strings[2]);
            //FirebaseInstanceIDService registerToken = new FirebaseInstanceIDService();
            //registerToken.callTokenRefresh(getApplicationContext());
            try {
                JSONObject resultObject = new JSONObject(results);
                int status = resultObject.getInt("status");
                if (status == 200) {
                    JSONArray users = resultObject.getJSONArray("users");
                    JSONObject user = users.getJSONObject(0);
                    String userID = "" + user.getInt("id");
                    FirebaseInstanceIDService updateToken = new FirebaseInstanceIDService();
                    Log.i("updateTokenRecycler", updateToken.toString());
                    updateToken.callTokenRefresh(userID, "update");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            HideProgress();
            Log.e("LOGIN POST EXECUTE: ", s);
            try {
                JSONObject result = new JSONObject(s);
                int status = result.getInt("status");
                if (status == 200) {
                    sessionManager = getSharedPreferences(SESSION, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sessionManager.edit();
                    editor.putString(SESSION_ID, String.valueOf(status));

                    JSONArray users = result.getJSONArray("users");
                    JSONObject user = users.getJSONObject(0);
                    if (user.getString("password").equals("facebooklogin")) {
                        Toast.makeText(LoginActivity.this, "Please Login via Facebook", Toast.LENGTH_SHORT).show();
                    } else {
                        editor.putInt("user_id", user.getInt("id"));
                        editor.putString("user_facebook_id", user.getString("facebook_id"));
                        editor.putString("user_image", user.getString("photo"));
                        editor.putString("user_first_name", user.getString("first_name"));
                        editor.putString("user_last_name", user.getString("last_name"));
                        editor.putString("user_name", user.getString("first_name") + " " + user.getString("last_name"));
                        editor.putString("user_full_name", user.getString("full_name"));
                        editor.putString("user_email", user.getString("email"));
                        editor.putString("user_contact", user.getString("contact_number"));
                        editor.putString("user_address", user.getString("address"));
                        editor.putString("user_address_block", user.getString("block"));
                        editor.putString("user_address_unit", user.getString("unit"));
                        editor.putString("user_address_street", user.getString("street"));
                        editor.putString("user_address_postal", user.getString("postal"));
                        editor.putInt("user_total_points", user.getInt("total_points"));
                        editor.putString("user_rank", user.getString("rank_name"));
                        editor.putString("login_type", "normal");
                        editor.apply();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                        finish();
                    }

                } else if (status == 404) {
                    //re.setText(R.string.not_registered);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                    LayoutInflater li = LayoutInflater.from(LoginActivity.this);
                    final View gtnc = li.inflate(R.layout.dialog_userhasnotregistered, null);
                    dialog.setCancelable(true);
                    dialog.setView(gtnc);
                    dialog.setPositiveButton("I would like to Register", (dialogInterface, i) ->  startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
                    dialog.setNegativeButton("Cancel", (dialogInterface,i) -> dialogInterface.dismiss());
                    AlertDialog dialogue = dialog.create();
                    dialogue.show();

                } else if (status == 405) {
                    String logindetail = result.getString("users");
                    if (logindetail.equals("facebooklogin")) {
                        Toast.makeText(LoginActivity.this, "Please Login via Facebook", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(LoginActivity.this, "Wrong credentials", Toast.LENGTH_LONG).show();
                    }
                } else {
                    String message = result.getString("message");
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean checkNetworks() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected void getUserDetails(LoginResult loginResult) {
        HideProgress();
        GraphRequest data_request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                (json_object, response) -> {
                    ShowProgress();
                    Log.wtf("jsonString: ", "" + json_object);
                    try {
                        JSONObject object = new JSONObject(String.valueOf(json_object));

                       fbid = "";
                        if(object.has("id")){
                            if(object.getString("id").equals("")){
                                fbid = "";
                            }else{
                                fbid = object.getString("id");
                            }
                        }else{
                            fbid = "";
                        }
                        fbname = "";
                        if (object.has("name")) {
                            if (object.getString("name").equals("")) {
                                fbname = "";
                            } else {
                                fbname = object.getString("name");
                            }
                        } else {
                            fbname = "";
                        }
                        if (object.has("picture")) {
                            JSONObject imageitems = object.getJSONObject("picture");
                            if (imageitems.has("data")) {
                                JSONObject imagedimens = imageitems.getJSONObject("data");
                                if (imagedimens.has("url")) {
                                    fbpic = imagedimens.getString("url");
                                } else {
                                    fbpic = "";
                                }
                            } else {
                                fbpic = "";
                            }
                        } else {
                            fbpic = "";
                        }
                        fbemail = "";
                        if (object.has("email")) {
                            if (object.getString("email").equals("")) {
                                fbemail = "";

                            } else {
                                fbemail = object.getString("email");
                            }
                        } else {
                            fbemail = "";
                        }
                        if(object.has("id")){
                            ShowProgress();
                            FacebookLogin fbLogin = new FacebookLogin();
                            fbLogin.execute(fbname, fbemail, fbpic, fbid);
                            LoginManager.getInstance().logOut();
                        }

                    } catch (JSONException e) {
                        HideProgress();
                        e.printStackTrace();
                    }
                });
        Bundle permission_param = new Bundle();
        permission_param.putString("fields", "id,name,email,picture.width(120).height(120)");
        data_request.setParameters(permission_param);
        data_request.executeAsync();


    }

    protected void onResume() {
        super.onResume();
        HideProgress();
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        HideProgress();
        hideSoftKeyBoard();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    public class FacebookLogin extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            HttpReq req = new HttpReq();
            API api = new API();

            String results = req.PostRequest(api.getFacebookLogin(), "facebook_id="+strings[3]+"&fullname=" + strings[0] + "&email=" + strings[1] + "&contactnumber=" + "&address=" + "" + "&image=" + strings[2]+"&token="+String.valueOf(FirebaseInstanceId.getInstance().getToken()));
            try {
                JSONObject resultObject = new JSONObject(results);
                int status = resultObject.getInt("status");
                if (status == 200) {
                    JSONArray users = resultObject.getJSONArray("result");
                    JSONObject user = users.getJSONObject(0);
                    String userID = "" + user.getInt("id");
                    FirebaseInstanceIDService updateToken = new FirebaseInstanceIDService();
                    Log.i("updateTokenRecycler", updateToken.toString());
                    updateToken.callTokenRefresh(userID, "update");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(String s) {
            HideProgress();
            super.onPostExecute(s);
            Log.e("RESTULT", s);
            try {
                JSONObject result = new JSONObject(s);
                int status = result.getInt("status");
                if (status == 200) {
                    sessionManager = getSharedPreferences(SESSION, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sessionManager.edit();
                    editor.putString(SESSION_ID, String.valueOf(status));
                    JSONArray users = result.getJSONArray("result");
                    JSONObject user = users.getJSONObject(0);
                    editor.putInt("user_id", user.getInt("id"));
                    editor.putString("user_image", user.getString("photo"));
                    editor.putString("user_first_name", user.getString("first_name"));
                    editor.putString("user_last_name", user.getString("last_name"));
                    editor.putString("user_name", user.getString("first_name") + " " + user.getString("last_name"));
                    editor.putString("user_full_name", user.getString("full_name"));
                    editor.putString("user_email", user.getString("email"));
                    editor.putString("user_contact", user.getString("contact_number"));
                    editor.putString("user_address", user.getString("address"));
                    editor.putString("user_address_block", user.getString("block"));
                    editor.putString("user_address_unit", user.getString("unit"));
                    editor.putString("user_address_street", user.getString("street"));
                    editor.putString("user_address_postal", user.getString("postal"));
                    editor.putInt("user_total_points", user.getInt("total_points"));
                    editor.putString("user_rank", user.getString("rank_name"));
                    editor.putString("login_type", "facebook");
                    editor.apply();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                    Log.e("ACTIVITY:", "TO MAIN");
                    finish();

                } else if (status == 201) {
                    sessionManager = getSharedPreferences(SESSION, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sessionManager.edit();
                    editor.putString(SESSION_ID, String.valueOf(status));
                    JSONArray users = result.getJSONArray("result");
                    JSONObject user = users.getJSONObject(0);
                    editor.putInt("user_id", user.getInt("id"));
                    editor.putString("user_image", user.getString("photo"));
                    editor.putString("user_first_name", user.getString("first_name"));
                    editor.putString("user_last_name", user.getString("last_name"));
                    editor.putString("user_name", user.getString("first_name") + " " + user.getString("last_name"));
                    editor.putString("user_full_name", user.getString("full_name"));
                    editor.putString("user_email", user.getString("email"));
                    editor.putString("user_contact", user.getString("contact_number"));
                    editor.putString("user_address", user.getString("address"));
                    editor.putString("user_address_block", user.getString("block"));
                    editor.putString("user_address_unit", user.getString("unit"));
                    editor.putString("user_address_street", user.getString("street"));
                    editor.putString("user_address_postal", user.getString("postal"));
                    editor.putInt("user_total_points", user.getInt("total_points"));
                    editor.putString("user_rank", user.getString("rank_name"));
                    editor.putString("login_type", "facebook");
                    editor.apply();
                    AddAchievements addachievements = new AddAchievements();
                    addachievements.execute(String.valueOf(user.getInt("id")));

                } else if(status == 404) {
                    Toast.makeText(LoginActivity.this, "Your account is not linked to facebook, please login manually!", Toast.LENGTH_LONG).show();
                    LoginManager.getInstance().logOut();
                }else{
                    Toast.makeText(LoginActivity.this, "An unexpected error has occured", Toast.LENGTH_LONG).show();
                    LoginManager.getInstance().logOut();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class ForgetPassword extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            API api = new API();
            HttpReq req = new HttpReq();
            return req.PostRequest(api.getForgotPassword(), "role=recycler&email=" + email.getText().toString());
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject result = new JSONObject(s);
                int status = result.getInt("status");
                if (status == 200) {
                    Toast.makeText(LoginActivity.this, result.getString("message"), Toast.LENGTH_LONG).show();
                } else if (status == 404) {
                    Toast.makeText(LoginActivity.this, "You have not registered yet!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "An unexpected error has occurred. please try again in a few minutes!", Toast.LENGTH_LONG).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void HideProgress() {
        email.setEnabled(true);
        password.setEnabled(true);
        login.setEnabled(true);
        custom_facebook_login.setEnabled(true);
        forgot_password.setEnabled(true);
        toolbar.setEnabled(true);
        progressbar.setVisibility(View.GONE);
    }

    public void ShowProgress() {
        email.setEnabled(false);
        password.setEnabled(false);
        login.setEnabled(false);
        custom_facebook_login.setEnabled(false);
        forgot_password.setEnabled(false);
        toolbar.setEnabled(false);
        progressbar.setVisibility(View.VISIBLE);
    }

    public class AddAchievements extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            HttpReq req = new HttpReq();
            return req.PostRequest("http://ehostingcentre.com/gravo/addachievementdetails.php", "id=" + strings[0]);

        }

        @Override
        protected void onPostExecute(String s) {
            Log.e("ACH RESULT", s);
            try {
                JSONObject results = new JSONObject(s);
                int status = results.getInt("status");
                if (status == 200) {
                    startActivity(new Intent(LoginActivity.this, FacebookAddDetailsActivity.class));
                    finish();
                } else if (status == 400) {
                    Toast.makeText(LoginActivity.this, "Unable to create your achievements", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "An unexpected error has occurred. please try again in a few minutes!", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(s);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideSoftKeyBoard();
    }
    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if (imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
