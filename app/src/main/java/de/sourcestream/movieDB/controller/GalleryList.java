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
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

import de.sourcestream.movieDB.MainActivity;
import de.sourcestream.movieDB.MovieDB;
import de.sourcestream.movieDB.R;
import de.sourcestream.movieDB.adapter.GalleryAdapter;
import de.sourcestream.movieDB.model.GalleryModel;

/**
 * This fragment is used in the gallery view.
 */
public class GalleryList extends Fragment implements AdapterView.OnItemClickListener {
    private MainActivity activity;
    private AbsListView listView;
    private String title;
    private GalleryPreviewSlide galleryPreview;
    private ArrayList<String> galleryPath;
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


        View rootView = inflater.inflate(R.layout.gallerylist, container, false);
        activity = ((MainActivity) getActivity());
        galleryPreview = new GalleryPreviewSlide();
        activity.getMDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        listView = (AbsListView) rootView.findViewById(R.id.gridView);
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
        activity.getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(activity, R.color.background_material_light));

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (activity.getSupportActionBar() != null)
                    activity.getSupportActionBar().show();
            }
        });

        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        if (Build.VERSION.SDK_INT >= 19)
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);


        listView.setOnItemClickListener(this);

        setGallery();
        System.gc();

        Tracker t = ((MovieDB) activity.getApplication()).getTracker();
        t.setScreenName("GalleryList - " + getTitle());
        t.send(new HitBuilders.ScreenViewBuilder().build());
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
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle args = new Bundle();
        args.putStringArrayList("galleryList", galleryPath);
        args.putInt("currPos", position);
        galleryPreview.setArguments(args);
        transaction.replace(R.id.frame_container, galleryPreview);
        // add the current transaction to the back stack:
        transaction.addToBackStack("galleryList");
        transaction.commit();

    }

    /**
     * Method which sets our gallery list content. We pass the data from our MovieDetails, CastDetails or TVDetails fragment.
     */
    private void setGallery() {
        galleryPath = this.getArguments().getStringArrayList("galleryList");

        ArrayList<GalleryModel> galleryList = new ArrayList<>();
        GalleryAdapter galleryAdapter = new GalleryAdapter(getActivity(), R.layout.galleryview_row, galleryList);
        listView.setAdapter(galleryAdapter);

        for (int i = 0; i < galleryPath.size(); i++) {
            GalleryModel gallery = new GalleryModel();
            gallery.setFilePath(MovieDB.imageUrl + getResources().getString(R.string.galleryImgSize) + galleryPath.get(i));
            galleryList.add(gallery);
        }

        galleryAdapter.notifyDataSetChanged();
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
