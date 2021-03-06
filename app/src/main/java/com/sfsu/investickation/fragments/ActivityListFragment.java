package com.sfsu.investickation.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.appcompat.BuildConfig;
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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.sfsu.adapters.ActivitiesListAdapter;
import com.sfsu.controllers.DatabaseDataController;
import com.sfsu.db.ActivitiesDao;
import com.sfsu.dialogs.UploadAlertDialog;
import com.sfsu.entities.Activities;
import com.sfsu.investickation.R;
import com.sfsu.investickation.RecyclerItemClickListener;
import com.sfsu.network.bus.BusProvider;
import com.sfsu.network.events.ActivityEvent;
import com.sfsu.network.handler.ApiRequestHandler;
import com.sfsu.utils.AppUtils;
import com.sfsu.utils.MyRecyclerScroll;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * <p>
 * Displays list of {@link Activities} created by Account. Each Activity might contain {@link com.sfsu.entities.Observation}
 * depending on user's choice.
 * </p>
 * <p>
 * The Activities diplayed are combination of that stored on the local storage as well as that stored on cloud. Each Activity
 * List item contains an icon which displays whether the Activity is stored on the local SD card or the Cloud.
 * </p>
 * <p>
 * In addition to that, each item displays the brief intro about each activity i.e. number of people in Activity, number of Pets
 * as well as total observations made in each Activity.
 * </p>
 */
public class ActivityListFragment extends Fragment implements SearchView.OnQueryTextListener, UploadAlertDialog.IUploadDataCallback {

    public static final String KEY_LOCAL_ACTIVITIES = "local_activities";
    public final String TAG = "~!@#ActivityList";
    @Bind(R.id.swipelayout_activity_list_main)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.recyclerview_activity_list)
    RecyclerView recyclerView_activity;
    @Bind(R.id.fab_activity_add)
    FloatingActionButton fab_addActivity;
    @Bind(R.id.relativeLayout_actList_main)
    RelativeLayout mRelativeLayout;
    @Bind(R.id.textview_static_activity_list_info)
    TextView txtView_activityListInfo;
    int count = 0;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    private IActivityCallBacks mInterface;
    private Context mContext;
    private List<Activities> mResponseActivitiesList, mActivitiesList, mLocalActivitiesList;
    private ActivitiesListAdapter activitiesListAdapter;
    private boolean loading = true;
    private LinearLayoutManager mLinearLayoutManager;
    private DatabaseDataController dbController;
    private int fabMargin;
    private Animation animation;
    private UploadAlertDialog mUploadAlertDialog;
    private List<Activities> uploadActivities;
    private ProgressDialog mProgressDialog;

    public ActivityListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_activity_list, container, false);
        ButterKnife.bind(this, rootView);

        animation = AnimationUtils.loadAnimation(mContext, R.anim.simple_grow);
        dbController = new DatabaseDataController(mContext, ActivitiesDao.getInstance());
        mUploadAlertDialog = new UploadAlertDialog(mContext, this);
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        // Add new Activity button.
        fab_addActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInterface.onActivityAddListener();
            }
        });

        // Setup refresh listener which triggers new data loading
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchActivitiesFromServerAndDatabaseAsync(true);
            }
        });
        // Configure the refreshing colors
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_dark,
                android.R.color.holo_blue_dark,
                android.R.color.holo_orange_light,
                android.R.color.holo_green_dark);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_fragment_activity_list);
        BusProvider.bus().register(this);
        mProgressDialog = new ProgressDialog(mContext);

        // get all the Activities
        fetchActivitiesFromServerAndDatabaseAsync(false);


        // by default the TextView is invisible
        txtView_activityListInfo.setVisibility(View.GONE);
        recyclerView_activity.setHasFixedSize(true);

        if (mContext != null) {
            mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView_activity.setLayoutManager(mLinearLayoutManager);
        } else {
            Log.d(TAG, " No layout manager supplied");
        }
    }

    /**
     * Function fetches Activities from Server and Local Storage asynchronously
     */
    private void fetchActivitiesFromServerAndDatabaseAsync(boolean isSwipeRefresh) {
        if (AppUtils.isConnectedOnline(mContext)) {
            // Run a separate thread to get data from DB and pass it ot handler which is accessed in the
            // onActivitiesLoadedSuccess construct.
            final Thread dbAccessThread = new Thread(new Runnable() {
                private final Handler mLocalActivitiesHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        // obtain all the Activities from the MessageQueue
                        mLocalActivitiesList = msg.getData().getParcelableArrayList(KEY_LOCAL_ACTIVITIES);
                    }
                };

                /**
                 * Creates a Message and sets the Bundle inside the message which carries the List of Activities returned from
                 * the Database
                 * @param localActivities
                 */
                void passActivitiesToHandler(ArrayList<Activities> localActivities) {
                    Message msg = mLocalActivitiesHandler.obtainMessage();
                    Bundle args = new Bundle();
                    args.putParcelableArrayList(KEY_LOCAL_ACTIVITIES, localActivities);
                    msg.setData(args);
                    mLocalActivitiesHandler.sendMessage(msg);
                }

                @Override
                public void run() {
                    try {
                        List<Activities> localActivities = (List<Activities>) dbController.getAll();
                        passActivitiesToHandler(new ArrayList<Activities>(localActivities));
                    } catch (RuntimeException e) {
                        throw new RuntimeException();
                    }
                }
            });
            dbAccessThread.start();

            // Async callback fetch all the Activities stored on the server
            BusProvider.bus().post(new ActivityEvent.OnLoadingInitialized("", ApiRequestHandler.GET_ALL));
            // depending on SwipeRefreshLayout, display the progressDialog
            if (!mSwipeRefreshLayout.isRefreshing())
                displayProgressDialog(getString(R.string.progressDialog_fetching_activities));

        } else {
            if (mSwipeRefreshLayout.isRefreshing())
                mSwipeRefreshLayout.setRefreshing(false);
            // get List of Activities from Database
            mLocalActivitiesList = (List<Activities>) dbController.getAll();
            // important to clear all the Activities before assigning it to new list
            if (mActivitiesList != null) {
                mActivitiesList.clear();
            }
            // assign the locally received Activities from local data storage
            mActivitiesList = mLocalActivitiesList;

            if (mActivitiesList.size() > 0 && mActivitiesList != null) {
                displayActivityList();
            } else if (mActivitiesList.size() == 0) {
                // display text message
                txtView_activityListInfo.setVisibility(View.VISIBLE);
                recyclerView_activity.setVisibility(View.GONE);
                mRelativeLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.lightText));
            }
        }
    }

    /**
     * Displays Progress Dialog
     */
    private void displayProgressDialog(String message) {
        if (mProgressDialog != null) {
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
        }
    }

    /**
     * Subscribes to event of success in loading list of {@link Activities} from Server. Combines all the Activities on the
     * server as well as those stored locally.
     *
     * @param onLoaded
     */
    @Subscribe
    public void onActivitiesLoadedSuccess(ActivityEvent.OnListLoaded onLoaded) {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing())
                mSwipeRefreshLayout.setRefreshing(false);

            // get all Response Activities from server
            mResponseActivitiesList = onLoaded.getResponseList();

            for (int i = 0; i < mResponseActivitiesList.size(); i++) {
                mResponseActivitiesList.get(i).setIsOnCloud(true);
            }

            // list already fetched from the Database in new thread
            for (int i = 0; i < mLocalActivitiesList.size(); i++) {
                mLocalActivitiesList.get(i).setIsOnCloud(false);
            }

            // merge two lists to produce a super list of local as well as remotely stored Activities.
            mResponseActivitiesList.addAll(mLocalActivitiesList);

            // important to clear all the Activities before assigning it to new list
            if (mActivitiesList != null) {
                mActivitiesList.clear();
            }

            // create a standard Activities list
            mActivitiesList = mResponseActivitiesList;

            // display List if everything is OK
            if (mActivitiesList.size() > 0 && mActivitiesList != null) {
                displayActivityList();
            } else if (mActivitiesList.size() == 0) {
                // display text message
                txtView_activityListInfo.setVisibility(View.VISIBLE);
                recyclerView_activity.setVisibility(View.GONE);
                mRelativeLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.lightText));
            }
        } catch (NullPointerException npe) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "onActivitiesLoadedSuccess: ", npe);
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "onActivitiesLoadedSuccess: ", e);
        }
    }

    /**
     * Subscribes to event of failure of loading list of {@link Activities} from server.
     *
     * @param onLoadingError
     */
    @Subscribe
    public void onActivitiesLoadedFailure(ActivityEvent.OnLoadingError onLoadingError) {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    /**
     * Helper method to initialize and display list of activities in RecyclerView. In addition, this method provides lazy loading
     * of data for better performance and interface.
     */
    private void displayActivityList() {
        //  FAB margin needed for animation
        fabMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);

        // set the List of Activities to Adapter.
        activitiesListAdapter = new ActivitiesListAdapter(mActivitiesList, mContext);
        activitiesListAdapter.notifyDataSetChanged();
        if (recyclerView_activity != null) {

            recyclerView_activity.setAdapter(activitiesListAdapter);

            // touch listener when the user clicks on the Activity in the List.
            recyclerView_activity.addOnItemTouchListener(new RecyclerItemClickListener(mContext, recyclerView_activity,
                    new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            // call the interface callback to listen to the item click event
                            mInterface.onActivitiesListItemClickListener(mActivitiesList.get(position));
                        }

                        @Override
                        public void onItemLongClick(View view, int position) {

                        }
                    }));

            recyclerView_activity.addOnScrollListener(new MyRecyclerScroll() {
                @Override
                public void show() {
                    fab_addActivity.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                }

                @Override
                public void hide() {
                    fab_addActivity.animate().translationY(fab_addActivity.getHeight() + fabMargin).setInterpolator
                            (new AccelerateInterpolator(2)).start();
                }
            });

            // finally apply the animation
            fab_addActivity.startAnimation(animation);

        }
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
    public void onStop() {
        super.onStop();
        BusProvider.bus().unregister(this);
        mLocalActivitiesList = null;
        mResponseActivitiesList = null;
        mActivitiesList = null;
        dbController.closeConnection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
        activitiesListAdapter = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mInterface = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (mActivitiesList != null && mActivitiesList.size() > 0) {
            inflater.inflate(R.menu.menu_activity_list, menu);
            final MenuItem item = menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
            if (searchView != null) {
                searchView.setOnQueryTextListener(this);
            } else {
                Log.i(TAG, "search is null");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload:
                uploadActivities();
                return true;
        }

        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * Helper method to filter the List of Activities on search text change in this Fragment.
     *
     * @param serverActivitiesList
     * @param query
     * @return
     */
    private List<Activities> filter(List<Activities> activitiesList, String query) {
        query = query.toLowerCase();
        final List<Activities> filteredActivitiesList = new ArrayList<>();
        if (activitiesList != null && activitiesList.size() > 0) {
            for (Activities activity : activitiesList) {
                // perform the search on Activity name
                final String text = activity.getActivityName().toLowerCase();
                if (text.contains(query)) {
                    filteredActivitiesList.add(activity);
                }
            }
        }
        return filteredActivitiesList;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (mActivitiesList != null && mActivitiesList.size() > 0) {
            final List<Activities> filteredActivitiesList = filter(mActivitiesList, query);
            activitiesListAdapter.animateTo(filteredActivitiesList);
            recyclerView_activity.scrollToPosition(0);
            return true;
        }
        return false;
    }


    /**
     * Upload all the {@link Activities} stored on the local database to the server
     */
    private void uploadActivities() {

        if (AppUtils.isConnectedOnline(mContext)) {
            if (mLocalActivitiesList != null) {
                mUploadAlertDialog.showUploadAlertDialog(mLocalActivitiesList.size());
            } else {
                mUploadAlertDialog.showUploadAlertDialog(-1);
            }
        } else {
            Toast.makeText(mContext, "No connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onUploadClick(long resultCode) {

        if (resultCode == UploadAlertDialog.RESULT_OK) {
            mInterface.onUploadListOfActivities();
        } else if (resultCode == UploadAlertDialog.RESULT_INVALID) {

        } else if (resultCode == UploadAlertDialog.RESULT_NO_DATA) {
        }
    }


    /**
     * Callback interface to handle callback methods for the ActivityListFragment fragment.
     */
    public interface IActivityCallBacks {
        /**
         * Callback method to handle the Item click event of activities list in {@link ActivityListFragment} Fragment.
         */
        public void onActivitiesListItemClickListener(Activities mActivity);

        /**
         * Callback method to handle the click event of the Add Button in {@link ActivityListFragment} Fragment.
         */
        public void onActivityAddListener();

        /**
         * Callback to upload list of {@link Activities} on the server
         */
        public void onUploadListOfActivities();
    }

}
