package com.example.mohamed.fligthapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class FlightHistory extends AppCompatActivity implements DataListener{
    boolean isTwoPane = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        FlightHistoryFragment flightHistoryFragment = new FlightHistoryFragment();
        flightHistoryFragment.setDataListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction().add(R.id.fl_history_act,flightHistoryFragment,"").commit();
        if(null!=findViewById(R.id.details_act))
            isTwoPane =true;
    }

    @Override
    public void setFlightData(String data) {
        if(!isTwoPane)
        {
            Intent intent = new Intent(this,Details.class);
            intent.putExtra("data",data);
            startActivity(intent);
        }
        else
        {
            DetailsFragment detailsFragment = new DetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("data",data);
            detailsFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.details_act,detailsFragment,"").commit();

        }
    }
}
