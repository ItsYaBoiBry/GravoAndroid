package com.greenravolution.gravodriver.loginsignup;

import android.content.AbstractThreadedSyncAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.greenravolution.gravodriver.MainActivity;
import com.greenravolution.gravodriver.R;
import com.greenravolution.gravodriver.functions.HttpReq;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    Toolbar toolbar;
    EditText ete, etfn, etln, etnum, etadd, etic, etpw, etli, etvl;
    Button bca;
    CheckBox ctnc;
    RelativeLayout rl;
    TextView btnc;
    public static final String SESSION = "login_status";
    public static final String SESSION_ID = "session";

    SharedPreferences sessionManager;
    int userstatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register);
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        toolbar = findViewById(R.id.toolbar);
        ete = findViewById(R.id.getEmail);
        etfn = findViewById(R.id.getFirstName);
        etln = findViewById(R.id.getLastName);
        etnum = findViewById(R.id.getNumber);
        etadd = findViewById(R.id.getAddress);
        etic = findViewById(R.id.getNRIC);
        etpw = findViewById(R.id.getPassword);
        etli = findViewById(R.id.getLiscenseNo);
        etvl = findViewById(R.id.getVehicleNumber);

        bca = findViewById(R.id.signup);
        ctnc = findViewById(R.id.ctnc);
        rl = findViewById(R.id.rl);
        btnc = findViewById(R.id.btnc);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(v -> {
            Intent ib = new Intent();
            ib.putExtra("type", "0");
            setResult(1, ib);
            finish();
        });
        bca.setOnClickListener(v -> {
            if (ete.getText().toString().isEmpty()
                    || etfn.getText().toString().isEmpty()
                    || etln.getText().toString().isEmpty()
                    || etnum.getText().toString().isEmpty()
                    || etadd.getText().toString().isEmpty()
                    || etic.getText().toString().isEmpty()
                    || etpw.getText().toString().isEmpty()
                    || etli.getText().toString().isEmpty()
                    || etvl.getText().toString().isEmpty()) {

                Snackbar.make(rl, "Please fill in all fields!", Snackbar.LENGTH_LONG).show();

            } else {

                if (ctnc.isChecked()) {
//                    Intent itmn = new Intent(RegisterActivity.this, MainActivity.class);
//                    itmn.putExtra("message", "Welcome!");
//                    Intent ib = new Intent();
//                    ib.putExtra("type", "1");
//                    setResult(1, ib);
//                    finish();
//                    startActivity(itmn);
                    Register register = new Register();
                    register.execute("https://greenravolution.com/API/collectorsignup.php");

                } else {
                    Snackbar.make(rl, "Please accept our Terms and Conditions", Snackbar.LENGTH_LONG).show();
                }

            }
        });
        btnc.setOnClickListener(v -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(RegisterActivity.this);
            LayoutInflater li = LayoutInflater.from(RegisterActivity.this);
            final View gtnc = li.inflate(R.layout.tnc_dialog, null);
            dialog.setCancelable(true);
            dialog.setView(gtnc);
            dialog.setPositiveButton("Accept", (dialogInterface, i) -> ctnc.setChecked(true));
            dialog.setNegativeButton("Later", (dialogInterface, i) -> {
            });
            AlertDialog dialogue = dialog.create();
            dialogue.show();
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

    public class Register extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            HttpReq req = new HttpReq();
            return req.PostRequest(strings[0],"firstname="+etfn.getText().toString()
                    +"&lastname="+etln.getText().toString()
                    +"&email="+ete.getText().toString()
                    +"&password="+etpw.getText().toString()
                    +"&contactnumber="+etnum.getText().toString()
                    +"&address="+etadd.getText().toString()
                    +"&nric="+etic.getText().toString()
                    +"&liscencenumber="+etli.getText().toString()
                    +"&vehiclenumber="+etvl.getText().toString());
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("ONPOSTEX SIGNUP: ",s);
            try {
                String userName = "";
                String userEmail = "";
                String userNumber = "";
                String userAddress = "";

                JSONObject result = new JSONObject(s);
                int status = result.getInt("status");
                if(status == 200){
                    JSONArray getUser = result.getJSONArray("users");
                    for (int i = 0; i < getUser.length(); i++) {
                        JSONObject user = getUser.getJSONObject(i);
                        userstatus = user.getInt("status");
                    }
                    if(userstatus == 1){

                        sessionManager = getSharedPreferences(SESSION, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sessionManager.edit();
                        editor.putString(SESSION_ID, String.valueOf(status));
                        editor.putString("name", userName);
                        editor.putString("email", userEmail);
                        editor.putString("number", userNumber);
                        editor.putString("address", userAddress);

                        editor.apply();
                        Intent itmchk = new Intent(RegisterActivity.this, MainActivity.class);
                        itmchk.putExtra("message", "Welcome Back " + userName + "!");
                        Intent ib = new Intent();
                        ib.putExtra("type", "1");
                        setResult(1, ib);
                        finish();
                        startActivity(itmchk);

                    }else if(userstatus == 0){
                        Toast.makeText(RegisterActivity.this,"You have not been approved to drive with Gravo yet! We will get back to you shortly.\n\nThank you for your patience!",Toast.LENGTH_SHORT).show();

                    }else if (userstatus == 2){
                        Toast.makeText(RegisterActivity.this,"Unfortunately, You do not fit the requirements to be a collector. We apologize for the inconvenience!",Toast.LENGTH_SHORT).show();

                    }else{
                        Toast.makeText(RegisterActivity.this,"An unexpected error has occurred. We apologize for the inconvenience!",Toast.LENGTH_SHORT).show();

                    }
                }else if(status == 404){
                    Toast.makeText(RegisterActivity.this,"An unexpected error has occurred. We apologize for the inconvenience!",Toast.LENGTH_SHORT).show();
                }else if(status == 400){
                    Toast.makeText(RegisterActivity.this,"An unexpected error has occurred. We apologize for the inconvenience!",Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

