package com.example.covid_tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tv_confirmed, tv_confirmed_new, tv_active, tv_active_new, tv_recovered, tv_recovered_new, tv_death,
            tv_death_new, tv_tests, tv_tests_new, tv_date, tv_time;

    private LinearLayout lin_state_data, lin_world_data;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PieChart pieChart;


    private String str_confirmed, str_confirmed_new, str_active, str_active_new, str_recovered, str_recovered_new,
            str_death, str_death_new, str_tests, str_tests_new, str_last_update_time;
    private int int_active_new;
    private ProgressDialog progressDialog;

    private boolean doubleBackToExitPressedOnce = false;
    private Toast backPressToast;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Covid-Tracker (India)");
        //Initialise
        Init();


        //Fetch data from API
        FetchData();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            FetchData();
            swipeRefreshLayout.setRefreshing(false);
            //Toast.makeText(MainActivity.this, "Data refreshed!", Toast.LENGTH_SHORT).show();
        });

        lin_state_data.setOnClickListener(v -> {
            //Toast.makeText(MainActivity.this, "State data", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, StateWiseDataActivity.class));
        });

        lin_world_data.setOnClickListener(v -> {
            //Toast.makeText(MainActivity.this, "World data", Toast.LENGTH_SHORT).show();
            //Intent intent = new Intent(MainActivity.this, WorldDataActivity.class);
            //startActivity(intent);
            startActivity(new Intent(MainActivity.this, WorldDataActivity.class));
        });
    }


    private void FetchData() {

        //show progress dialog
        ShowDialog(this);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String apiUrl = "https://api.covid19india.org/data.json";

        pieChart.clearChart();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                apiUrl,
                null,
                response -> {

                    //As the data of the json are in a nested array, so we need to define the array from which we want to fetch the data.
                    JSONArray all_state_jsonArray = null;
                    JSONArray testData_jsonArray = null;


                    try {

                        all_state_jsonArray = response.getJSONArray("statewise");
                        testData_jsonArray = response.getJSONArray("tested");
                        JSONObject data_india = all_state_jsonArray.getJSONObject(0);
                        JSONObject test_data_india = testData_jsonArray.getJSONObject(testData_jsonArray.length()-1);
                        JSONObject test_data_india1 = testData_jsonArray.getJSONObject(testData_jsonArray.length()-2);

                        //Fetching data for India and storing it in String
                        str_confirmed = data_india.getString("confirmed");   //Confirmed cases in India
                        str_confirmed_new = data_india.getString("deltaconfirmed");   //New Confirmed cases from last update time

                        str_active = data_india.getString("active");    //Active cases in India

                        str_recovered = data_india.getString("recovered");  //Total recovered cased in India
                        str_recovered_new = data_india.getString("deltarecovered"); //New recovered cases from last update time

                        str_death = data_india.getString("deaths");     //Total deaths in India
                        str_death_new = data_india.getString("deltadeaths");    //New death cases from last update time

                        str_last_update_time = data_india.getString("lastupdatedtime"); //Last update date and time

                        str_tests = test_data_india.getString("totalsamplestested"); //Total samples tested in India
                        str_tests_new = test_data_india.getString("samplereportedtoday");   //New samples tested today

                        if(str_tests.isEmpty()){
                            str_tests = test_data_india1.getString("totalsamplestested"); //Total samples tested in India
                            str_tests_new = test_data_india1.getString("samplereportedtoday");   //New samples tested today
                        }

                        Handler delayToshowProgess = new Handler();

                        delayToshowProgess.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Setting text in the textview
                                tv_confirmed.setText(NumberFormat.getInstance().format(Integer.parseInt(str_confirmed)));
                                tv_confirmed_new.setText("+" + NumberFormat.getInstance().format(Integer.parseInt(str_confirmed_new)));

                                tv_active.setText(NumberFormat.getInstance().format(Integer.parseInt(str_active)));

                                int_active_new = Integer.parseInt(str_confirmed_new)
                                        - (Integer.parseInt(str_recovered_new) + Integer.parseInt(str_death_new));
                                tv_active_new.setText("+"+NumberFormat.getInstance().format(int_active_new));

                                tv_recovered.setText(NumberFormat.getInstance().format(Integer.parseInt(str_recovered)));
                                tv_recovered_new.setText("+"+NumberFormat.getInstance().format(Integer.parseInt(str_recovered_new)));

                                tv_death.setText(NumberFormat.getInstance().format(Integer.parseInt(str_death)));
                                tv_death_new.setText("+"+NumberFormat.getInstance().format(Integer.parseInt(str_death_new)));


                                    tv_tests.setText(NumberFormat.getInstance().format(Integer.parseInt(str_tests)));
                                    tv_tests_new.setText("+"+NumberFormat.getInstance().format(Integer.parseInt(str_tests_new)));

                                    tv_date.setText(FormatDate(str_last_update_time, 1));
                                    tv_time.setText(FormatDate(str_last_update_time, 2));


                                pieChart.addPieSlice(new PieModel("Active", Integer.parseInt(str_active), Color.parseColor("#007afe")));
                                pieChart.addPieSlice(new PieModel("Recovered", Integer.parseInt(str_recovered), Color.parseColor("#08a045")));
                                pieChart.addPieSlice(new PieModel("Deceased", Integer.parseInt(str_death), Color.parseColor("#F6404F")));

                                pieChart.startAnimation();

                                DismissDialog();

                            }
                        }, 500);



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                },
                error -> error.printStackTrace());
        requestQueue.add(jsonObjectRequest);
    }

    public void ShowDialog(Context context) {
        //setting up progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public void DismissDialog() {
        progressDialog.dismiss();
    }


    public String FormatDate(String date, int testCase) {
        Date mDate = null;
        String dateFormat;
        try {
            mDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).parse(date);
            if (testCase == 0) {
                dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(mDate);
                return dateFormat;
            } else if (testCase == 1) {
                dateFormat = new SimpleDateFormat("dd MMM yyyy").format(mDate);
                return dateFormat;
            } else if (testCase == 2) {
                dateFormat = new SimpleDateFormat("hh:mm a").format(mDate);
                return dateFormat;
            } else {
                Log.d("error", "Wrong input! Choose from 0 to 2");
                return "Error";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return date;
        }
    }

    private void Init() {
        tv_confirmed = findViewById(R.id.activity_main_confirmed_textview);
        tv_confirmed_new = findViewById(R.id.activity_main_confirmed_new_textview);
        tv_active = findViewById(R.id.activity_main_active_textview);
        tv_active_new = findViewById(R.id.activity_main_active_new_textview);
        tv_recovered = findViewById(R.id.activity_main_recovered_textview);
        tv_recovered_new = findViewById(R.id.activity_main_recovered_new_textview);
        tv_death = findViewById(R.id.activity_main_death_textview);
        tv_death_new = findViewById(R.id.activity_main_death_new_textview);
        tv_tests = findViewById(R.id.activity_main_samples_textview);
        tv_tests_new = findViewById(R.id.activity_main_samples_new_textview);
        tv_date = findViewById(R.id.activity_main_date_textview);
        tv_time = findViewById(R.id.activity_main_time_textview);

        pieChart = findViewById(R.id.activity_main_piechart);
        swipeRefreshLayout = findViewById(R.id.activity_main_swipe_refresh_layout);
        lin_state_data = findViewById(R.id.activity_main_statewise_lin);
        lin_world_data = findViewById(R.id.activity_main_world_data_lin);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.menu_about){
            //Toast.makeText(MainActivity.this, "About is Clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            backPressToast.cancel();
            super.onBackPressed();
            return;
        }
        doubleBackToExitPressedOnce = true;
        backPressToast = Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT);
        backPressToast.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}

