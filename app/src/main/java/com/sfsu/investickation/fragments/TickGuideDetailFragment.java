package com.sfsu.investickation.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sfsu.adapters.ImagePagerAdapter;
import com.sfsu.entities.Tick;
import com.sfsu.investickation.R;
import com.sfsu.investickation.TickGuideMasterActivity;
import com.sfsu.investickation.TickInfoActivity;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


// sophia added

import android.support.v4.app.FragmentManager;

import static com.sfsu.investickation.R.id.viewPager_tickDet_Images;


/**
 * Displays the detailed information about all the Ticks contained in the InvesTICKations project.
 */
public class TickGuideDetailFragment extends Fragment {

    private final String TAG = "~!@#TickDetailFrag";
    @Bind(R.id.textview_known_for)
    TextView txtView_knownFor;
    @Bind(R.id.textview_tick_formal_name_inDetails)
    TextView txtView_tickFormalNameDetails;
    @Bind(R.id.textview_tick_formal_name_inHeading)
    TextView txtView_tickFormalNameHeading;
    @Bind(R.id.textview_location)
    TextView txtView_tickLocation;
//    @Bind(R.id.textview_tick_name)
//    TextView txtView_tickName;
    @Bind(R.id.textview_tick_species)
    TextView txtView_tickSpecies;
    @Bind(R.id.textview_tick_details)
    TextView txtView_description;
    @Bind(R.id.textview_season)
    TextView txtView_season;
    @Bind(R.id.textview_geoLocation)
    TextView txtView_geoLocation;
    @Bind(viewPager_tickDet_Images)
    ViewPager mViewPager;
    @Bind(R.id.circlepager_indicator_images)
    CirclePageIndicator mCirclePageIndicator;
    @Bind(R.id.button_tick_info)
    FloatingActionButton tickInfoButton;
    private Bundle args;
    private Tick mTick;
    private Context mContext;
    private ImagePagerAdapter mImagePagerAdapter;
    // arrayList of imageUrls
    private List<String> imageUrls;
    // private List<Interger> imageUrls;
    private int currentPage;

    // sophia added
    private FragmentManager mFragmentManager;


    public TickGuideDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Method to create {@link TickGuideDetailFragment} instance.
     *
     * @param key
     * @param tickObj
     * @return
     */
    public static TickGuideDetailFragment newInstance(String key, Tick tickObj) {
        TickGuideDetailFragment tickGuideDetailFragment = new TickGuideDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(key, tickObj);
        tickGuideDetailFragment.setArguments(args);
        return tickGuideDetailFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mContext = context;
        } catch (Exception e) {

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            args = getArguments();
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tick_guide_detail, container, false);
        ButterKnife.bind(this, rootView);

        final Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar_tick_details);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) rootView.findViewById(R.id
                .collapsing_toolbar_guide_detail);
        String title = getResources().getString(R.string.tickDetails_toolbar_title);
        collapsingToolbar.setTitle(title);

//        imageUrls = new ArrayList<>();
//        imageUrls.add(mTick.getImageUrl());
////        imageUrls.add("https://s3-us-west-2.amazonaws.com/tickphotos/Ixodes+pacificus_collage.png"); // list of Strings
////        imageUrls.add(R.drawable.image_2);
////        imageUrls.add(R.drawable.image_3);
////        imageUrls.add(R.drawable.image_4);
//
//        mImagePagerAdapter = new ImagePagerAdapter(mContext, imageUrls);
//
//        mViewPager.setAdapter(mImagePagerAdapter);
//        mCirclePageIndicator.setViewPager(mViewPager);
//
//        mCirclePageIndicator.setOnPageChangeListener(new PageChangeListener());
//
//        mViewPager.setCurrentItem(currentPage);

//        Log.i("Test2", mTick.getSeason());

        try {
            if (args.getParcelable(TickGuideMasterActivity.KEY_TICK_DETAIL) != null) {
                mTick = args.getParcelable(TickGuideMasterActivity.KEY_TICK_DETAIL);
            }

            txtView_tickFormalNameDetails.setText(mTick.getScientific_name());
            Log.i("Test2", mTick.getScientific_name());
            txtView_tickSpecies.setText(mTick.getSpecies());
            txtView_knownFor.setText(mTick.getKnown_for());
            txtView_tickFormalNameHeading.setText(mTick.getScientific_name());
            txtView_description.setText(mTick.getDescription());
            txtView_season.setText(mTick.getSeason());
            txtView_tickLocation.setText(mTick.getFound_near_habitat());
            txtView_geoLocation.setText(mTick.getGeoLocation());

            // for demo purpose, just create an ArrayList of images and show it from drawable


           // Picasso.with(mContext).load(mTick.getImageUrl()).into(imageView_tickImage);
            imageUrls = new ArrayList<>();
            Log.i("Test", mTick.getImageUrl());
            imageUrls.add(mTick.getImageUrl());
//        imageUrls.add("https://s3-us-west-2.amazonaws.com/tickphotos/Ixodes+pacificus_collage.png"); // list of Strings
//        imageUrls.add(R.drawable.image_2);
//        imageUrls.add(R.drawable.image_3);
//        imageUrls.add(R.drawable.image_4);

            mImagePagerAdapter = new ImagePagerAdapter(mContext, imageUrls);
            mViewPager.setAdapter(mImagePagerAdapter);
            mCirclePageIndicator.setViewPager(mViewPager);

            mCirclePageIndicator.setOnPageChangeListener(new PageChangeListener());

            mViewPager.setCurrentItem(currentPage);


            tickInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), TickInfoActivity.class);
                    intent.putExtra("TickData", mTick);
                    getActivity().startActivity(intent);
                }
            });


        } catch (NullPointerException ne) {
            throw new NullPointerException();
        } catch (Exception e) {

        }

        return rootView;
    }





    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_fragment_tick_detail);
    }

    /**
     * Listens to the change of the pages in ViewPager and sets the current content based on the CurrentPage
     */
    private class PageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int position) {
            currentPage = position;
        }

    }


}

