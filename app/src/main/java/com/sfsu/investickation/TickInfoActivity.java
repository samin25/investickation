package com.sfsu.investickation;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.sfsu.adapters.ImagePagerAdapter;
import com.sfsu.entities.Tick;
import com.sfsu.investickation.fragments.TickGuideDetailFragment;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TickInfoActivity extends AppCompatActivity {

    @Bind(R.id.textview_known_for)
    TextView txtView_knownFor;
    @Bind(R.id.textview_tick_formal_name)
    TextView txtView_tickFormalName;
    @Bind(R.id.textview_location)
    TextView txtView_tickLocation;
//    @Bind(R.id.textview_tick_name)
//    TextView txtView_tickName;
    @Bind(R.id.textview_tick_species)
    TextView txtView_tickSpecies;
//    @Bind(R.id.textview_tick_details)
//    TextView txtView_description;
    @Bind(R.id.textview_season)
    TextView txtView_season;
    @Bind(R.id.textview_geoLocation)
    TextView txtView_geoLocation;
    private Bundle args;
    private Tick mTick;
    private Context mContext;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tick_info);
        ButterKnife.bind(this);
        Tick tick = getIntent().getParcelableExtra("TickData");

//        TextView test = (TextView) findViewById(R.id.textview_tick_species);
//        test.setText("DERP");

//        txtView_tickName.setText(tick.getTickName());
//            Log.i("Test2", tick.getTickName());
            txtView_tickSpecies.setText(tick.getSpecies());
            txtView_knownFor.setText(tick.getKnown_for());
            txtView_tickFormalName.setText(tick.getScientific_name());
//            txtView_description.setText(tick.getDescription());
            txtView_season.setText(tick.getSeason());
            txtView_tickLocation.setText(tick.getFound_near_habitat());
            txtView_geoLocation.setText(tick.getGeoLocation());

    }

}

