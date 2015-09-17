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


public class TVDetailsInfo extends Fragment implements AdapterView.OnItemClickListener {
    private View rootView;
    private MainActivity activity;
    private ImageView backDropPath;
    private int backDropCheck;
    private TextView title;
    private ImageView posterPath;
    private TextView statusText;
    private TextView typeText;
    private TextView episodeRuntime;
    private TextView numberOfEpisodesText;
    private TextView numberOfSeasonsText;
    private TextView firstAirDateText;
    private TextView lastAirDateText;
    private TextView genres;
    private TextView countries;
    private TextView companies;
    private RatingBar ratingBar;
    private TextView voteCount;
    private CircledImageView moreIcon;
    private CircledImageView homeIcon;
    private CircledImageView galleryIcon;
    private ObservableParallaxScrollView scrollView;
    private GridView tvDetailsSimilarGrid;
    private ArrayList<SimilarModel> similarList;
    private View similarHolder;
    private TVDetails tvDetails;

    public TVDetailsInfo() {

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


        rootView = inflater.inflate(R.layout.tvdetailsinfo, container, false);
        activity = ((MainActivity) getActivity());
        backDropPath = (ImageView) rootView.findViewById(R.id.backDropPath);
        title = (TextView) rootView.findViewById(R.id.title);
        posterPath = (ImageView) rootView.findViewById(R.id.posterPath);
        statusText = (TextView) rootView.findViewById(R.id.status);
        typeText = (TextView) rootView.findViewById(R.id.type);
        episodeRuntime = (TextView) rootView.findViewById(R.id.episodeRuntime);
        numberOfEpisodesText = (TextView) rootView.findViewById(R.id.numberOfEpisodes);
        numberOfSeasonsText = (TextView) rootView.findViewById(R.id.numberOfSeasons);
        firstAirDateText = (TextView) rootView.findViewById(R.id.firstAirDate);
        lastAirDateText = (TextView) rootView.findViewById(R.id.lastAirDate);
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

        // Highest Z-index has to be declared last
        moreIcon = (CircledImageView) rootView.findViewById(R.id.moreIcon);
        moreIcon.bringToFront();
        scrollView = (ObservableParallaxScrollView) rootView.findViewById(R.id.tvdetailsinfo);

        tvDetailsSimilarGrid = (GridView) rootView.findViewById(R.id.tvDetailsSimilarGrid);
        similarHolder = rootView.findViewById(R.id.similarHolder);
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
        if (activity.getTvDetailsFragment() != null) {
            moreIcon.setOnClickListener(activity.getTvDetailsFragment().getOnMoreIconClick());
            activity.getTvDetailsFragment().getOnMoreIconClick().setKey(false);
        }

        if (activity.getTVDetailsInfoBundle() != null)
            onOrientationChange(activity.getTVDetailsInfoBundle());

        if (scrollView != null) {
            // TouchInterceptionViewGroup should be a parent view other than ViewPager.
            // This is a workaround for the issue #117:
            // https://github.com/ksoichiro/Android-ObservableScrollView/issues/117
            scrollView.setTouchInterceptionViewGroup((ViewGroup) activity.getTvDetailsFragment().getView().findViewById(R.id.containerLayout));
            scrollView.setScrollViewCallbacks(activity.getTvDetailsFragment());
        }

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

    public TextView getTitle() {
        return title;
    }

    public ImageView getPosterPath() {
        return posterPath;
    }

    public TextView getStatusText() {
        return statusText;
    }

    public TextView getTypeText() {
        return typeText;
    }

    public TextView getEpisodeRuntime() {
        return episodeRuntime;
    }

    public TextView getNumberOfEpisodesText() {
        return numberOfEpisodesText;
    }

    public TextView getNumberOfSeasonsText() {
        return numberOfSeasonsText;
    }

    public TextView getFirstAirDateText() {
        return firstAirDateText;
    }

    public TextView getLastAirDateText() {
        return lastAirDateText;
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
     * Fired when are restoring from backState or orientation has changed.
     *
     * @param outState our bundle with saved state. Our parent fragment handles the saving.
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

        // Release date and title
        activity.setTextFromHtml(title, args.getString("titleText"));

        // Status
        activity.setText(statusText, args.getString("status"));

        // Type
        if (!args.getString("typeText").isEmpty())
            activity.setText(typeText, args.getString("typeText"));
        else activity.hideView(typeText);

        // Episode runtime
        if (!args.getString("episodeRuntime").isEmpty())
            activity.setText(episodeRuntime, args.getString("episodeRuntime"));
        else activity.hideView(episodeRuntime);

        // Number of episodes
        if (!args.getString("numberOfEpisodesText").isEmpty())
            activity.setText(numberOfEpisodesText, args.getString("numberOfEpisodesText"));
        else activity.hideView(numberOfEpisodesText);

        // Number of seasons
        if (!args.getString("numberOfSeasonsText").isEmpty())
            activity.setText(numberOfSeasonsText, args.getString("numberOfSeasonsText"));
        else activity.hideView(numberOfSeasonsText);

        // First air date
        if (!args.getString("firstAirDateText").isEmpty())
            activity.setText(firstAirDateText, args.getString("firstAirDateText"));
        else activity.hideView(firstAirDateText);

        // Last air date
        if (!args.getString("lastAirDateText").isEmpty())
            activity.setText(lastAirDateText, args.getString("lastAirDateText"));
        else activity.hideView(lastAirDateText);

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
        activity.setTVDetailsInfoBundle(null);
        posterPath.setImageDrawable(null);
        backDropPath.setImageDrawable(null);
        tvDetailsSimilarGrid.setAdapter(null);
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

    public void setSimilarList(ArrayList<SimilarModel> similarList) {
        this.similarList = similarList;
        SimilarAdapter similarAdapter = new SimilarAdapter(getActivity(), R.layout.similar_row, similarList);
        tvDetailsSimilarGrid.setAdapter(similarAdapter);
        tvDetailsSimilarGrid.setOnItemClickListener(this);

        if (similarList.size() < 4) {
            ViewGroup.LayoutParams lp = tvDetailsSimilarGrid.getLayoutParams();
            lp.height /= 2;
        }
    }

    public ArrayList<SimilarModel> getSimilarList() {
        return similarList;
    }

    public View getSimilarHolder() {
        return similarHolder;
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
        if (activity.getTvDetailsSimFragment() != null && activity.getLastVisitedSimTV() == similarList.get(position).getId() && activity.getTvDetailsSimFragment().getTimeOut() == 0) {
            // Old tv details retrieve info and re-init component else crash
            activity.getTvDetailsSimFragment().onSaveInstanceState(new Bundle());
            Bundle bundle = new Bundle();
            bundle.putInt("id", similarList.get(position).getId());
            Bundle save = activity.getTvDetailsSimFragment().getSave();
            // Re-init movie details and set save information
            tvDetails = new TVDetails();
            tvDetails.setTimeOut(0);
            tvDetails.setSave(save);
            tvDetails.setArguments(bundle);
        } else tvDetails = new TVDetails();

        activity.setLastVisitedSimTV(similarList.get(position).getId());
        activity.getTvDetailsFragment().setAddToBackStack(true);
        activity.getTvDetailsFragment().onSaveInstanceState(new Bundle());
        if (activity.getSearchViewCount())
            activity.incSearchTvDetails();

        activity.setTvDetailsFragment(null);
        activity.setSaveInTVDetailsSimFragment(true);
        tvDetails.setTitle(similarList.get(position).getTitle());
        FragmentManager manager = getActivity().getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt("id", similarList.get(position).getId());
        tvDetails.setArguments(bundle);
        transaction.replace(R.id.frame_container, tvDetails);
        // add the current transaction to the back stack:
        transaction.addToBackStack("similarDetails");
        transaction.commit();


    }

}
