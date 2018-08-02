package com.greenravolution.gravo.functions;

import com.greenravolution.gravo.R;
import com.greenravolution.gravo.objects.OrderDetails;
import com.greenravolution.gravo.objects.Orders;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by user on 14/3/2018.
 */

public class Rates {
    public Rates() {

    }


    public String getRates(int category_id, int weight, String rates) {
        try {
            JSONArray getRates = new JSONArray(rates);
            for (int i = 0; i < getRates.length(); i++) {
                JSONObject rate = getRates.getJSONObject(i);
                if (category_id == rate.getInt("id")) {
                    String price = rate.getString("rate");
                    String[] getPrice = price.split("/");
                    Double pricing = Double.parseDouble(getPrice[0]) * weight;
                    return String.valueOf(pricing);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "0.00";

    }

    public String getRate(int category_id, String rates) {

        try {
            JSONArray getRates = new JSONArray(rates);
            for (int i = 0; i < getRates.length(); i++) {
                JSONObject rate = getRates.getJSONObject(i);
                if (category_id == rate.getInt("id")) {
                    String price = rate.getString("rate");
                    return price;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "No Such Item";

    }

    public String getItem(int cat_id, String rates) {
        try {
            JSONArray getRates = new JSONArray(rates);
            for (int i = 0; i < getRates.length(); i++) {
                JSONObject rate = getRates.getJSONObject(i);
                if (cat_id == rate.getInt("id")) {
                    String cat = rate.getString("type");
                    return cat;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "No Such Item";

    }
    public int getImage(String type) {
        switch (type) {
            case "Paper | Newspaper":
                return R.drawable.paper_main;
            case "Paper | White Paper (black print only)":
                return R.drawable.paper_bp;
            case "Paper | Cardboard Cartons":
                return R.drawable.paper_oc;
            case "Paper | Textbooks, Magazines, Colored Paper":
                return R.drawable.paper_otb;
            case "Metals | Copper Wire -( <= 4mm)":
                return R.drawable.metal_copper_wire_one;
            case "Metals | Copper Wire -( > 4mm)":
                return R.drawable.metal_copper_wire_one;
            case "Metals | Untainted -Stripped Copper Wires":
                return R.drawable.metal_untainted_copper_wire;
            case "Metals | Dirty -Stripped Copper Wires":
                return R.drawable.metal_copper_wire_two;
            case "Metals | Brass - ":
                return R.drawable.metal_brass_item;
            case "Metals | Copper Pipe -Copper Plate":
                return R.drawable.metal_main;
            case "Metals | Telephone Wires - ":
                return R.drawable.metal_telephone_cable;
            case "Metals | Aluminium (UBC)- ":
                return R.drawable.metal_aluminium;
            case "Metals | Mixed Wires -(bundled / coiled)":
                return R.drawable.metal_mixed_wires;
            case "E-Waste | Smartphone (operational)":
                return R.drawable.ewaste_mobile_phone;
            case "E-Waste | Smartphone (non-operational)":
                return R.drawable.ewaste_mobile_phone;
            case "E-Waste | Laptop (non-operational)":
                return R.drawable.ewaste_laptop;
            case "E-Waste | CPU":
                return R.drawable.ewaste_cpu;
            case "E-Waste | LCD Screen":
                return R.drawable.ewaste_lcd_screen;
            case "E-Waste | LCD Screen (Cracked)":
                return R.drawable.ewaste_lcd_screen;
        }
        return 0;

    }

    public int getImageColour(String color) {
        switch (color) {
            case "Paper":
                return R.color.brand_yellow;
            case "Metals":
                return R.color.brand_orange;
            case "E-Waste":
                return R.color.brand_purple;
        }
        return 0;
    }
}
