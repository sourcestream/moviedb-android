/*
 *  Copyright 2015 sourcestream GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.sourcestream.movieDB.controller;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.sourcestream.movieDB.MainActivity;
import de.sourcestream.movieDB.MovieDB;
import de.sourcestream.movieDB.R;
import de.sourcestream.movieDB.adapter.MovieAdapter;
import de.sourcestream.movieDB.helper.Scrollable;
import de.sourcestream.movieDB.model.MovieModel;

/**
 * Controller for the upcoming movies.
 */
public class TVList extends Fragment implements AdapterView.OnItemClickListener {

    private MainActivity activity;
    private View rootView;
    private ArrayList<MovieModel> tvList;
    private int checkLoadMore = 0;
    private int totalPages;
    private MovieAdapter movieAdapter;
    private String currentList = "";
    private int backState;
    private String title;
    private TVDetails tvDetails;
    private AbsListView listView;
    private EndlessScrollListener endlessScrollListener;
    private ProgressBar spinner;
    private Toast toastLoadingMore;
    private HttpURLConnection conn;
    private boolean isLoading;
    private Bundle save;
    private boolean fragmentActive;
    private int lastVisitedTV;
    private float scale;
    private boolean phone;
    private int minThreshold;

    public TVList() {
    }

    /**
     * Called to do initial creation of a fragment.
     * This is called after onAttach(Activity) and before onCreateView(LayoutInflater, ViewGroup, Bundle).
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            save = savedInstanceState.getBundle("save");
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           sets the layout for the current view.
     * @param container          the container which holds the current view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     *                           Return the View for the fragment's UI, or null.
     */
    @SuppressLint("ShowToast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (this.getArguments().getString("currentList") != null) {
            switch (this.getArguments().getString("currentList")) {
                case "onTheAir":
                    rootView = inflater.inflate(R.layout.tv_ontheair, container, false);
                    break;
                case "airingToday":
                    rootView = inflater.inflate(R.layout.tv_airingtoday, container, false);
                    break;
                case "popular":
                    rootView = inflater.inflate(R.layout.tv_popular, container, false);
                    break;
                case "topRated":
                    rootView = inflater.inflate(R.layout.tv_toprated, container, false);
                    break;
                default:
                    rootView = inflater.inflate(R.layout.tv_ontheair, container, false);
            }
        } else
            rootView = inflater.inflate(R.layout.movieslist, container, false);


        activity = ((MainActivity) getActivity());
        toastLoadingMore = Toast.makeText(activity, R.string.loadingMore, Toast.LENGTH_SHORT);
        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar);
        phone = getResources().getBoolean(R.bool.portrait_only);
        scale = getResources().getDisplayMetrics().density;
        if (phone)
            minThreshold = (int) (-49 * scale);
        else
            minThreshold = (int) (-42 * scale);

        Tracker t = ((MovieDB) activity.getApplication()).getTracker();
        t.setScreenName("TVList");
        t.send(new HitBuilders.ScreenViewBuilder().build());

        return rootView;
    }

    /**
     * @param savedInstanceState if the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (this.getArguments().getString("currentList") != null) {
            switch (this.getArguments().getString("currentList")) {

                case "onTheAir":
                    listView = (AbsListView) rootView.findViewById(R.id.TVOnTheAirList);
                    break;
                case "airingToday":
                    listView = (AbsListView) rootView.findViewById(R.id.TVAiringTodayList);
                    break;
                case "popular":
                    listView = (AbsListView) rootView.findViewById(R.id.TVPopularList);
                    break;
                case "topRated":
                    listView = (AbsListView) rootView.findViewById(R.id.TVTopRatedList);
                    break;
                default:
                    listView = (AbsListView) rootView.findViewById(R.id.TVOnTheAirList);
            }
        } else
            listView = (AbsListView) rootView.findViewById(R.id.TVOnTheAirList);

        //Handle orientation change starts
        if (save != null) {
            checkLoadMore = save.getInt("checkLoadMore");
            totalPages = save.getInt("totalPages");
            setCurrentList(save.getString("currentListURL"));
            setTitle(save.getString("title"));
            isLoading = save.getBoolean("isLoading");
            lastVisitedTV = save.getInt("lastVisitedTV");
            if (save.getInt("backState") == 1) {
                backState = 1;
                tvList = save.getParcelableArrayList("listData");
                movieAdapter = new MovieAdapter(getActivity(), R.layout.row, tvList);
                endlessScrollListener = new EndlessScrollListener();
                endlessScrollListener.setCurrentPage(save.getInt("currentPage"));
                endlessScrollListener.setOldCount(save.getInt("oldCount"));
                endlessScrollListener.setLoading(save.getBoolean("loading"));
            } else {
                backState = 0;
            }
        }

        if (listView != null) {

            ((Scrollable) listView).setScrollViewCallbacks(activity.getTvSlideTab());

            getActivity().setTitle(getTitle());
            listView.setOnItemClickListener(this);


            // checks if we have set arguments to load movies for a specific person.
            if (this.getArguments().getString("currentList") != null) {
                // check if we were on movie details and we pressed back, to prevent list reloading
                if (backState == 0) {
                    if (this.getArguments().getString("currentList").equals("onTheAir") && activity.getCurrentTVViewPagerPos() == 0)
                        updateList();
                    if (isLoading)
                        spinner.setVisibility(View.VISIBLE);
                } else {
                    // If memory heap is higher than 20MB we use the default viewpager offscreen logic
                    // else we remove invisible list's items to save up more RAM
                    if (MainActivity.getMaxMem() / 1048576 > 20)
                        fragmentActive = true;

                    if (fragmentActive) {
                        listView.setAdapter(movieAdapter);
                        listView.setOnScrollListener(endlessScrollListener);
                    }
                    // Maintain scroll position
                    if (save != null && save.getParcelable("listViewScroll") != null)
                        listView.onRestoreInstanceState(save.getParcelable("listViewScroll"));
                }
            }

        }
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has been clicked.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        activity.resetMovieDetailsBundle();
        activity.setRestoreMovieDetailsAdapterState(true);
        activity.setRestoreMovieDetailsState(false);
        activity.setOrientationChanged(false);
        activity.resetCastDetailsBundle();
        activity.resetTvDetailsBundle();
        if (tvDetails != null && lastVisitedTV == tvList.get(position).getId() && tvDetails.getTimeOut() == 0) {
            // Old movie details retrieve info and re-init component else crash
            tvDetails.onSaveInstanceState(new Bundle());
            Bundle bundle = new Bundle();
            bundle.putInt("id", tvList.get(position).getId());
            Bundle save = tvDetails.getSave();
            tvDetails = new TVDetails();
            tvDetails.setTimeOut(0);
            tvDetails.setSave(save);
            tvDetails.setArguments(bundle);
        } else tvDetails = new TVDetails();

        lastVisitedTV = tvList.get(position).getId();
        tvDetails.setTitle(tvList.get(position).getTitle());
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt("id", tvList.get(position).getId());
        tvDetails.setArguments(bundle);
        transaction.replace(R.id.frame_container, tvDetails);
        // add the current transaction to the back stack:
        transaction.addToBackStack("TVList");
        transaction.commit();
        fragmentActive = true;
        activity.getTvSlideTab().showInstantToolbar();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * This class handles the connection to our backend server.
     * If the connection is successful we set our list data.
     */
    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (checkLoadMore == 0) {
                activity.showView(spinner);
                isLoading = true;
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        toastLoadingMore.show();
                    }
                });
            }

        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(10000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();

                int status = conn.getResponseCode();
                if (status == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();

                    JSONObject movieData = new JSONObject(sb.toString());
                    totalPages = movieData.getInt("total_pages");
                    JSONArray movieArray = movieData.getJSONArray("results");

                    // is added checks if we are still on the same view, if we don't do this check the program will crash
                    if (isAdded()) {
                        for (int i = 0; i < movieArray.length(); i++) {
                            JSONObject object = movieArray.getJSONObject(i);

                            MovieModel movie = new MovieModel();
                            movie.setId(object.getInt("id"));
                            movie.setTitle(object.getString("name"));
                            if (!object.getString("first_air_date").equals("null") && !object.getString("first_air_date").isEmpty())
                                movie.setReleaseDate(object.getString("first_air_date"));


                            if (!object.getString("poster_path").equals("null") && !object.getString("poster_path").isEmpty())
                                movie.setPosterPath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + object.getString("poster_path"));


                            tvList.add(movie);
                        }

                        return true;
                    }

                }


            } catch (ParseException | IOException | JSONException e) {
                if (conn != null)
                    conn.disconnect();
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // is added checks if we are still on the same view, if we don't do this check the program will cra
            if (isAdded()) {
                if (checkLoadMore == 0) {
                    activity.hideView(spinner);
                    isLoading = false;
                }

                if (!result) {
                    Toast.makeText(getActivity(), R.string.noConnection, Toast.LENGTH_LONG).show();
                    backState = 0;
                } else {
                    movieAdapter.notifyDataSetChanged();
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            toastLoadingMore.cancel();
                        }
                    });
                    final View toolbarView = activity.findViewById(R.id.toolbar);
                    listView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (toolbarView.getTranslationY() == -toolbarView.getHeight() && ((Scrollable) listView).getCurrentScrollY() < minThreshold) {
                                if (phone)
                                    listView.smoothScrollBy((int) (56 * scale), 0);
                                else
                                    listView.smoothScrollBy((int) (59 * scale), 0);
                            }
                        }
                    });
                    backState = 1;
                    save = null;
                }
            }
        }

    }

    /**
     * This class listens for scroll events on the list.
     */
    public class EndlessScrollListener implements AbsListView.OnScrollListener {

        private int currentPage = 1;
        private boolean loading = false;
        private int oldCount = 0;

        public EndlessScrollListener() {
        }


        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

            if (oldCount != totalItemCount && firstVisibleItem + visibleItemCount >= totalItemCount) {
                loading = true;
                oldCount = totalItemCount;
            }
            if (loading) {
                if (currentPage != totalPages) {
                    currentPage++;
                    checkLoadMore = 1;
                    loading = false;
                    final JSONAsyncTask request = new JSONAsyncTask();
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                request.execute(MovieDB.url + getCurrentList() + "?&api_key=" + MovieDB.key + "&page=" + currentPage).get(10000, TimeUnit.MILLISECONDS);
                            } catch (TimeoutException | ExecutionException | InterruptedException e) {
                                request.cancel(true);
                                // we abort the http request, else it will cause problems and slow connection later
                                if (conn != null)
                                    conn.disconnect();
                                toastLoadingMore.cancel();
                                currentPage--;
                                loading = true;
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getActivity(), getResources().getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }
                    }).start();
                } else {
                    if (totalPages != 1) {
                        Toast.makeText(getActivity(), R.string.nomoreresults, Toast.LENGTH_SHORT).show();
                    }
                    loading = false;

                }
            }


        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getOldCount() {
            return oldCount;
        }

        public void setOldCount(int oldCount) {
            this.oldCount = oldCount;
        }

        public boolean getLoading() {
            return loading;
        }

        public void setLoading(boolean loading) {
            this.loading = loading;
        }
    }

    public void setCurrentList(String currentList) {
        this.currentList = currentList;
    }

    public String getCurrentList() {
        return currentList;
    }

    public int getBackState() {
        return backState;
    }

    /**
     * Fired when list is empty and we should update it.
     */
    public void updateList() {
        if (listView != null) {
            tvList = new ArrayList<>();
            movieAdapter = new MovieAdapter(getActivity(), R.layout.row, tvList);
            listView.setAdapter(movieAdapter);
            endlessScrollListener = new EndlessScrollListener();
            listView.setOnScrollListener(endlessScrollListener);
            checkLoadMore = 0;
            final JSONAsyncTask request = new JSONAsyncTask();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        if (!isLoading)
                            request.execute(MovieDB.url + getCurrentList() + "?&api_key=" + MovieDB.key).get(10000, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException | ExecutionException | InterruptedException e) {
                        request.cancel(true);
                        toastLoadingMore.cancel();
                        if (spinner != null)
                            activity.hideView(spinner);
                        // we abort the http request, else it will cause problems and slow connection later
                        if (conn != null)
                            conn.disconnect();
                        isLoading = false;
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }).start();
        }
    }

    /**
     * Update the title. We use this method to save our title and then to set it on the Toolbar.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the title.
     */
    private String getTitle() {
        return this.title;
    }

    /**
     * Called to ask the fragment to save its current dynamic state,
     * so it can later be reconstructed in a new instance of its process is restarted.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Used to avoid bug where we add item in the back stack
        // and if we change orientation twice the item from the back stack has null values
        if (save != null)
            outState.putBundle("save", save);
        else {
            Bundle send = new Bundle();
            send.putInt("checkLoadMore", checkLoadMore);
            send.putInt("totalPages", totalPages);
            send.putString("currentListURL", getCurrentList());
            send.putString("title", getTitle());
            send.putBoolean("isLoading", isLoading);
            send.putInt("lastVisitedTV", lastVisitedTV);
            if (backState == 1) {
                send.putInt("backState", 1);
                send.putParcelableArrayList("listData", tvList);
                // used to restore the scroll listener variables
                send.putInt("currentPage", endlessScrollListener.getCurrentPage());
                send.putInt("oldCount", endlessScrollListener.getOldCount());
                send.putBoolean("loading", endlessScrollListener.getLoading());
                // Save scroll position
                if (listView != null) {
                    Parcelable listState = listView.onSaveInstanceState();
                    send.putParcelable("listViewScroll", listState);
                }
            } else
                send.putInt("backState", 0);

            outState.putBundle("save", send);
        }
    }

    /**
     * This method is used if the device has low heap size <=20 MB.
     * Using this method we manage to have only one active list view at a time.
     */
    public void cleanUp() {
        // WE clean unused lists to save up RAM if the heap size is less or equal to 20MB
        if (MainActivity.getMaxMem() / 1048576 <= 20) {
            if (tvList != null) {
                listView.setAdapter(null);
            }
        }
    }

    public void setFragmentActive(boolean fragmentActive) {
        this.fragmentActive = fragmentActive;
    }

    /**
     * Used if the device has low heap size <=20 MB.
     */
    public void setAdapter() {
        if (listView != null && listView.getAdapter() == null)
            listView.setAdapter(movieAdapter);
    }

    /**
     * Returns the list with data.
     * Used when you click on search icon to get this list and set it there if the search list is empty.
     */
    public ArrayList<MovieModel> getTVList() {
        return tvList;
    }

    public AbsListView getListView() {
        return listView;
    }

    /**
     * Fired when fragment is destroyed.
     */
    public void onDestroyView() {
        super.onDestroyView();
    }
}