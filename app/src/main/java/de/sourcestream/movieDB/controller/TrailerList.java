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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

import de.sourcestream.movieDB.MainActivity;
import de.sourcestream.movieDB.MovieDB;
import de.sourcestream.movieDB.R;
import de.sourcestream.movieDB.adapter.TrailerAdapter;
import de.sourcestream.movieDB.model.TrailerModel;

/**
 * This fragment is used in the trailer view.
 */
public class TrailerList extends Fragment implements AdapterView.OnItemClickListener {
    private MainActivity activity;
    private AbsListView listView;
    private String title;
    private ArrayList<String> trailerPath;
    private Bundle save;

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


        View rootView;
        rootView = inflater.inflate(R.layout.gallerylist, container, false);
        listView = (AbsListView) rootView.findViewById(R.id.gridView);
        activity = ((MainActivity) getActivity());

        Tracker t = ((MovieDB) activity.getApplication()).getTracker();
        t.setScreenName("TrailerList - " + getTitle());
        t.send(new HitBuilders.ScreenViewBuilder().build());

        return rootView;
    }

    /**
     * @param savedInstanceState if the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.isVisible()) {
            // Check orientation and lock to portrait if we are on phone
            if (getResources().getBoolean(R.bool.portrait_only)) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
        if (save != null)
            setTitle(save.getString("title"));
        activity.setTitle(getTitle());
        listView.setOnItemClickListener(this);

        setTrailer();
        System.gc();
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
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MovieDB.youtube + trailerPath.get(position))));
    }

    /**
     * Method which sets our trailer list content. We pass the data from our MovieDetails fragment.
     */
    private void setTrailer() {
        trailerPath = this.getArguments().getStringArrayList("trailerList");

        ArrayList<TrailerModel> trailerList = new ArrayList<>();
        TrailerAdapter trailerAdapter = new TrailerAdapter(getActivity(), R.layout.trailerview_row, trailerList);
        listView.setAdapter(trailerAdapter);

        for (int i = 0; i < trailerPath.size(); i++) {
            TrailerModel trailer = new TrailerModel();
            trailer.setFilePath(trailerPath.get(i));
            trailerList.add(trailer);
        }

        trailerAdapter.notifyDataSetChanged();

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
            send.putString("title", getTitle());

            outState.putBundle("save", send);
        }
    }

    /**
     * Set empty adapter to free memory when this fragment is inactive
     */
    public void onDestroyView() {
        super.onDestroyView();
        listView.setAdapter(null);
    }

}
