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

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import de.sourcestream.movieDB.adapter.GenresAdapter;
import de.sourcestream.movieDB.model.GenresModel;

/**
 * Genres list.
 */
public class GenresList extends Fragment implements AdapterView.OnItemClickListener {
    private MainActivity activity;
    private ProgressBar spinner;
    private AbsListView listView;
    private ArrayList<GenresModel> genresList;
    private GenresAdapter genresAdapter;
    private MovieList movieList;
    private int backState;
    private View rootView;

    private HttpURLConnection conn;
    private Bundle save;

    public GenresList() {
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        rootView = inflater.inflate(R.layout.genreslist, container, false);
        activity = ((MainActivity) getActivity());
        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar);
        listView = (AbsListView) rootView.findViewById(R.id.genresList);

        Tracker t = ((MovieDB) activity.getApplication()).getTracker();
        t.setScreenName("Genres");
        t.send(new HitBuilders.ScreenViewBuilder().build());

        return rootView;
    }

    /**
     * @param savedInstanceState if the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        movieList = new MovieList();

        if (save != null) {
            backState = save.getInt("backState");
            if (backState == 1) {
                genresList = save.getParcelableArrayList("listData");
                genresAdapter = new GenresAdapter(getActivity(), R.layout.genresrow, genresList);
            }
        }
        if (backState == 0) {
            updateList();
        } else {
            listView.setAdapter(genresAdapter);
        }

        getActivity().setTitle(getResources().getString(R.string.genresTitle));
        listView.setOnItemClickListener(this);
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
        if (movieList.getCurrentList().equals("genre/" + genresList.get(position).getId() + "/movies"))
            movieList.setBackState(1);
        else {
            movieList.setCurrentList("genre/" + genresList.get(position).getId() + "/movies");
            movieList.setBackState(0);
        }
        movieList.setTitle(genresList.get(position).getName());
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle args = new Bundle();
        args.putString("currentList", "genresList");
        movieList.setArguments(args);
        transaction.replace(R.id.frame_container, movieList);
        // add the current transaction to the back stack:
        transaction.addToBackStack("genresList");
        transaction.commit();
        backState = 1;
    }

    /**
     * This class handles the connection to our backend server.
     * If the connection is successful we set the list data.
     */
    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activity.showView(spinner);
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

                    JSONObject jsonData = new JSONObject(sb.toString());
                    JSONArray genresArray = jsonData.getJSONArray("genres");

                    for (int i = 0; i < genresArray.length(); i++) {
                        JSONObject object = genresArray.getJSONObject(i);
                        GenresModel genre = new GenresModel();
                        genre.setName(object.getString("name"));
                        genre.setId(object.getInt("id"));
                        genresList.add(genre);
                    }

                    return true;
                }


            } catch (IOException | JSONException e) {
                if (conn != null)
                    conn.disconnect();
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            activity.hideView(spinner);

            if (!result) {
                if (getResources() != null) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.noConnection), Toast.LENGTH_LONG).show();
                    backState = 0;
                }
            } else {
                genresAdapter.notifyDataSetChanged();
                backState = 1;
            }
        }
    }

    /**
     * Fired from the main activity. Makes a new request to the server.
     * Sets list, adapter, timeout.
     */
    public void updateList() {
        if (getActivity() != null) {
            listView = (AbsListView) rootView.findViewById(R.id.genresList);
            genresList = new ArrayList<>();
            genresAdapter = new GenresAdapter(getActivity(), R.layout.genresrow, genresList);
            listView.setAdapter(genresAdapter);
            final JSONAsyncTask request = new JSONAsyncTask();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        request.execute(MovieDB.url + "genre/movie/list?&api_key=" + MovieDB.key).get(10000, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException | ExecutionException | InterruptedException e) {
                        request.cancel(true);
                        // we abort the http request, else it will cause problems and slow connection later
                        if (conn != null)
                            conn.disconnect();
                        if (spinner != null)
                            activity.hideView(spinner);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        backState = 0;
                    }
                }
            }).start();
        }
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
            send.putInt("backState", backState);
            if (backState == 1) {
                send.putParcelableArrayList("listData", genresList);
                // used to restore the scroll listener variables
                // Save scroll position
                if (listView != null) {
                    Parcelable listState = listView.onSaveInstanceState();
                    send.putParcelable("listViewScroll", listState);
                }

            }
            outState.putBundle("save", send);

        }
    }

    /**
     * If our connection was successful we want to save our list data,
     * so when we click back it will be retained.
     */
    public int getBackState() {
        return backState;
    }

    public MovieList getMovieListView() {
        return movieList;
    }


    /**
     * Set empty adapter to free memory when this fragment is inactive
     */
    public void onDestroyView() {
        super.onDestroyView();
    }
}
