package com.greenravolution.gravo.contents;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.greenravolution.gravo.R;
import com.greenravolution.gravo.functions.AsyncAddTransaction;
import com.greenravolution.gravo.functions.AsyncDeleteCartDetails;
import com.greenravolution.gravo.functions.AsyncEditCartDetails;
import com.greenravolution.gravo.functions.GetAsyncRequest;
import com.greenravolution.gravo.functions.PickUpDayReminder;
import com.greenravolution.gravo.functions.Rates;
import com.greenravolution.gravo.objects.API;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

public class ActivityCart extends AppCompatActivity implements View.OnTouchListener, OnItemClickListener {
    public static final String SESSION = "login_status";
    SharedPreferences preferences;
    Toolbar toolbar;
    Button points, cash;
    TextView scheduleDate;
    Boolean isPressed;
    GetAsyncRequest getcart;
    String requestParameters = "userid=";
    LinearLayout cartItems;
    SharedPreferences sessionManager;
    TextView tvNoOfItems;

    ImageView ivItem;
    String itemType;
    String itemRate;
    TextView tvTotalWeight;

    JSONObject item;

    private static final String LOG_TAG = "Places Autocomplete";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyAcySxtEoAxt3PcTc-LAAa506DeOGAV7nY";


    int no_of_items = 0;
    GetAsyncRequest.OnAsyncResult getRates = (resultCode, message) -> {
        try {
            JSONObject result = new JSONObject(message);
            int status = result.getInt("status");
            if (status == 404) {
                TextView noitems = new TextView(getApplicationContext());
                noitems.setText("No items to recycle yet!");
                noitems.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                noitems.setPadding(00, 250, 0, 00);
                noitems.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                noitems.setTextColor(getResources().getColor(R.color.grey));
                noitems.setTextSize(20);
                noitems.setAlpha((float) 0.50);
                cartItems.addView(noitems);

            } else if (status == 200) {
                Log.e("CART DETAILS: ", message);
                JSONArray cartDetailsArray = result.getJSONArray("result");

                no_of_items = cartDetailsArray.length();
                tvNoOfItems = findViewById(R.id.no_of_items);
                tvNoOfItems.setText(no_of_items + " item(s)");

                TextView tvTotalPrice = findViewById(R.id.totalPrice);
                double totalPrice = 0.0;
                double totalWeight = 0.0;
                double totalPiece = 0.0;

                for (int i = 0; i < cartDetailsArray.length(); i++) {
                    JSONObject cartDetails = cartDetailsArray.getJSONObject(i);
                    Log.e("cartDetails", cartDetails.toString());
                    int cartid = cartDetails.getInt("id");
                    int catid = cartDetails.getInt("category_id");
                    Double itemTotalPrice = cartDetails.getDouble("price");
                    Double itemTotalPiece = cartDetails.getDouble("weight");
                    Double itemTotalWeight = cartDetails.getDouble("weight");
                    if (catid == 14 || catid == 15 || catid == 16 || catid == 17 || catid == 18 || catid == 19) {
                        totalPiece = totalPiece + itemTotalPiece;
                        totalPrice = totalPrice + itemTotalPrice;
                    } else {
                        Log.e("price", itemTotalPrice + "");
                        itemRate = "";

                        totalWeight = totalWeight + itemTotalWeight;
                        totalPrice = totalPrice + itemTotalPrice;
                    }

                    //totalPrice = totalPrice + itemTotalPrice;


                    GetAsyncRequest.OnAsyncResult getCategoryById = (categoryResultCode, categoryMessage) -> {
                        try {
                            JSONObject results = new JSONObject(categoryMessage);
                            JSONArray rates = results.getJSONArray("result");
                            Log.e("rates", rates.toString() + "\n");
                            item = rates.getJSONObject(0);

                            itemType = item.getString("item");
                            itemRate = item.getString("rate");

                            String[] itemArray = {cartid + "", itemType + "", itemTotalPrice + "", itemTotalWeight + "", itemRate + ""};

                            Log.e("cost", itemTotalPrice + " " + itemTotalWeight + cartid);

                            cartItems.addView(initView(itemArray));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    };
                    GetAsyncRequest asyncRequest2 = new GetAsyncRequest();
                    asyncRequest2.setOnResultListener(getCategoryById);
                    asyncRequest2.execute("http://ehostingcentre.com/gravo/getCategories.php?type=withid&category=" + catid);

                }

                tvTotalPrice.setText(String.format("$%.2f", totalPrice));
                Log.e("TOTAL PRICE", String.valueOf(totalPrice));
                Log.e("TOTAL WEIGHT", String.valueOf(totalWeight));
                Log.e("TOTAL PIECE", String.valueOf(totalPiece));
                if (String.valueOf(totalPiece).equals("0.0")) {
                    tvTotalWeight.setText(String.format("%sKG", String.valueOf(totalWeight)));
                } else {
                    tvTotalWeight.setText(String.format("%sKG, %s Piece(s)", String.valueOf(totalWeight), String.valueOf(totalPiece)));
                }

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    };
    private int mYear, mMonth, mDay;

    public View initView(String[] itemArray) {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        if (inflater != null) {
            View view;
            ArrayList<com.greenravolution.gravo.objects.Rates> ratesList = new ArrayList<>();
            view = inflater.inflate(R.layout.cart_items, null);

            TextView tvTitle;
            Button delete, plus, minus;
            TextView tvTotalPrice;
            TextView tvWeight;
            TextView tvRate;


            SharedPreferences sessionManager = getApplication().getSharedPreferences(SESSION, Context.MODE_PRIVATE);
            String rates = sessionManager.getString("rates", "");

            ivItem = view.findViewById(R.id.getImage);
            tvTitle = view.findViewById(R.id.getTitle);
            delete = view.findViewById(R.id.delete);
            plus = view.findViewById(R.id.btnPlus);
            minus = view.findViewById(R.id.btnMinus);
            tvTotalPrice = view.findViewById(R.id.getPrice);
            tvWeight = view.findViewById(R.id.getWeight);
            tvRate = view.findViewById(R.id.getRate);
            tvTotalWeight = findViewById(R.id.totalWeight);

            int cartId = Integer.parseInt(itemArray[0]);
            String color = itemArray[1].substring(0, itemArray[1].indexOf(" "));

            Rates rateClass = new Rates();
            ivItem.setBackgroundColor(getResources().getColor(rateClass.getImageColour(color)));
            ivItem.setImageResource(rateClass.getImage(itemArray[1]));

            tvTitle.setText(itemArray[1]);
            tvTotalPrice.setText("$" + itemArray[2]);
            tvWeight.setText(itemArray[3]);
            tvRate.setText("$" + itemArray[4]);
            double doubleRate = Double.parseDouble(itemArray[4].substring(0, itemArray[4].indexOf("/")));
            delete.setOnClickListener(v -> {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setCancelable(false);
                dialog.setTitle("Delete Item");
                dialog.setMessage("Delete this item?");
                dialog.setPositiveButton("yes", (dialogInterface, i) -> {

                    preferences = getSharedPreferences(SESSION, Context.MODE_PRIVATE);
                    String user_id = String.valueOf(preferences.getInt("user_id", 0));

                    API links = new API();
                    String deleteCartDetailsUrl = links.deleteCartDetails();

                    AsyncDeleteCartDetails deleteCart = new AsyncDeleteCartDetails();

                    TextView tvAllPrice = findViewById(R.id.totalPrice);


                    double allPrice = Double.parseDouble((tvAllPrice.getText().toString()).substring(1));
                    double itemPrice = Double.parseDouble((tvTotalPrice.getText().toString()).substring(1));
                    Log.i("allPrice,currentPrice", allPrice + " " + itemPrice + " " + tvRate);

                    allPrice = allPrice - itemPrice;

                    tvAllPrice.setText("$" + String.format("%.2f", allPrice));

                    //totalweight
                    if(tvWeight.getText().toString() != "0.0"){
                        String totalWeight = tvTotalWeight.getText().toString();
                        double numberOfPieces = Double.parseDouble(totalWeight.substring(totalWeight.indexOf(",") + 2, totalWeight.indexOf("P") - 1));
                        double newNumberOfPieces = numberOfPieces - Double.parseDouble(tvWeight.getText().toString());
                        String newStringPieces = totalWeight.substring(0, totalWeight.indexOf(",") + 1) + " " + newNumberOfPieces + " Piece(s)";
                        tvTotalWeight.setText(newStringPieces);
                        Log.d("newStringPieces", newStringPieces);
                    }

                    String[] paramsArray = {deleteCartDetailsUrl, user_id, cartId + ""};
                    deleteCart.execute(paramsArray);

                    TextView noOfItems = findViewById(R.id.no_of_items);
                    int numberOfItems = Integer.parseInt((noOfItems.getText().toString()).substring(0, noOfItems.getText().toString().indexOf(" ")));
                    int newNumberOfItems = numberOfItems--;
                    noOfItems.setText(newNumberOfItems + " item(s)");

                    view.setVisibility(View.GONE);
                    no_of_items = no_of_items - 1;
                    tvNoOfItems = findViewById(R.id.no_of_items);
                    tvNoOfItems.setText(no_of_items + " item(s)");
                    Toast.makeText(ActivityCart.this, "Deleted", Toast.LENGTH_SHORT).show();
                });
                dialog.setNegativeButton("No", (dialogInterface, i) -> {

                    Toast.makeText(getApplicationContext(), "Not deleted", Toast.LENGTH_LONG).show();
                });
                AlertDialog dialogue = dialog.create();
                dialogue.show();

            });
            minus.setOnClickListener((View v) -> {
                double getWeight = Double.parseDouble(tvWeight.getText().toString());
                double getPrice = Double.parseDouble(tvTotalPrice.getText().toString().substring(1));
                if (getWeight <= 0) {
                    Log.i("Cart : ", "item is already 0");
                } else {
                    double newWeight = getWeight - 1;
                    tvWeight.setText(String.valueOf(newWeight));

                    String totalWeight = tvTotalWeight.getText().toString();
                    double numberOfPieces = Double.parseDouble(totalWeight.substring(totalWeight.indexOf(",")+2,totalWeight.indexOf("P")-1));
                    double newNumberOfPieces = numberOfPieces - 1;
                    String newStringPieces = totalWeight.substring(0,totalWeight.indexOf(",")+1)+ " " + newNumberOfPieces + " Piece(s)";
                    tvTotalWeight.setText(newStringPieces);
                    Log.d("newStringPieces",newStringPieces);

                    double newPrice = getPrice - ((getWeight - newWeight) * doubleRate);
                    tvTotalPrice.setText(String.format("$%s", String.valueOf(String.format("%.2f", newPrice))));

                    TextView tvAllPrice = findViewById(R.id.totalPrice);

                    Double allPrice = Double.parseDouble((tvAllPrice.getText().toString()).substring(1));
                    allPrice = allPrice - (getPrice - newPrice);

                    tvAllPrice.setText(String.format("$%.2f", allPrice));


                    String user_id = String.valueOf(preferences.getInt("user_id", 0));

                    API links = new API();
                    String editCartDetailsUrl = links.editCartDetails();

                    AsyncEditCartDetails editCart = new AsyncEditCartDetails();
                    String[] paramsArray = {editCartDetailsUrl, user_id, newWeight + "", newPrice + "", cartId + ""};
                    for (int i = 0; i < paramsArray.length; i++) {
                        Log.e("params", paramsArray[i]);
                    }
                    editCart.execute(paramsArray);


                }
            });
            plus.setOnClickListener((View v) -> {
                double getWeight = Double.parseDouble(tvWeight.getText().toString());
                double getPrice = Double.parseDouble(tvTotalPrice.getText().toString().substring(1));

                double newWeight = getWeight + 1;
                tvWeight.setText(String.valueOf(newWeight));

                if(tvWeight.getText().toString() == "0.0"){
                    String totalWeight = tvTotalWeight.getText().toString();
                    //double numberOfPieces = Double.parseDouble(totalWeight.substring(totalWeight.indexOf(",") + 2, totalWeight.indexOf("P") - 1));
                    double newNumberOfPieces = 1;
                    String newStringPieces = totalWeight + ",  " + newNumberOfPieces + " Piece(s)";
                    tvTotalWeight.setText(newStringPieces);
                    Log.d("newStringPieces", newStringPieces);
                } else {
                    String totalWeight = tvTotalWeight.getText().toString();
                    double numberOfPieces = Double.parseDouble(totalWeight.substring(totalWeight.indexOf(",") + 2, totalWeight.indexOf("P") - 1));
                    double newNumberOfPieces = numberOfPieces + 1;
                    String newStringPieces = totalWeight.substring(0, totalWeight.indexOf(",") + 1) + " " + newNumberOfPieces + " Piece(s)";
                    tvTotalWeight.setText(newStringPieces);
                    Log.d("newStringPieces", newStringPieces);
                }

                double newPrice = getPrice - ((getWeight - newWeight) * doubleRate);

                tvTotalPrice.setText("$" + String.valueOf(String.format("%.2f", newPrice)));

                TextView tvAllPrice = findViewById(R.id.totalPrice);

                Double allPrice = Double.parseDouble((tvAllPrice.getText().toString()).substring(1));
                allPrice = allPrice + (newPrice - getPrice);

                tvAllPrice.setText("$" + String.format("%.2f", allPrice));

                String user_id = String.valueOf(preferences.getInt("user_id", 0));

                API links = new API();
                String editCartDetailsUrl = links.editCartDetails();

                AsyncEditCartDetails editCart = new AsyncEditCartDetails();
                String[] paramsArray = {editCartDetailsUrl, user_id, newWeight + "", newPrice + "", cartId + ""};
                for (int i = 0; i < paramsArray.length; i++) {
                    Log.e("params", paramsArray[i]);
                }
                editCart.execute(paramsArray);


            });
            return view;
        }
        return null;

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        API links = new API();
        GetAsyncRequest getcart = new GetAsyncRequest();
        getcart.setOnResultListener(getRates);

        preferences = getSharedPreferences(SESSION, Context.MODE_PRIVATE);
        String id = String.valueOf(preferences.getInt("user_id", 0));
        String name = String.valueOf(preferences.getString("user_name", ""));
        String url = links.getCart() + requestParameters + id;
        Log.e("URL LINK: ", url);
        getcart.execute(url);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());
        cartItems = findViewById(R.id.cartItems);
        EditText etPhone = findViewById(R.id.etPhone);
        AutoCompleteTextView etAddress = findViewById(R.id.etAddress);
        EditText etRemarks = findViewById(R.id.etRemarks);
        TextView tvTotalPrice = findViewById(R.id.totalPrice);
        tvTotalWeight = findViewById(R.id.totalWeight);
        etPhone.setText(preferences.getString("user_contact", ""));
        etAddress.setText(preferences.getString("user_address", ""));

        etAddress.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        etAddress.setOnItemClickListener(ActivityCart.this);

        cash = findViewById(R.id.cash);
        cash.setOnClickListener(v -> {

            //collectiondate collectionaddress collectionuser collectionnumber total_price total_weight remarks transaction_id userid status
            //yyyy-mm-dd

            String userPhoneNo = etPhone.getText().toString();
            String userAddress = etAddress.getText().toString();
            String userRemarks = etRemarks.getText().toString();
            String userPreTotalPrice = tvTotalPrice.getText().toString();
            String userPreTotalWeight = tvTotalWeight.getText().toString();
            String userTotalPrice = userPreTotalPrice.substring(1);
            Log.e("Cart total price", userTotalPrice);
            String userTotalWeight = "0";
            Log.e("price", userTotalPrice);
            final int resultCodetest;

            Log.i("validation", scheduleDate.getText().toString() + etPhone.getText().toString() + etAddress.getText().toString());

            if (etPhone.getText().toString().equals("") || etAddress.getText().toString().equals("") || scheduleDate.getText().toString().equals("SELECT DATE")) {
                Toast.makeText(this, "Missing fields", Toast.LENGTH_SHORT).show();

            } else if (no_of_items == 0) {
                Toast.makeText(this, "No items in cart", Toast.LENGTH_SHORT).show();

            } else {
                int status_id = 1;

                String[] getdatesplit = scheduleDate.getText().toString().split("-");
                String newdate = getdatesplit[2] + "-" + getdatesplit[1] + "-" + getdatesplit[0];
                //Toast.makeText(this,""+newdate,Toast.LENGTH_SHORT).show();

                String addTransactionUrl = links.addTransaction();

                Calendar calAlarm = Calendar.getInstance();
                calAlarm.set(Calendar.DAY_OF_MONTH, Integer.parseInt(getdatesplit[0]));
                calAlarm.set(Calendar.MONTH, Integer.parseInt(getdatesplit[1]));
                calAlarm.set(Calendar.YEAR, Integer.parseInt(getdatesplit[2]));
                calAlarm.set(Calendar.HOUR, 0);
                calAlarm.set(Calendar.HOUR_OF_DAY, 0);
                calAlarm.set(Calendar.MINUTE, 0);
                calAlarm.set(Calendar.SECOND, 5);
                calAlarm.set(Calendar.AM_PM, 0);


                setAlarmForPickUpDay(calAlarm);

                AsyncAddTransaction addTransaction = new AsyncAddTransaction();
                String[] paramsArray = {addTransactionUrl, newdate, userAddress, name, userPhoneNo, userTotalPrice, tvTotalWeight.getText().toString(), userRemarks, "00000", id, status_id + ""};
                for (int i = 0; i < paramsArray.length; i++) {
                    Log.e("params", paramsArray[i]);
                }

                AsyncAddTransaction.OnAsyncResult getTransactionId = (resultCode, message) -> {
                    Log.i("message", message);
                    Log.i("resultCodeHere",resultCode + "");
                    if(resultCode == 200){
                        startActivity(new Intent(this, ActivitySuccessfullTransaction.class).putExtra("date", newdate));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),"An unexpected error has occured, Please notify us through the help centre.",Toast.LENGTH_SHORT);
                    }
                };

                addTransaction.setOnResultListener(getTransactionId);
                addTransaction.execute(paramsArray);


                Log.i("resultCodeHere","successtransactionpage");




            }

        });

        scheduleDate = findViewById(R.id.scheduleDate);
        scheduleDate.setOnClickListener(v -> getScheduleDate());

    }

    public void getScheduleDate() {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> scheduleDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year), mYear, mMonth, mDay);

        DatePicker datePicker = datePickerDialog.getDatePicker();

        Calendar minC = Calendar.getInstance();
        minC.add(Calendar.DAY_OF_YEAR, 1);
        long minDate = minC.getTimeInMillis();

        Calendar current = Calendar.getInstance();
        current.add(Calendar.DATE, 14);
        long maxDate = current.getTimeInMillis();

        datePicker.setMinDate(minDate);
        datePicker.setMaxDate(maxDate);

        datePickerDialog.show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.btnPlus) {

        }
        return false;
    }

    public void setAlarmForPickUpDay(Calendar calAlarm) {
        Log.i("calAlarm Status", calAlarm.toString());
        Intent iReminder = new Intent(this, PickUpDayReminder.class);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(this, 1234, iReminder,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calAlarm.getTimeInMillis(), pendingIntent);
    }

    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
    }

    public static ArrayList autocomplete(String input) {
        ArrayList resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:sg");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
        private ArrayList resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return String.valueOf(resultList.get(index));
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

}

//    Intent intent = new Intent(getContext(), ActivitySelectedTransaction.class);
//    int chosenID = Integer.parseInt(fragmentTransaction.getTag().toString());
//    Log.i("getTag",chosenID+"");
//    intent.putExtra("intChosenID",chosenID);
//    startActivity(intent);


