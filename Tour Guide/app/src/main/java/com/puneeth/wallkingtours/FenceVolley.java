package com.puneeth.wallkingtours;


import android.net.Uri;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class FenceVolley {

    private static final String dataUrl ="https://www.christopherhield.com/data/WalkingTourContent.json";

    private static final String TAG = "FenceVolley";

    private static ArrayList<LatLng> path_list=new ArrayList<>();



    public static void downloadFences(MapsActivity mapsActivity) {

        RequestQueue queue = Volley.newRequestQueue(mapsActivity);

        Uri.Builder buildURL = Uri.parse(dataUrl).buildUpon();

        String urlToUse = buildURL.build().toString();

        Response.Listener<JSONObject> listener = response -> {
            try {
                handleSuccess(response.toString(), mapsActivity);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        };

        Response.ErrorListener error = error1 -> handleFail(error1);

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, urlToUse,
                        null, listener, error);

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    private static void handleFail(VolleyError error1)
    {

        Log.d(TAG, "handleFail: "+error1);
    }

    private static void handleSuccess(String responseText,
                                                MapsActivity mapsActivity) throws JSONException {

        // HashMap<String, FenceData> fenceDataMap=new HashMap<>();
        ArrayList<FenceData> fenceDataMap = new ArrayList<>();
        JSONObject response = new JSONObject(responseText);
        Log.d(TAG, "handleSuccess: invoked");
        JSONArray fenceArray = response.getJSONArray("fences");
        // JSONArray pathArray = response.getJSONArray("path");
        Log.d(TAG, "handleSuccess: loaded " + fenceArray.length());
        for (int i = 0; i < fenceArray.length(); i++) {
            JSONObject temp = fenceArray.getJSONObject(i);
            String id = temp.getString("id");
            String address = temp.getString("address");
            String latitude = temp.getString("latitude");
            String longitude = temp.getString("longitude");
            String radius = temp.getString("radius");
            String description = temp.getString("description");
            String fenceColor = temp.getString("fenceColor");
//            if (fenceColor != null && fenceColor.startsWith("#")) {
//                // Concatenate "FF" after "#"
//                fenceColor = "#" + "FF" + fenceColor.substring(1);
//            }
            String image = temp.getString("image");
            FenceData fd = new FenceData(id, address, latitude, longitude, radius, description, fenceColor, image);
            fenceDataMap.add(fd);
            //  fenceDataMap.put(id,fd);
            // dirList.add(dir);

        }


        JSONArray jsonArray1 = response.getJSONArray("path");
        path_list.clear();
        Log.d(TAG, "handleSuccess: json array length="+jsonArray1.length());
        for (int i = 0; i < jsonArray1.length(); i++) {
            String loc = jsonArray1.getString(i);


            String[] latLong = loc.split(", ");


            LatLng ll = new LatLng(Double.parseDouble(latLong[1]), Double.parseDouble(latLong[0]));


            path_list.add(ll);
        }
            mapsActivity.runOnUiThread(() -> mapsActivity.acceptFenceData(fenceDataMap, path_list));
            fenceDataMap.clear();



    }
}
