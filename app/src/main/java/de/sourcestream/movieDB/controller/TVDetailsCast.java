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
import android.widget.AdapterView;

import java.util.ArrayList;

import de.sourcestream.movieDB.MainActivity;
import de.sourcestream.movieDB.R;
import de.sourcestream.movieDB.adapter.CastAdapter;
import de.sourcestream.movieDB.helper.ObservableListView;
import de.sourcestream.movieDB.model.CastModel;


public class TVDetailsCast extends Fragment implements AdapterView.OnItemClickListener {
    private ObservableListView listView;
    private CastAdapter castAdapter;
    private ArrayList<CastModel> castList;
    private MainActivity activity;
    private int lastVisitedPerson;
    private CastDetails castDetails;

    public TVDetailsCast() {

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

        View rootView = inflater.inflate(R.layout.moviedetailscast, container, false);
        listView = (ObservableListView) rootView.findViewById(R.id.castList);
        activity = ((MainActivity) getActivity());
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (listView != null) {
            listView.setScrollViewCallbacks(activity.getTvDetailsFragment());
            listView.setTouchInterceptionViewGroup((ViewGroup) activity.getTvDetailsFragment().getView().findViewById(R.id.containerLayout));
            listView.setOnItemClickListener(this);
            Bundle save = activity.getTVDetailsCastBundle();
            if (save != null) {
                castList = save.getParcelableArrayList("castList");
                castAdapter = new CastAdapter(getActivity(), R.layout.castrow, this.castList);
                listView.setAdapter(castAdapter);
                lastVisitedPerson = save.getInt("lastVisitedPerson");
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
        activity.getTvDetailsFragment().showInstantToolbar();
        activity.setTvDetailsFragment(null);
        activity.setRestoreMovieDetailsAdapterState(true);
        activity.setRestoreMovieDetailsState(false);
        if (activity.getCastDetailsFragment() != null && lastVisitedPerson == castList.get(position).getId() && activity.getCastDetailsFragment().getTimeOut() == 0) {
            // Old movie details retrieve info and re-init component else crash
            activity.getCastDetailsFragment().onSaveInstanceState(new Bundle());
            Bundle bundle = new Bundle();
            bundle.putInt("id", castList.get(position).getId());
            Bundle save = activity.getCastDetailsFragment().getSave();
            // Re-init movie details and set save information
            castDetails = new CastDetails();
            castDetails.setTimeOut(0);
            castDetails.setSave(save);
            castDetails.setArguments(bundle);
        } else castDetails = new CastDetails();

        lastVisitedPerson = castList.get(position).getId();
        ((TVDetails) getParentFragment()).setAddToBackStack(true);
        getParentFragment().onSaveInstanceState(new Bundle());
        if (activity.getSearchViewCount())
            activity.incSearchTvDetails();
        castDetails.setTitle(castList.get(position).getName());
        FragmentManager manager = getActivity().getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt("id", castList.get(position).getId());
        castDetails.setArguments(bundle);
        transaction.replace(R.id.frame_container, castDetails);
        // add the current transaction to the back stack:
        transaction.addToBackStack("movieDetails");
        transaction.commit();

    }

    /**
     * Sets the adapter of the cast list.
     * We call this method from TVDetails (parent fragment).
     *
     * @param castList list data
     */
    public void setAdapter(ArrayList<CastModel> castList) {
        this.castList = castList;
        castAdapter = new CastAdapter(getActivity(), R.layout.castrow, this.castList);
        listView.setAdapter(castAdapter);
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
     * Return the value of the last visited person.
     * We use this to check if it the current id is the same as the last visited.
     * This way we prevent new request to the server.
     */
    public int getLastVisitedPerson() {
        return lastVisitedPerson;
    }

    /**
     * Fired when fragment is destroyed.
     */
    public void onDestroyView() {
        super.onDestroyView();
        activity.setTVDetailsCastBundle(null);
        listView.setAdapter(null);
    }

    public ObservableListView getListView() {
        return listView;
    }

    public ArrayList<CastModel> getCastList() {
        return castList;
    }

    public boolean canScroll() {
        if (isAdded()) {
            int last = listView.getLastVisiblePosition();
            if (listView.getChildAt(last) != null) {
                if (last == listView.getCount() - 1 && listView.getChildAt(last).getBottom() <= (listView.getHeight() + (63 * getResources().getDisplayMetrics().density))) {
                    // It fits!
                    return false;
                } else {
                    // It doesn't fit...
                    return true;
                }
            }
        } // if getChildAt(last) is null this means we changed on different view and yes we can scroll
        return true;
    }
}
