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
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.sourcestream.movieDB.MainActivity;
import de.sourcestream.movieDB.R;
import de.sourcestream.movieDB.helper.ObservableScrollView;


public class TVDetailsOverview extends Fragment {
    private View rootView;
    private MainActivity activity;
    private TextView overview;
    private ObservableScrollView scrollView;

    public TVDetailsOverview() {

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


        rootView = inflater.inflate(R.layout.tvdetailsoverview, container, false);
        activity = ((MainActivity) getActivity());
        overview = (TextView) rootView.findViewById(R.id.overviewContent);
        scrollView = (ObservableScrollView) rootView.findViewById(R.id.tvdetailsoverview);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                View toolbarView = activity.findViewById(R.id.toolbar);
                if (toolbarView != null) {
                    int toolbarHeight = toolbarView.getHeight();
                    DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
                    int height = displayMetrics.heightPixels;
                    overview.setMinHeight(height + toolbarHeight);
                }
            }
        });

        return rootView;
    }

    /**
     * @param savedInstanceState if the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (activity.getTVDetailsOverviewBundle() != null)
            overview.setText(activity.getTVDetailsOverviewBundle().getString("overview"));

        if (scrollView != null) {
            // TouchInterceptionViewGroup should be a parent view other than ViewPager.
            // This is a workaround for the issue #117:
            // https://github.com/ksoichiro/Android-ObservableScrollView/issues/117
            scrollView.setTouchInterceptionViewGroup((ViewGroup) activity.getTvDetailsFragment().getView().findViewById(R.id.containerLayout));
            scrollView.setScrollViewCallbacks(activity.getTvDetailsFragment());
        }

    }

    public TextView getOverview() {
        return overview;
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
    }

    /**
     * Fired when fragment is destroyed.
     */
    public void onDestroyView() {
        super.onDestroyView();
        activity.setTVDetailsOverviewBundle(null);
    }


    public ObservableScrollView getScrollView() {
        return scrollView;
    }
}
