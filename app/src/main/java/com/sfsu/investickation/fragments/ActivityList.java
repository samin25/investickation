package com.sfsu.investickation.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.sfsu.adapters.ActivitiesListAdapter;
import com.sfsu.entities.Activities;
import com.sfsu.investickation.R;
import com.sfsu.investickation.RecyclerItemClickListener;
import com.sfsu.network.bus.BusProvider;
import com.sfsu.network.events.ActivityEvent;
import com.sfsu.utils.AppUtils;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows list of Activities created by User. Each Activity might contains {@link com.sfsu.entities.Observation} depending on
 * user's choice.
 */
public class ActivityList extends Fragment implements SearchView.OnQueryTextListener {

    private final String LOGTAG = "~!@#ActivityList :";
    private IActivityCallBacks mInterface;
    private Context mContext;
    private List<Activities> serverActivitiesList;
    private RecyclerView recyclerView_activity;
    private ActivitiesListAdapter mActivitiesListAdapter;

    public ActivityList() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Activities");
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        // initialize the Bus to get list of Activities from server.
        // must be cached for frequent accesses.
        BusProvider.bus().post(new ActivityEvent.OnLoadingInitialized("", AppUtils.GET_ALL_METHOD));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_activity_list, container, false);

        recyclerView_activity = (RecyclerView) v.findViewById(R.id.recyclerview_activity_list);
        recyclerView_activity.setHasFixedSize(true);

        if (mContext != null) {
            LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView_activity.setLayoutManager(mLinearLayoutManager);
            
        } else {
            Log.d(LOGTAG, " No layout manager supplied");
        }

        // Add new Activity button.
        final FloatingActionButton addProject = (FloatingActionButton) v.findViewById(R.id.fab_activity_add);
        addProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInterface.onActivityAddListener();
            }
        });

        return v;
    }


    /**
     * Subscribes to loading list of Activities from Server. This builds the list and is us
     *
     * @param onLoaded
     */
    @Subscribe
    public void onActivitiesLoadedSuccess(ActivityEvent.OnLoaded onLoaded) {
        Log.i(LOGTAG, "Activities loaded successfully");
        serverActivitiesList = onLoaded.getResponseList();

        if (serverActivitiesList.size() > 0 && serverActivitiesList != null) {
            displayActivitiesList();
        } else {
            Log.i(LOGTAG, "success but failed");
        }
    }

    @Subscribe
    public void onActivitiesLoadedSuccess(ActivityEvent.OnLoadingError onLoadingError) {
        Log.i(LOGTAG, "Activities loaded failure");
        Log.i(LOGTAG, onLoadingError.getErrorMessage());
    }


    /**
     * Helper method to display list of activities in RecyclerView.
     */
    private void displayActivitiesList() {
        Log.i(LOGTAG, "in wanted method");
        // set the List of Activities to Adapter.
        mActivitiesListAdapter = new ActivitiesListAdapter(serverActivitiesList);
        recyclerView_activity.setAdapter(mActivitiesListAdapter);

        // touch listener when the user clicks on the Activity in the List.
        recyclerView_activity.addOnItemTouchListener(new RecyclerItemClickListener(mContext, recyclerView_activity,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // call the interface callback to listen to the item click event
                        mInterface.onItemClickListener(serverActivitiesList.get(position));
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                    }
                }));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mInterface = (IActivityCallBacks) activity;
            mContext = activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IActivityInteractionListener interface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mInterface = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_activity_list, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        final List<Activities> filteredActivitiesList = filter(serverActivitiesList, query);
        mActivitiesListAdapter.animateTo(filteredActivitiesList);
        recyclerView_activity.scrollToPosition(0);
        return true;
    }

    /**
     * Helper method to filter the List of Activities on search text change in this Fragment.
     *
     * @param serverActivitiesList
     * @param query
     * @return
     */
    private List<Activities> filter(List<Activities> serverActivitiesList, String query) {
        query = query.toLowerCase();
        final List<Activities> filteredActivitiesList = new ArrayList<>();
        for (Activities activity : serverActivitiesList) {
            // perform the search on TickName since it will be visible to user.
            final String text = activity.getActivityName().toLowerCase();
            if (text.contains(query)) {
                filteredActivitiesList.add(activity);
            }
        }
        return filteredActivitiesList;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.bus().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.bus().register(this);
    }

    /**
     * Callback interface to handle callback methods for the ActivityList fragment.
     */
    public interface IActivityCallBacks {
        /**
         * Callback method to handle the Item click event of ActivityList.
         */
        public void onItemClickListener(Activities mActivity);

        /**
         * Callback method to handle the click event of the Add Button in ActivityList Fragment.
         */
        public void onActivityAddListener();
    }

}
