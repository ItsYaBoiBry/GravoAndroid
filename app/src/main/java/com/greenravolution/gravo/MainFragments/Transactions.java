package com.greenravolution.gravo.MainFragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.greenravolution.gravo.R;
import com.greenravolution.gravo.contents.ActivitySelectedTransaction;
import com.greenravolution.gravo.functions.AsyncGetCompleteTransaction;
import com.greenravolution.gravo.objects.API;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class Transactions extends Fragment {
    //items are used for temporary purposes. Replace with Listview.
    LinearLayout item1, item2, transactionLayout, progressbar;
    SwipeRefreshLayout refreshLayout;

    AsyncGetCompleteTransaction.OnAsyncResult getDates = (resultCode, message) -> {
        StopLoading();
        refreshLayout.setRefreshing(false);
        try {
            LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(LAYOUT_INFLATER_SERVICE);
            JSONObject result = new JSONObject(message);
            int status = result.getInt("status");

            if (status == 200) {
                transactionLayout.removeAllViews();
                JSONArray getTransactionArray = result.getJSONArray("result");

                for (int i = 0; i < getTransactionArray.length(); i++) {

                    JSONObject transactionObject = getTransactionArray.getJSONObject(i);
                    String transactionID = transactionObject.getString("id");
                    String transactionDate = transactionObject.getString("collection_date");
                    String transactionIDKey = transactionObject.getString("transaction_id_key");
                    String transactionTotalPrice = transactionObject.getString("total_price");
                    String transactionTotalWeight = transactionObject.getString("total_weight");

                    String day = transactionDate.substring(transactionDate.lastIndexOf('-') + 1);
                    String month = transactionDate.substring(transactionDate.indexOf('-') + 1, transactionDate.lastIndexOf('-'));
                    String year = transactionDate.substring(0, transactionDate.indexOf('-'));

                    String fixedDate = day + "/" + month + "/" + year;
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = new Date();

                    try {
                        date = formatter.parse(fixedDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

//                    String newDate = date+"";
//                    String dayWord = newDate.substring(0,newDate.indexOf(" "));
//                    String monthWord = newDate.substring(newDate.indexOf(" ")+1,newDate.indexOf(" ")+4);

                    View fragmentTransaction;
                    String transactionStatus = transactionObject.getString("status_type");

                    if (transactionStatus.equals("Transaction Complete")) {
                        assert inflater != null;
                        fragmentTransaction = inflater.inflate(R.layout.transaction_page_items_complete, null);
                    } else {
                        assert inflater != null;
                        fragmentTransaction = inflater.inflate(R.layout.transaction_page_items, null);
                    }

                    TextView tvTransactionID = fragmentTransaction.findViewById(R.id.tvTransactionID);
                    TextView tvWeight = fragmentTransaction.findViewById(R.id.tvWeight);
                    TextView tvPrice = fragmentTransaction.findViewById(R.id.tvPrice);
                    TextView tvStatus = fragmentTransaction.findViewById(R.id.tvStatus);
                    TextView tvDate = fragmentTransaction.findViewById(R.id.tvDate);

                    //photos
//                    LinearLayout transaction_detail = fragmentTransaction.findViewById(R.id.transaction_details);

//                    if(transactionObject.has("details")){
//                        JSONArray detailsArray = transactionObject.getJSONArray("details");
//
//                        for(int detail=0; detail<detailsArray.length(); detail++){
//                            JSONObject detailObject = detailsArray.getJSONObject(detail);
//                            String type = detailObject.getString("category_type");
//
//                            String formattedType = type.substring(0,type.indexOf(" "));
//                            Rates rateClass = new Rates();
//
//                            ImageView ivDetailImage = new ImageView(getContext());
//
//                            ivDetailImage.setBackgroundColor(getResources().getColor(rateClass.getImageColour(formattedType)));
//                            ivDetailImage.setImageResource(rateClass.getImage(type));
//                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200,200);
//                            lp.setMargins(10,0,10,0);
//                            ivDetailImage.setLayoutParams(lp);
//
//                            transaction_detail.addView(ivDetailImage);
//                        }
//                    }

                    Log.e("long date", "getTime : " + date.getTime());

                    fragmentTransaction.setTag(transactionID);

                    tvTransactionID.setText(String.format("#%s", transactionIDKey));
                    tvWeight.setText(transactionTotalWeight);
                    tvPrice.setText(String.format("$%s", transactionTotalPrice));
                    tvStatus.setText(transactionStatus.toUpperCase());
                    tvDate.setText(fixedDate);

                    fragmentTransaction.setOnClickListener(v -> {
                        //Toast.makeText(getActivity(),"clicked "+fragmentCalendar.getTag(),Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getContext(), ActivitySelectedTransaction.class);
                        int chosenID = Integer.parseInt(fragmentTransaction.getTag().toString());
                        Log.i("getTag", chosenID + "");
                        intent.putExtra("intChosenID", chosenID);
                        startActivity(intent);

                    });
                    transactionLayout.addView(fragmentTransaction);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    };

    public Transactions() {
        // Required empty public constructor
    }

    public void StopLoading() {
        progressbar.setVisibility(View.GONE);
    }

    public void StartLoading() {
        progressbar.setVisibility(View.VISIBLE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);
        transactionLayout = view.findViewById(R.id.transactionDetails);
        progressbar = view.findViewById(R.id.progressbar);
        refreshLayout = view.findViewById(R.id.refreshlayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                SharedPreferences preferences;
                API links = new API();
                AsyncGetCompleteTransaction getTransactions = new AsyncGetCompleteTransaction();
                getTransactions.setOnResultListener(getDates);
                preferences = getActivity().getSharedPreferences("login_status", Context.MODE_PRIVATE);
                String id = String.valueOf(preferences.getInt("user_id", 0));
                String url = links.getTransaction() + "?type=userid&userid=" + id;
                getTransactions.execute(url);

            }
        });
        StopLoading();


        SharedPreferences preferences;
        API links = new API();
        AsyncGetCompleteTransaction getTransactions = new AsyncGetCompleteTransaction();
        getTransactions.setOnResultListener(getDates);
        preferences = getActivity().getSharedPreferences("login_status", Context.MODE_PRIVATE);
        String id = String.valueOf(preferences.getInt("user_id", 0));
        String url = links.getTransaction() + "?type=userid&userid=" + id;
        StartLoading();
        getTransactions.execute(url);

//        item1 = view.findViewById(R.id.item1);
//        item2 = view.findViewById(R.id.item2);
//        item1.setOnClickListener(v-> startActivity(new Intent(getContext(), ActivitySelectedTransaction.class).putExtra("transaction_id","TRANSACTION #10091983294823")));
//        item2.setOnClickListener(v-> startActivity(new Intent(getContext(), ActivitySelectedTransactionDone.class).putExtra("transaction_id","TRANSACTION #10091983294823")));

        return view;
    }

}
