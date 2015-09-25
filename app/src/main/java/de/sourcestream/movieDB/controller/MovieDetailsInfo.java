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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

import de.sourcestream.movieDB.MainActivity;
import de.sourcestream.movieDB.R;
import de.sourcestream.movieDB.adapter.SimilarAdapter;
import de.sourcestream.movieDB.model.SimilarModel;
import de.sourcestream.movieDB.view.ObservableParallaxScrollView;


public class MovieDetailsInfo extends Fragment implements AdapterView.OnItemClickListener {
    private MainActivity activity;
    private View rootView;
    private ImageView backDropPath;
    private int backDropCheck;
    private TextView titleText;
    private TextView releaseDate;
    private ImageView posterPath;
    private TextView tagline;
    private TextView statusText;
    private TextView runtime;
    private TextView genres;
    private TextView countries;
    private TextView companies;
    private RatingBar ratingBar;
    private TextView voteCount;
    private CircledImageView moreIcon;
    private CircledImageView homeIcon;
    private CircledImageView galleryIcon;
    private CircledImageView trailerIcon;
    private GridView movieDetailsSimilarGrid;
    private ArrayList<SimilarModel> similarList;
    private View similarHolder;
    private ObservableParallaxScrollView scrollView;
    private MovieDetails movieDetails = new MovieDetails();

    public MovieDetailsInfo() {

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


        rootView = inflater.inflate(R.layout.moviedetailsinfo, container, false);
        activity = ((MainActivity) getActivity());
        backDropPath = (ImageView) rootView.findViewById(R.id.backDropPath);


        titleText = (TextView) rootView.findViewById(R.id.title);
        releaseDate = (TextView) rootView.findViewById(R.id.releaseDate);
        posterPath = (ImageView) rootView.findViewById(R.id.posterPath);
        tagline = (TextView) rootView.findViewById(R.id.tagline);
        statusText = (TextView) rootView.findViewById(R.id.status);
        runtime = (TextView) rootView.findViewById(R.id.runtime);
        genres = (TextView) rootView.findViewById(R.id.genres);
        countries = (TextView) rootView.findViewById(R.id.countries);
        companies = (TextView) rootView.findViewById(R.id.companies);
        ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);
        voteCount = (TextView) rootView.findViewById(R.id.voteCount);

        homeIcon = (CircledImageView) rootView.findViewById(R.id.homeIcon);
        homeIcon.setVisibility(View.INVISIBLE);
        homeIcon.bringToFront();

        galleryIcon = (CircledImageView) rootView.findViewById(R.id.galleryIcon);
        galleryIcon.setVisibility(View.INVISIBLE);
        galleryIcon.bringToFront();

        trailerIcon = (CircledImageView) rootView.findViewById(R.id.trailerIcon);
        trailerIcon.setVisibility(View.INVISIBLE);
        trailerIcon.bringToFront();

        // Highest Z-index has to be declared last
        moreIcon = (CircledImageView) rootView.findViewById(R.id.moreIcon);
        moreIcon.bringToFront();

        movieDetailsSimilarGrid = (GridView) rootView.findViewById(R.id.movieDetailsSimilarGrid);
        similarHolder = rootView.findViewById(R.id.similarHolder);
        scrollView = (ObservableParallaxScrollView) rootView.findViewById(R.id.moviedetailsinfo);
        View detailsLayout = rootView.findViewById(R.id.detailsLayout);
        ViewCompat.setElevation(detailsLayout, 2 * getResources().getDisplayMetrics().density);
        ViewCompat.setElevation(moreIcon, 2 * getResources().getDisplayMetrics().density);
        ViewCompat.setElevation(homeIcon, 2 * getResources().getDisplayMetrics().density);
        ViewCompat.setElevation(galleryIcon, 2 * getResources().getDisplayMetrics().density);
        ViewCompat.setElevation(trailerIcon, 2 * getResources().getDisplayMetrics().density);
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
        if (activity.getMovieDetailsFragment() != null) {
            moreIcon.setOnClickListener(activity.getMovieDetailsFragment().getOnMoreIconClick());
            activity.getMovieDetailsFragment().getOnMoreIconClick().setKey(false);
        }


        if (activity.getMovieDetailsInfoBundle() != null)
            onOrientationChange(activity.getMovieDetailsInfoBundle());

        if (scrollView != null) {
            // TouchInterceptionViewGroup should be a parent view other than ViewPager.
            // This is a workaround for the issue #117:
            // https://github.com/ksoichiro/Android-ObservableScrollView/issues/117
            scrollView.setTouchInterceptionViewGroup((ViewGroup) activity.getMovieDetailsFragment().getView().findViewById(R.id.containerLayout));
            scrollView.setScrollViewCallbacks(activity.getMovieDetailsFragment());
        }
    }

    public TextView getTitleText() {
        return titleText;
    }

    public TextView getReleaseDate() {
        return releaseDate;
    }

    public ImageView getPosterPath() {
        return posterPath;
    }

    public TextView getStatusText() {
        return statusText;
    }

    public TextView getTagline() {
        return tagline;
    }


    public TextView getRuntime() {
        return runtime;
    }

    public TextView getGenres() {
        return genres;
    }

    public TextView getCountries() {
        return countries;
    }

    public TextView getCompanies() {
        return companies;
    }

    public RatingBar getRatingBar() {
        return ratingBar;
    }

    public TextView getVoteCount() {
        return voteCount;
    }

    public ImageView getBackDropPath() {
        return backDropPath;
    }

    public int getBackDropCheck() {
        return backDropCheck;
    }

    public void setBackDropCheck(int backDropCheck) {
        this.backDropCheck = backDropCheck;
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

    public CircledImageView getTrailerIcon() {
        return trailerIcon;
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
        // BackDrop path
        backDropCheck = args.getInt("backDropCheck");
        if (backDropCheck == 0) {
            activity.setBackDropImage(backDropPath, args.getString("backDropUrl"));
            backDropPath.setTag(args.getString("backDropUrl"));
        }

        // Title
        activity.setText(titleText, args.getString("titleText"));

        // Release date
        activity.setText(releaseDate, args.getString("releaseDate"));

        // Status
        activity.setText(statusText, args.getString("status"));

        // Tag line
        if (!args.getString("tagline").isEmpty())
            tagline.setText(args.getString("tagline"));
        else
            activity.hideTextView(tagline);

        // RunTime
        if (!args.getString("runTime").isEmpty())
            activity.setText(runtime, args.getString("runTime"));
        else activity.hideView(runtime);

        // Genres
        if (!args.getString("genres").isEmpty())
            activity.setText(genres, args.getString("genres"));
        else activity.hideView(genres);

        // Production Countries
        if (!args.getString("productionCountries").isEmpty())
            activity.setText(countries, args.getString("productionCountries"));
        else activity.hideView(countries);

        // Production Companies
        if (!args.getString("productionCompanies").isEmpty()) {
            activity.setText(companies, args.getString("productionCompanies"));
            if (args.getString("productionCountries").isEmpty()) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) companies.getLayoutParams();
                lp.setMargins(0, (int) (28 * getResources().getDisplayMetrics().density), 0, 0);
            }
        } else activity.hideView(companies);


        // Poster path
        if (args.getString("posterPathURL") != null) {
            activity.setImage(posterPath, args.getString("posterPathURL"));
            activity.setImageTag(posterPath, args.getString("posterPathURL"));
        }


        // Rating
        if (args.getString("voteCount").isEmpty()) {
            activity.hideRatingBar(ratingBar);
            activity.hideTextView(voteCount);
        } else {
            ratingBar.setRating(args.getFloat("rating"));
            activity.setText(voteCount, args.getString("voteCount"));
        }

        // Similar list
        similarList = args.getParcelableArrayList("similarList");
        if (similarList != null && similarList.size() > 0)
            setSimilarList(similarList);
        else
            activity.hideView(similarHolder);


    }

    /**
     * Fired when fragment is destroyed.
     */
    public void onDestroyView() {
        super.onDestroyView();
        activity.setMovieDetailsInfoBundle(null);
        posterPath.setImageDrawable(null);
        backDropPath.setImageDrawable(null);
        movieDetailsSimilarGrid.setAdapter(null);
    }

    public void setSimilarList(ArrayList<SimilarModel> similarList) {
        this.similarList = similarList;
        SimilarAdapter similarAdapter = new SimilarAdapter(getActivity(), R.layout.similar_row, similarList);
        movieDetailsSimilarGrid.setAdapter(similarAdapter);
        movieDetailsSimilarGrid.setOnItemClickListener(this);

        if (similarList.size() < 4) {
            ViewGroup.LayoutParams lp = movieDetailsSimilarGrid.getLayoutParams();
            lp.height /= 2;
        }
    }

    public ArrayList<SimilarModel> getSimilarList() {
        return similarList;
    }

    public View getSimilarHolder() {
        return similarHolder;
    }


    public ObservableParallaxScrollView getScrollView() {
        return scrollView;
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

        activity.setRestoreMovieDetailsAdapterState(true);
        activity.setRestoreMovieDetailsState(false);
        if (activity.getMovieDetailsSimFragment() != null && activity.getLastVisitedSimMovie() == similarList.get(position).getId() && activity.getMovieDetailsSimFragment().getTimeOut() == 0) {
            // Old movie details retrieve info and re-init component else crash
            activity.getMovieDetailsSimFragment().onSaveInstanceState(new Bundle());
            Bundle bundle = new Bundle();
            bundle.putInt("id", similarList.get(position).getId());
            Bundle save = activity.getMovieDetailsSimFragment().getSave();
            // Re-init movie details and set save information
            movieDetails = new MovieDetails();
            movieDetails.setTimeOut(0);
            movieDetails.setSave(save);
            movieDetails.setArguments(bundle);
        } else movieDetails = new MovieDetails();

        activity.setLastVisitedSimMovie(similarList.get(position).getId());
        activity.getMovieDetailsFragment().setAddToBackStack(true);
        activity.getMovieDetailsFragment().onSaveInstanceState(new Bundle());
        if (activity.getSearchViewCount())
            activity.incSearchMovieDetails();

        activity.setMovieDetailsFragment(null);
        activity.setSaveInMovieDetailsSimFragment(true);
        movieDetails.setTitle(similarList.get(position).getTitle());
        FragmentManager manager = getActivity().getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt("id", similarList.get(position).getId());
        movieDetails.setArguments(bundle);
        transaction.replace(R.id.frame_container, movieDetails);
        // add the current transaction to the back stack:
        transaction.addToBackStack("similarDetails");
        transaction.commit();


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
}
