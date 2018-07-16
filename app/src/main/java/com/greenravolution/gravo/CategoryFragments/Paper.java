package com.greenravolution.gravo.CategoryFragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.greenravolution.gravo.R;
import com.greenravolution.gravo.contents.ActivityCart;
import com.greenravolution.gravo.functions.HttpReq;
import com.greenravolution.gravo.functions.Rates;
import com.greenravolution.gravo.objects.API;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class Paper extends Fragment {
    public static final String SESSION = "login_status";
    LinearLayout paperContents;
    int weightInt = 0;
    FrameLayout frameLayout;


    SharedPreferences sessionManager;


    public Paper() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_paper, container, false);

        paperContents = view.findViewById(R.id.paperContents);
        SharedPreferences sessionManager = getActivity().getSharedPreferences(SESSION, Context.MODE_PRIVATE);
        String rates = sessionManager.getString("rates", "");
        try {
            JSONArray array = new JSONArray(rates);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                int id = object.getInt("id");
                String types = object.getString("item");
                String[] type = types.split(" ");
                if (type[0].contains("Paper")) {
                    String rat = object.getString("rate");
                    String typeName = "";
                    for (int j = 2; j < type.length; j++) {
                        typeName += type[j] + " ";
                    }
                    com.greenravolution.gravo.objects.Rates rate = new com.greenravolution.gravo.objects.Rates(id, types, rat);
                    paperContents.addView(initView(rate));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return view;
    }

    public View initView(com.greenravolution.gravo.objects.Rates rate) {
        Rates rateClass = new Rates();
        API links = new API();

        LayoutInflater inflater2 = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View contents = inflater2.inflate(R.layout.category_page_items, null);
        LinearLayout itemView = contents.findViewById(R.id.item);
        TextView itemName = contents.findViewById(R.id.itemName);
        TextView itemRate = contents.findViewById(R.id.itemRate);
        ImageView itemImage = contents.findViewById(R.id.itemImage);
        itemView.setBackgroundColor(getResources().getColor(rateClass.getImageColour("Paper")));
        ImageView itemMinus = contents.findViewById(R.id.itemMinus);
        frameLayout = contents.findViewById(R.id.framelayout);

        EditText itemsWeight = contents.findViewById(R.id.itemWeight);
        itemsWeight.setText(String.valueOf(weightInt));
        Button addToBag = contents.findViewById(R.id.add_to_bag);
        addToBag.setOnClickListener(v -> {
            int getWeight = Integer.parseInt(itemsWeight.getText().toString());
            if (getWeight == 0) {
                Toast.makeText(getContext(), "This item is empty.", Toast.LENGTH_SHORT).show();
            } else {
                int itemId = rate.getId();
                String chosenItemRate = rate.getRate();
                SharedPreferences preferences = getActivity().getSharedPreferences(SESSION, Context.MODE_PRIVATE);
                int id = preferences.getInt("user_id", 0);

                int indexOfSlash = chosenItemRate.indexOf('/');
                double itemPrice = Double.parseDouble(chosenItemRate.substring(0, indexOfSlash));

                double totalPrice = getWeight * itemPrice;

                AsyncAddCartDetails add = new AsyncAddCartDetails();
                String[] paramsArray = {links.addCartDetails(), id + "", getWeight + "", totalPrice + "", itemId + ""};
                add.execute(paramsArray);


            }
        });
        itemsWeight.setText(String.valueOf(weightInt));
        itemMinus.setOnClickListener((View v) -> {
            if (itemsWeight.getText().toString().equals("")) {
                itemsWeight.setText(String.valueOf(weightInt));

            } else {
                int getWeight = Integer.parseInt(itemsWeight.getText().toString());
                if (getWeight <= 0) {

                } else {
                    getWeight = getWeight - 1;
                    itemsWeight.setText(String.valueOf(getWeight));

                }

            }

        });

        ImageView itemPlus = contents.findViewById(R.id.itemPlus);
        itemPlus.setOnClickListener((View v) -> {
            if (itemsWeight.getText().toString().equals("")) {
                itemsWeight.setText(String.valueOf(weightInt));
            } else {
                int getWeight = Integer.parseInt(itemsWeight.getText().toString());
                getWeight = getWeight + 1;
                itemsWeight.setText(String.valueOf(getWeight));

            }

        });

        itemImage.setImageResource(rateClass.getImage(rate.getType()));
        Log.e("type: ", rate.getType());
        String[] type = rate.getType().split(" ");
        StringBuilder typeName = new StringBuilder();
        for (int j = 2; j < type.length; j++) {
            typeName.append(type[j]).append(" ");
        }
        Log.e("paper; ", typeName.toString());
        itemName.setText(typeName.toString());
        itemRate.setText(rate.getRate());

        return contents;
    }
    public class AsyncAddCartDetails extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... paramsArray) {
            HttpReq postReq = new HttpReq();
            return postReq.PostRequest(paramsArray[0], "userid=" + paramsArray[1] + "&weight=" + paramsArray[2] + "&price=" + paramsArray[3] + "&category=" + paramsArray[4]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject result = new JSONObject(s);
                int status = result.getInt("status");
                String message = result.getString("message");
                frameLayout = getActivity().findViewById(R.id.framelayout);
                if(status == 200){
                    Snackbar snackbar = Snackbar.make(frameLayout, "Item added to bag!", Snackbar.LENGTH_LONG)
                            .setAction("VIEW", v -> getContext().startActivity(new Intent(getContext(), ActivityCart.class)));
                    snackbar.setActionTextColor(Objects.requireNonNull(getContext()).getResources().getColor(R.color.brand_pink)).show();
                    View snackbarview = snackbar.getView();
                    snackbarview.setBackgroundColor(getContext().getResources().getColor(R.color.white));
                    snackbarview.setMinimumHeight(250);
                    int snackbarTextId = android.support.design.R.id.snackbar_text;
                    TextView textView = snackbarview.findViewById(snackbarTextId);
                    textView.setGravity(Gravity.CENTER_VERTICAL);
                    textView.setMinimumHeight(250);
                    Rates rates = new Rates();
                    textView.setTextColor(getResources().getColor(rates.getImageColour("Paper")));
                    textView.setTextSize(20);
                    snackbarview.animate().translationY(-20);
                    snackbar.setActionTextColor(Objects.requireNonNull(getContext()).getResources().getColor(R.color.brand_pink)).setDuration(Snackbar.LENGTH_LONG).show();

                }


                Log.i("status", status + "");
                Log.i("message", message + "");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
