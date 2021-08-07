package com.example.covid_tracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.covid_tracker.Adapters.StateWiseAdapter;
import com.example.covid_tracker.Models.StateWiseModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class StateWiseDataActivity extends AppCompatActivity {


    private RecyclerView rv_state_wise;
    private StateWiseAdapter stateWiseAdapter;
    private ArrayList<StateWiseModel> stateWiseModelArrayList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText et_search;

    private String str_state, str_confirmed, str_confirmed_new, str_active, str_active_new, str_recovered, str_recovered_new,
            str_death, str_death_new, str_lastupdatedate;

    private MainActivity activity = new MainActivity();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state_wise_data);

        getSupportActionBar().setTitle("Select State");
        //Initialise all views
        Init();

        //Fetch Statewise data
        FetchStateWiseData();

        //Setting swipe refresh layout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            FetchStateWiseData();
            swipeRefreshLayout.setRefreshing(false);
        });

        //Search
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Filter(s.toString());
            }
        });
    }

    private void Filter(String text) {
        ArrayList<StateWiseModel> filteredList = new ArrayList<>();
        for (StateWiseModel item : stateWiseModelArrayList) {
            if (item.getState().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        stateWiseAdapter.filterList(filteredList, text);
    }


    private void FetchStateWiseData() {
        //Show progress dialog
        activity.ShowDialog(this);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String apiURL = "https://api.covid19india.org/data.json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                apiURL,
                null,
                response -> {
                    try {
                        JSONArray jsonArray = response.getJSONArray("statewise");
                        stateWiseModelArrayList.clear();

                        for (int i = 1; i < jsonArray.length() ; i++){
                            JSONObject statewise = jsonArray.getJSONObject(i);

                            //After fetching, storing the data into strings
                            str_state = statewise.getString("state");

                            str_confirmed = statewise.getString("confirmed");
                            str_confirmed_new = statewise.getString("deltaconfirmed");

                            str_active = statewise.getString("active");

                            str_death = statewise.getString("deaths");
                            str_death_new = statewise.getString("deltadeaths");

                            str_recovered = statewise.getString("recovered");
                            str_recovered_new = statewise.getString("deltarecovered");
                            str_lastupdatedate = statewise.getString("lastupdatedtime");

                            //Creating an object of our statewise model class and passing the values in the constructor
                            StateWiseModel stateWiseModel = new StateWiseModel(str_state, str_confirmed, str_confirmed_new, str_active,
                                    str_death, str_death_new, str_recovered, str_recovered_new, str_lastupdatedate);
                            //adding data to our arraylist
                            stateWiseModelArrayList.add(stateWiseModel);
                        }

                        Handler makeDelay = new Handler();
                        makeDelay.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                stateWiseAdapter.notifyDataSetChanged();
                                activity.DismissDialog();
                            }
                        }, 300);

                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace());
        requestQueue.add(jsonObjectRequest);
    }


    private void Init() {
        swipeRefreshLayout = findViewById(R.id.activity_state_wise_swipe_refresh_layout);
        et_search = findViewById(R.id.activity_state_wise_search_editText);

        rv_state_wise = findViewById(R.id.activity_state_wise_recyclerview);
        rv_state_wise.setHasFixedSize(true);
        rv_state_wise.setLayoutManager(new LinearLayoutManager(this));

        stateWiseModelArrayList = new ArrayList<>();
        stateWiseAdapter = new StateWiseAdapter(StateWiseDataActivity.this, stateWiseModelArrayList);
        rv_state_wise.setAdapter(stateWiseAdapter);
    }


}