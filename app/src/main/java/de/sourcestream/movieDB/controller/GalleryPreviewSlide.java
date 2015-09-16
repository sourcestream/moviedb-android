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
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.sourcestream.movieDB.MainActivity;
import de.sourcestream.movieDB.R;
import de.sourcestream.movieDB.adapter.GalleryPreviewSlideAdapter;

/**
 * This fragment creates our ViewPager and holds GalleryPreviewDetail for each image loaded.
 */
public class GalleryPreviewSlide extends Fragment {
    private MainActivity activity;
    private ArrayList<String> galleryList;
    private View rootView;
    private int mUIFlag = View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

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

        rootView = inflater.inflate(R.layout.gallerypreview, container, false);
        activity = ((MainActivity) getActivity());
        if (this.getArguments() != null)
            galleryList = this.getArguments().getStringArrayList("galleryList");
        return rootView;
    }

    /**
     * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned,
     * but before any saved state has been restored in to the view.
     *
     * @param view               The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        GalleryPreviewSlideAdapter galleryPreviewSlideAdapter = new GalleryPreviewSlideAdapter(getFragmentManager(), getResources(), galleryList);
        ViewPager mViewPager = (ViewPager) rootView.findViewById(R.id.galleryPager);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setAdapter(galleryPreviewSlideAdapter);
        if (this.getArguments() != null)
            mViewPager.setCurrentItem(this.getArguments().getInt("currPos"));


    }

    /**
     * @param savedInstanceState if the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (activity.getSupportActionBar() != null)
            activity.getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= 19) {
            mUIFlag ^= View.SYSTEM_UI_FLAG_IMMERSIVE;
        }
        activity.getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(activity, R.color.black));
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            activity.getWindow().getDecorView().setSystemUiVisibility(mUIFlag);
        else
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }


}
