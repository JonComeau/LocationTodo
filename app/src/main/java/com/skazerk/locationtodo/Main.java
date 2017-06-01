package com.skazerk.locationtodo;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Main extends AppCompatActivity {

    private List<String> masterData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        JSONObject obj;
        masterData = new ArrayList<String>();

        final ListView master = (ListView) findViewById(R.id.masterList);

        try{
            obj = new JSONObject(readJSONFromAsset());
            JSONArray jArray = obj.getJSONArray("master");
            Log.d("master", "Start");

            if (jArray != null) {
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject tmpJSON = jArray.getJSONObject(i);
                    masterData.add(tmpJSON.getString("name"));
                    Log.d("master", masterData.get(i));
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1,
                    masterData);

            Log.d("master", "End");
            master.setAdapter(adapter);

            master.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Toast.makeText(getApplicationContext(), masterData.get(i), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String readJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("master.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
