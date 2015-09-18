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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import java.util.ArrayList;

import de.sourcestream.movieDB.MainActivity;
import de.sourcestream.movieDB.R;
import de.sourcestream.movieDB.adapter.MovieAdapter;
import de.sourcestream.movieDB.helper.Scrollable;
import de.sourcestream.movieDB.model.MovieModel;

/**
 * This fragment is used in the Cast Details. It holds the credits content.
 */
public class CastDetailsCredits extends Fragment implements AdapterView.OnItemClickListener {
    private AbsListView listView;
    private ArrayList<MovieModel> moviesList;
    private MovieAdapter movieAdapter;
    private MainActivity activity;
    private MovieDetails movieDetails;
    private TVDetails tvDetails;

    public CastDetailsCredits() {

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

        View rootView = inflater.inflate(R.layout.castdetailscredits, container, false);
        listView = (AbsListView) rootView.findViewById(R.id.castdetailscredits);
        activity = ((MainActivity) getActivity());
        return rootView;
    }

    /**
     * @param savedInstanceState if the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (listView != null) {
            listView.setOnItemClickListener(this);
            ((Scrollable) listView).setScrollViewCallbacks(activity.getCastDetailsFragment());
            ((Scrollable) listView).setTouchInterceptionViewGroup((ViewGroup) activity.getCastDetailsFragment().getView().findViewById(R.id.containerLayout));
            Bundle save = activity.getCastDetailsCreditsBundle();
            if (save != null) {
                moviesList = save.getParcelableArrayList("moviesList");
                movieAdapter = new MovieAdapter(getActivity(), R.layout.castdetailscredits_row, this.moviesList);
                listView.setAdapter(movieAdapter);
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
        activity.getCastDetailsFragment().showInstantToolbar();
        activity.setCastDetailsFragment(null);
        activity.setRestoreMovieDetailsAdapterState(true);
        activity.setRestoreMovieDetailsState(false);

        if (moviesList.get(position).getMediaType().equals("movie")) {
            if (activity.getMovieDetailsFragment() != null && activity.getLastVisitedMovieInCredits() == moviesList.get(position).getId() && activity.getMovieDetailsFragment().getTimeOut() == 0) {
                // Old movie details retrieve info and re-init component else crash
                activity.getMovieDetailsFragment().onSaveInstanceState(new Bundle());
                Bundle bundle = new Bundle();
                bundle.putInt("id", moviesList.get(position).getId());
                Bundle save = activity.getMovieDetailsFragment().getSave();
                // Re-init movie details and set save information
                movieDetails = new MovieDetails();
                movieDetails.setTimeOut(0);
                movieDetails.setSave(save);
                movieDetails.setArguments(bundle);
            } else movieDetails = new MovieDetails();
        }

        if (moviesList.get(position).getMediaType().equals("tv")) {
            if (activity.getTvDetailsFragment() != null && activity.getLastVisitedMovieInCredits() == moviesList.get(position).getId() && activity.getTvDetailsFragment().getTimeOut() == 0) {
                // Old movie details retrieve info and re-init component else crash
                activity.getTvDetailsFragment().onSaveInstanceState(new Bundle());
                Bundle bundle = new Bundle();
                bundle.putInt("id", moviesList.get(position).getId());
                Bundle save = activity.getTvDetailsFragment().getSave();
                // Re-init movie details and set save information
                tvDetails = new TVDetails();
                tvDetails.setTimeOut(0);
                tvDetails.setSave(save);
                tvDetails.setArguments(bundle);
            } else tvDetails = new TVDetails();
        }


        activity.setLastVisitedMovieInCredits(moviesList.get(position).getId());
        ((CastDetails) getParentFragment()).setAddToBackStack(true);
        getParentFragment().onSaveInstanceState(new Bundle());
        if (activity.getSearchViewCount())
            activity.incSearchCastDetails();


        FragmentManager manager = getActivity().getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt("id", moviesList.get(position).getId());

        if (moviesList.get(position).getMediaType().equals("movie")) {
            movieDetails.setTitle(moviesList.get(position).getTitle());
            movieDetails.setArguments(bundle);
            transaction.replace(R.id.frame_container, movieDetails);
        }
        if (moviesList.get(position).getMediaType().equals("tv")) {
            tvDetails.setTitle(moviesList.get(position).getTitle());
            tvDetails.setArguments(bundle);
            transaction.replace(R.id.frame_container, tvDetails);
        }
        // add the current transaction to the back stack:
        transaction.addToBackStack("castDetails");
        transaction.commit();

    }

    /**
     * Sets the adapter of the movie List.
     * We call this method from CastDetails (parent fragment).
     *
     * @param moviesList list data
     */
    public void setAdapter(ArrayList<MovieModel> moviesList) {
        this.moviesList = moviesList;
        movieAdapter = new MovieAdapter(getActivity(), R.layout.castdetailscredits_row, this.moviesList);
        listView.setAdapter(movieAdapter);
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
        activity.setCastDetailsCreditsBundle(null);
        listView.setAdapter(null);
    }

    public AbsListView getListView() {
        return listView;
    }

    public ArrayList<MovieModel> getMoviesList() {
        return moviesList;
    }

    public boolean canScroll() {
        if (isAdded()) {
            int last = listView.getLastVisiblePosition();
            if (listView.getChildAt(last) != null) {
                if (last == listView.getCount() - 1 && listView.getChildAt(last).getBottom() <= (listView.getHeight() + (87.5 * getResources().getDisplayMetrics().density))) {
                    // It fits!
                    return false;
                } else {
                    // It doesn't fit...
                    return true;
                }
            }
        }// if getChildAt(last) is null this means we changed on different view and yes we can scroll
        return true;
    }

}
