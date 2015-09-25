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
import android.support.v4.view.ViewCompat;
import android.support.wearable.view.CircledImageView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.sourcestream.movieDB.MainActivity;
import de.sourcestream.movieDB.R;
import de.sourcestream.movieDB.adapter.SimilarAdapter;
import de.sourcestream.movieDB.model.SimilarModel;
import de.sourcestream.movieDB.view.ObservableParallaxScrollView;

/**
 * This fragment is used in the Cast Details. It holds the information content.
 */
public class CastDetailsInfo extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {
    private MainActivity activity;
    private View rootView;
    private TextView name;
    private ImageView profilePath;
    private TextView birthInfo;
    private TextView alsoKnownAs;
    private CircledImageView moreIcon;
    private CircledImageView homeIcon;
    private CircledImageView galleryIcon;
    private ObservableParallaxScrollView scrollView;
    private GridView castDetailsKnownGrid;
    private ArrayList<SimilarModel> knownList;
    private View knownHolder;
    private MovieDetails movieDetails;
    private TVDetails tvDetails;
    private Button showMoreButton;

    public CastDetailsInfo() {

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


        rootView = inflater.inflate(R.layout.castdetailsinfo, container, false);
        activity = ((MainActivity) getActivity());
        name = (TextView) rootView.findViewById(R.id.name);
        profilePath = (ImageView) rootView.findViewById(R.id.profilePath);
        birthInfo = (TextView) rootView.findViewById(R.id.birthInfo);
        alsoKnownAs = (TextView) rootView.findViewById(R.id.alsoKnownAs);

        homeIcon = (CircledImageView) rootView.findViewById(R.id.homeIcon);
        homeIcon.setVisibility(View.INVISIBLE);
        homeIcon.bringToFront();

        galleryIcon = (CircledImageView) rootView.findViewById(R.id.galleryIcon);
        galleryIcon.setVisibility(View.INVISIBLE);
        galleryIcon.bringToFront();

        moreIcon = (CircledImageView) rootView.findViewById(R.id.moreIcon);
        moreIcon.bringToFront();
        scrollView = (ObservableParallaxScrollView) rootView.findViewById(R.id.castdetailsinfo);

        castDetailsKnownGrid = (GridView) rootView.findViewById(R.id.castDetailsKnownGrid);
        knownHolder = rootView.findViewById(R.id.knownHolder);
        showMoreButton = (Button) rootView.findViewById(R.id.showMoreButton);
        showMoreButton.setOnClickListener(this);
        View detailsLayout = rootView.findViewById(R.id.detailsLayout);
        ViewCompat.setElevation(detailsLayout, 2 * getResources().getDisplayMetrics().density);
        ViewCompat.setElevation(moreIcon, 2 * getResources().getDisplayMetrics().density);
        ViewCompat.setElevation(homeIcon, 2 * getResources().getDisplayMetrics().density);
        ViewCompat.setElevation(galleryIcon, 2 * getResources().getDisplayMetrics().density);
        // Prevent event bubbling else if you touch on the details layout when the info tab is scrolled it will open gallery view
        detailsLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (activity.getCastDetailsFragment() != null) {
            moreIcon.setOnClickListener(activity.getCastDetailsFragment().getOnMoreIconClick());
            activity.getCastDetailsFragment().getOnMoreIconClick().setKey(false);
        }

        if (activity.getCastDetailsInfoBundle() != null)
            onOrientationChange(activity.getCastDetailsInfoBundle());

        if (scrollView != null) {
            // TouchInterceptionViewGroup should be a parent view other than ViewPager.
            // This is a workaround for the issue #117:
            // https://github.com/ksoichiro/Android-ObservableScrollView/issues/117
            scrollView.setTouchInterceptionViewGroup((ViewGroup) activity.getCastDetailsFragment().getView().findViewById(R.id.containerLayout));
            scrollView.setScrollViewCallbacks(activity.getCastDetailsFragment());
        }

    }

    public TextView getName() {
        return name;
    }

    public ImageView getProfilePath() {
        return profilePath;
    }

    public TextView getBirthInfo() {
        return birthInfo;
    }

    public TextView getAlsoKnownAs() {
        return alsoKnownAs;
    }

    public CircledImageView getMoreIcon() {
        return moreIcon;
    }

    public CircledImageView getHomeIcon() {
        return homeIcon;
    }

    public CircledImageView getGalleryIcon() {
        return galleryIcon;
    }

    public View getRootView() {
        return rootView;
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
     * Fired when are restoring from backState or orientation has changed.
     *
     * @param args our bundle with saved state. Our parent fragment handles the saving.
     */
    @SuppressWarnings("ConstantConditions")
    private void onOrientationChange(Bundle args) {
        // Name
        name.setText(args.getString("name"));

        // Poster path
        if (args.getString("profilePathURL") != null) {
            activity.setBackDropImage(profilePath, args.getString("profilePathURL"));
            profilePath.setTag(args.getString("profilePathURL"));
        }

        // Birth info
        if (!args.getString("birthInfo").isEmpty())
            activity.setText(birthInfo, args.getString("birthInfo"));
        else activity.hideView(birthInfo);

        // Also known as
        if (!args.getString("alsoKnownAs").isEmpty()) {
            activity.setText(alsoKnownAs, args.getString("alsoKnownAs"));
        } else activity.hideView(alsoKnownAs);


        knownList = args.getParcelableArrayList("knownList");
        if (knownList != null && knownList.size() > 0)
            setKnownList(knownList);
        else
            activity.hideView(knownHolder);

    }

    /**
     * Fired when fragment is destroyed.
     */
    public void onDestroyView() {
        super.onDestroyView();
        activity.setCastDetailsInfoBundle(null);
        profilePath.setImageDrawable(null);
        castDetailsKnownGrid.setAdapter(null);
    }

    public ObservableParallaxScrollView getScrollView() {
        return scrollView;
    }

    /**
     * @return Returns true this ScrollView can be scrolled
     */
    public boolean canScroll() {
        if (isAdded()) {
            View child = scrollView.getChildAt(0);
            if (child != null) {
                int childHeight = child.getHeight();
                return (scrollView.getHeight() + (119 * getResources().getDisplayMetrics().density)) < childHeight;
            }
        }
        return false;
    }

    public void setKnownList(ArrayList<SimilarModel> similarList) {
        this.knownList = similarList;
        SimilarAdapter similarAdapter = new SimilarAdapter(getActivity(), R.layout.similar_row, similarList);
        castDetailsKnownGrid.setAdapter(similarAdapter);
        castDetailsKnownGrid.setOnItemClickListener(this);

        if (knownList.size() < 4) {
            ViewGroup.LayoutParams lp = castDetailsKnownGrid.getLayoutParams();
            lp.height /= 2;
        }
    }

    public ArrayList<SimilarModel> getKnownList() {
        return knownList;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        activity.getCastDetailsFragment().showInstantToolbar();
        activity.setCastDetailsFragment(null);
        activity.setRestoreMovieDetailsAdapterState(true);
        activity.setRestoreMovieDetailsState(false);

        if (knownList.get(position).getMediaType().equals("movie")) {
            if (activity.getMovieDetailsFragment() != null && activity.getLastVisitedMovieInCredits() == knownList.get(position).getId() && activity.getMovieDetailsFragment().getTimeOut() == 0) {
                // Old movie details retrieve info and re-init component else crash
                activity.getMovieDetailsFragment().onSaveInstanceState(new Bundle());
                Bundle bundle = new Bundle();
                bundle.putInt("id", knownList.get(position).getId());
                Bundle save = activity.getMovieDetailsFragment().getSave();
                // Re-init movie details and set save information
                movieDetails = new MovieDetails();
                movieDetails.setTimeOut(0);
                movieDetails.setSave(save);
                movieDetails.setArguments(bundle);
            } else movieDetails = new MovieDetails();
        }

        if (knownList.get(position).getMediaType().equals("tv")) {
            if (activity.getTvDetailsFragment() != null && activity.getLastVisitedMovieInCredits() == knownList.get(position).getId() && activity.getTvDetailsFragment().getTimeOut() == 0) {
                // Old movie details retrieve info and re-init component else crash
                activity.getTvDetailsFragment().onSaveInstanceState(new Bundle());
                Bundle bundle = new Bundle();
                bundle.putInt("id", knownList.get(position).getId());
                Bundle save = activity.getTvDetailsFragment().getSave();
                // Re-init movie details and set save information
                tvDetails = new TVDetails();
                tvDetails.setTimeOut(0);
                tvDetails.setSave(save);
                tvDetails.setArguments(bundle);
            } else tvDetails = new TVDetails();
        }


        activity.setLastVisitedMovieInCredits(knownList.get(position).getId());
        ((CastDetails) getParentFragment()).setAddToBackStack(true);
        getParentFragment().onSaveInstanceState(new Bundle());
        if (activity.getSearchViewCount())
            activity.incSearchCastDetails();


        FragmentManager manager = getActivity().getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt("id", knownList.get(position).getId());

        if (knownList.get(position).getMediaType().equals("movie")) {
            movieDetails.setTitle(knownList.get(position).getTitle());
            movieDetails.setArguments(bundle);
            transaction.replace(R.id.frame_container, movieDetails);
        }
        if (knownList.get(position).getMediaType().equals("tv")) {
            tvDetails.setTitle(knownList.get(position).getTitle());
            tvDetails.setArguments(bundle);
            transaction.replace(R.id.frame_container, tvDetails);
        }
        // add the current transaction to the back stack:
        transaction.addToBackStack("castDetails");
        transaction.commit();

    }

    public Button getShowMoreButton() {
        return showMoreButton;
    }

    @Override
    public void onClick(View v) {
        ((CastDetails) getParentFragment()).getmViewPager().setCurrentItem(1);
    }

    public View getKnownHolder() {
        return knownHolder;
    }

}
