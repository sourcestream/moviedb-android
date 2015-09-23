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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.wearable.view.CircledImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.sourcestream.movieDB.MainActivity;
import de.sourcestream.movieDB.MovieDB;
import de.sourcestream.movieDB.R;
import de.sourcestream.movieDB.adapter.TVDetailsSlideAdapter;
import de.sourcestream.movieDB.helper.ObservableScrollViewCallbacks;
import de.sourcestream.movieDB.helper.ScrollState;
import de.sourcestream.movieDB.helper.Scrollable;
import de.sourcestream.movieDB.model.CastModel;
import de.sourcestream.movieDB.model.SeasonModel;
import de.sourcestream.movieDB.model.SimilarModel;
import de.sourcestream.movieDB.view.MovieDetailsSlidingTabLayout;
import de.sourcestream.movieDB.view.ObservableParallaxScrollView;


public class TVDetails extends Fragment implements ObservableScrollViewCallbacks {

    private MainActivity activity;
    private View rootView;
    private int currentId;
    private int timeOut;
    private HttpURLConnection conn;
    private String title;
    private Bundle save;


    private ArrayList<SeasonModel> seasonList;
    private ArrayList<CastModel> castList;

    private ProgressBar spinner;
    private int moreIconCheck;
    private CircledImageView moreIcon;
    private CircledImageView homeIcon;
    private int homeIconCheck;
    private int galleryIconCheck;
    private CircledImageView galleryIcon;
    private ArrayList<String> galleryList;

    private onGalleryIconClick onGalleryIconClick;
    private onMoreIconClick onMoreIconClick;
    private onHomeIconClick onHomeIconClick;


    private MovieDetailsSlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    private TVDetailsSlideAdapter tvDetailsSlideAdapter;
    private onPageChangeSelected onPageChangeSelected;
    private TranslateAnimation downAnimation;
    private DownAnimationListener downAnimationListener;
    private TranslateAnimation upAnimation;
    private UpAnimationListener upAnimationListener;
    private TranslateAnimation iconUpAnimation;
    private IconUpAnimationListener iconUpAnimationListener;
    private TranslateAnimation iconDownAnimation;
    private IconDownAnimationListener iconDownAnimationListener;
    private TVDetailsInfo tvDetailsInfo;
    private TVDetailsCast tvDetailsCast;
    private TVDetailsOverview tvDetailsOverview;
    private int tvDetailsInfoScrollY;
    private boolean addToBackStack;
    private String homeIconUrl;
    private float oldScrollY;
    private float dy;
    private float upDy;
    private float downDy;
    private float downDyTrans;
    private boolean upDyKey;
    private boolean downDyKey;
    private float scrollSpeed = 2.2F;
    private int currPos;
    private boolean infoTabScrollPosUpdated;
    private JSONAsyncTask request;
    private int iconMarginConstant;
    private int iconMarginLandscape;
    private int iconConstantSpecialCase;
    private int twoIcons;
    private int twoIconsToolbar;
    private int oneIcon;
    private int oneIconToolbar;
    private float scale;
    private boolean phone;
    private int hideThreshold;
    private int minThreshold;
    private int iconDirection;
    private boolean noCast;

    public TVDetails() {
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

        activity = ((MainActivity) getActivity());
        onGalleryIconClick = new onGalleryIconClick();
        onMoreIconClick = new onMoreIconClick();
        onHomeIconClick = new onHomeIconClick();
        onPageChangeSelected = new onPageChangeSelected();
        downAnimationListener = new DownAnimationListener();
        upAnimationListener = new UpAnimationListener();
        iconUpAnimationListener = new IconUpAnimationListener();
        iconDownAnimationListener = new IconDownAnimationListener();
        phone = getResources().getBoolean(R.bool.portrait_only);
        scale = getResources().getDisplayMetrics().density;
        if (phone) {
            hideThreshold = (int) (-105 * scale);
            minThreshold = (int) (-49 * scale);
        } else {
            hideThreshold = (int) (-100 * scale);
            minThreshold = (int) (-42 * scale);
        }

        if (currentId != this.getArguments().getInt("id") || this.timeOut == 1) {
            rootView = inflater.inflate(R.layout.tvdetails, container, false);
            spinner = (ProgressBar) rootView.findViewById(R.id.progressBar);

            homeIcon = (CircledImageView) rootView.findViewById(R.id.homeIcon);
            homeIcon.bringToFront();
            homeIcon.setVisibility(View.INVISIBLE);
            galleryIcon = (CircledImageView) rootView.findViewById(R.id.galleryIcon);
            galleryIcon.bringToFront();
            galleryIcon.setVisibility(View.INVISIBLE);

            // Highest Z-index has to be declared last
            moreIcon = (CircledImageView) rootView.findViewById(R.id.moreIcon);
            moreIcon.bringToFront();
        }
        moreIcon.setOnClickListener(onMoreIconClick);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (activity.getTvDetailsBundle().size() > 0 && activity.getRestoreMovieDetailsState()) {
            save = activity.getTvDetailsBundle().get(activity.getTvDetailsBundle().size() - 1);
            activity.removeTvDetailsBundle(activity.getTvDetailsBundle().size() - 1);
            if (activity.getSearchViewCount())
                activity.decSearchTvDetails();
            activity.setRestoreMovieDetailsState(false);
        }
        if (save != null && save.getInt("timeOut") == 1)
            activity.setRestoreMovieDetailsAdapterState(true);
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        tvDetailsSlideAdapter = new TVDetailsSlideAdapter(getChildFragmentManager(), getResources(), activity);
        if (mViewPager != null)
            currPos = mViewPager.getCurrentItem();
        mViewPager = (ViewPager) rootView.findViewById(R.id.tvDetailsPager);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(tvDetailsSlideAdapter);
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (MovieDetailsSlidingTabLayout) rootView.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(activity, R.color.tabSelected));
        mSlidingTabLayout.bringToFront();
    }


    /**
     * @param savedInstanceState if the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (save != null) {
            setTitle(save.getString("title"));
            currentId = save.getInt("currentId");
            timeOut = save.getInt("timeOut");
            if (timeOut == 0) {
                spinner.setVisibility(View.GONE);
                onOrientationChange(save);
            }
        }

        if (currentId != this.getArguments().getInt("id") || this.timeOut == 1) {
            currentId = this.getArguments().getInt("id");
            moreIcon.setVisibility(View.INVISIBLE);
            mSlidingTabLayout.setVisibility(View.INVISIBLE);
            mViewPager.setVisibility(View.INVISIBLE);
            spinner.setVisibility(View.VISIBLE);

            request = new JSONAsyncTask();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        request.execute(MovieDB.url + "tv/" + currentId + "?append_to_response=images,credits,similar&api_key=" + MovieDB.key).get(10000, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException | ExecutionException | InterruptedException | CancellationException e) {
                        request.cancel(true);
                        // we abort the http request, else it will cause problems and slow connection later
                        if (conn != null)
                            conn.disconnect();
                        if (spinner != null)
                            activity.hideView(spinner);
                        if (mViewPager != null)
                            activity.hideLayout(mViewPager);
                        if (getActivity() != null && !(e instanceof CancellationException)) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        setTimeOut(1);
                    }
                }
            }).start();
        }
        activity.setTitle(getTitle());
        activity.setTvDetailsFragment(this);
        if (activity.getSaveInTVDetailsSimFragment()) {
            activity.setSaveInTVDetailsSimFragment(false);
            activity.setTvDetailsSimFragment(this);
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                tvDetailsInfo = (TVDetailsInfo) tvDetailsSlideAdapter.getRegisteredFragment(0);
                tvDetailsCast = (TVDetailsCast) tvDetailsSlideAdapter.getRegisteredFragment(1);
                tvDetailsOverview = (TVDetailsOverview) tvDetailsSlideAdapter.getRegisteredFragment(2);
            }
        });

        showInstantToolbar();

        iconMarginConstant = activity.getIconMarginConstant();
        iconMarginLandscape = activity.getIconMarginLandscape();
        iconConstantSpecialCase = activity.getIconConstantSpecialCase();
        twoIcons = activity.getTwoIcons();
        twoIconsToolbar = activity.getTwoIconsToolbar();
        oneIcon = activity.getOneIcon();
        oneIconToolbar = activity.getOneIconToolbar();

        Tracker t = ((MovieDB) activity.getApplication()).getTracker();
        t.setScreenName("TVDetails - " + getTitle());
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    /**
     * This class handles the connection to our backend server.
     * If the connection is successful we set information on our views.
     */
    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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

                    // is added checks if we are still on the same view, if we don't do this check the program will crash
                    while (tvDetailsInfo == null) {
                        Thread.sleep(200);
                    }
                    if (isAdded() && tvDetailsInfo != null) {
                        // Backdrop path
                        if (!jsonData.getString("backdrop_path").equals("null") && !jsonData.getString("backdrop_path").isEmpty()) {
                            activity.setBackDropImage(tvDetailsInfo.getBackDropPath(), jsonData.getString("backdrop_path"));
                            activity.setImageTag(tvDetailsInfo.getBackDropPath(), jsonData.getString("backdrop_path"));
                        } else if (!jsonData.getString("poster_path").equals("null") && !jsonData.getString("poster_path").isEmpty()) {
                            activity.setBackDropImage(tvDetailsInfo.getBackDropPath(), jsonData.getString("poster_path"));
                            activity.setImageTag(tvDetailsInfo.getBackDropPath(), jsonData.getString("poster_path"));
                        } else
                            tvDetailsInfo.setBackDropCheck(1);

                        // Title
                        activity.setText(tvDetailsInfo.getTitle(), jsonData.getString("name"));


                        // Poster path
                        if (!jsonData.getString("poster_path").equals("null") && !jsonData.getString("poster_path").isEmpty()) {
                            activity.setImage(tvDetailsInfo.getPosterPath(), jsonData.getString("poster_path"));
                            activity.setImageTag(tvDetailsInfo.getPosterPath(), jsonData.getString("poster_path"));
                        }


                        // Status
                        if (!jsonData.getString("status").equals("null") && !jsonData.getString("status").isEmpty())
                            activity.setText(tvDetailsInfo.getStatusText(), jsonData.getString("status"));
                        else
                            activity.hideTextView(tvDetailsInfo.getStatusText());

                        // Type
                        if (!jsonData.getString("type").equals("null") && !jsonData.getString("type").isEmpty())
                            activity.setText(tvDetailsInfo.getTypeText(), getResources().getString(R.string.type) + " " + jsonData.getString("type"));
                        else
                            activity.hideTextView(tvDetailsInfo.getTypeText());


                        // Episode runTime
                        JSONArray episodeRunTimeArray = jsonData.getJSONArray("episode_run_time");
                        String episodeRunTimeData = "";

                        if (episodeRunTimeArray.length() == 1)
                            episodeRunTimeData = episodeRunTimeArray.getString(0) + " " + getResources().getString(R.string.min);

                        if (episodeRunTimeArray.length() == 2)
                            episodeRunTimeData = episodeRunTimeArray.getString(0) + " - " + episodeRunTimeArray.getString(1) + " " + getResources().getString(R.string.min);

                        if (episodeRunTimeArray.length() > 2) {
                            for (int i = 0; i < episodeRunTimeArray.length(); i++) {
                                if (i + 1 == episodeRunTimeArray.length())
                                    episodeRunTimeData += episodeRunTimeArray.getString(i) + " " + getResources().getString(R.string.min);
                                else
                                    episodeRunTimeData += episodeRunTimeArray.getString(i) + ", ";
                            }
                        }

                        if (episodeRunTimeData.isEmpty())
                            activity.hideTextView(tvDetailsInfo.getEpisodeRuntime());
                        else
                            activity.setText(tvDetailsInfo.getEpisodeRuntime(), episodeRunTimeData);


                        // Number of episodes
                        if (!jsonData.getString("number_of_episodes").equals("null") && !jsonData.getString("number_of_episodes").isEmpty())
                            activity.setText(tvDetailsInfo.getNumberOfEpisodesText(), getResources().getString(R.string.numberOfEpisodes) + " " + jsonData.getString("number_of_episodes"));
                        else
                            activity.hideTextView(tvDetailsInfo.getNumberOfEpisodesText());

                        // Number of seasons
                        if (!jsonData.getString("number_of_seasons").equals("null") && !jsonData.getString("number_of_seasons").isEmpty())
                            activity.setText(tvDetailsInfo.getNumberOfSeasonsText(), getResources().getString(R.string.numberOfSeasons) + " " + jsonData.getString("number_of_seasons"));
                        else
                            activity.hideTextView(tvDetailsInfo.getNumberOfSeasonsText());

                        // First air date
                        if (!jsonData.getString("first_air_date").equals("null") && !jsonData.getString("first_air_date").isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            try {
                                Date date = sdf.parse(jsonData.getString("first_air_date"));
                                String formattedDate = activity.getDateFormat().format(date);
                                activity.setText(tvDetailsInfo.getFirstAirDateText(), getResources().getString(R.string.firstAirDate) + " " + formattedDate);
                            } catch (java.text.ParseException e) {
                                activity.hideTextView(tvDetailsInfo.getFirstAirDateText());
                            }
                        } else
                            activity.hideTextView(tvDetailsInfo.getFirstAirDateText());

                        // Last air date
                        if (!jsonData.getString("last_air_date").equals("null") && !jsonData.getString("last_air_date").isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            try {
                                Date date = sdf.parse(jsonData.getString("last_air_date"));
                                String formattedDate = activity.getDateFormat().format(date);
                                activity.setText(tvDetailsInfo.getLastAirDateText(), getResources().getString(R.string.lastAirDate) + " " + formattedDate);
                            } catch (java.text.ParseException e) {
                                activity.hideTextView(tvDetailsInfo.getLastAirDateText());
                            }
                        } else
                            activity.hideTextView(tvDetailsInfo.getLastAirDateText());


                        // Homepage icon
                        if (!jsonData.getString("homepage").isEmpty() && !jsonData.getString("homepage").equals("null")) {
                            homeIconUrl = jsonData.getString("homepage");
                            homeIconCheck = 0;
                        } else {
                            activity.invisibleView(homeIcon);
                            homeIconCheck = 1;
                        }

                        // Gallery
                        JSONObject galleryObject = jsonData.getJSONObject("images");
                        JSONArray galleryBackdropsArray = galleryObject.getJSONArray("backdrops");
                        JSONArray galleryPosterArray = galleryObject.getJSONArray("posters");
                        galleryList = new ArrayList<>();
                        if (galleryPosterArray.length() > 0 || galleryBackdropsArray.length() > 0) {
                            for (int i = 0; i < galleryBackdropsArray.length(); i++) {
                                JSONObject object = galleryBackdropsArray.getJSONObject(i);
                                galleryList.add(object.getString("file_path"));
                            }
                            for (int i = 0; i < galleryPosterArray.length(); i++) {
                                JSONObject object = galleryPosterArray.getJSONObject(i);
                                galleryList.add(object.getString("file_path"));
                            }
                            galleryIconCheck = 0;
                        } else {
                            activity.invisibleView(galleryIcon);
                            galleryIconCheck = 1;
                        }

                        // Genres
                        JSONArray genresArray = jsonData.getJSONArray("genres");
                        String genresData = "";
                        for (int i = 0; i < genresArray.length(); i++) {
                            if (i + 1 == genresArray.length())
                                genresData += genresArray.getJSONObject(i).get("name");
                            else
                                genresData += genresArray.getJSONObject(i).get("name") + ", ";
                        }

                        if (genresData.isEmpty())
                            activity.hideTextView(tvDetailsInfo.getGenres());
                        else {
                            activity.setText(tvDetailsInfo.getGenres(), genresData);
                        }

                        // Origin countries
                        JSONArray countriesArray = jsonData.getJSONArray("origin_country");
                        String countriesData = "";
                        for (int i = 0; i < countriesArray.length(); i++) {
                            if (i + 1 == countriesArray.length())
                                countriesData += countriesArray.getString(i);
                            else
                                countriesData += countriesArray.getString(i) + "\n";
                        }

                        if (countriesData.isEmpty())
                            activity.hideTextView(tvDetailsInfo.getCountries());
                        else {
                            activity.setText(tvDetailsInfo.getCountries(), countriesData);
                        }

                        // Production companies
                        JSONArray companiesArray = jsonData.getJSONArray("production_companies");
                        String companiesData = "";
                        for (int i = 0; i < companiesArray.length(); i++) {
                            if (i + 1 == companiesArray.length())
                                companiesData += companiesArray.getJSONObject(i).get("name");
                            else
                                companiesData += companiesArray.getJSONObject(i).get("name") + "\n";
                        }

                        if (companiesData.isEmpty())
                            activity.hideTextView(tvDetailsInfo.getCompanies());
                        else {
                            activity.setText(tvDetailsInfo.getCompanies(), companiesData);
                            // if countries is empty we need to set the margin on companies
                            if (countriesData.isEmpty()) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) tvDetailsInfo.getCompanies().getLayoutParams();
                                        lp.setMargins(0, (int) (28 * scale), 0, 0);
                                    }
                                });
                            }
                        }


                        // Rating
                        if (Float.parseFloat(jsonData.getString("vote_average")) == 0.0f) {
                            activity.hideRatingBar(tvDetailsInfo.getRatingBar());
                            activity.hideTextView(tvDetailsInfo.getVoteCount());
                        } else {
                            activity.setRatingBarValue(tvDetailsInfo.getRatingBar(), (Float.parseFloat(jsonData.getString("vote_average")) / 2));
                            activity.setText(tvDetailsInfo.getVoteCount(), jsonData.getString("vote_count") + " " + getString(R.string.voteCount));
                        }

                   /* //Season info
                    JSONArray seasonArray = jsonData.getJSONArray("seasons");
                    seasonList = new ArrayList<>();
                    for (int i = 0; i < seasonArray.length(); i++) {
                        JSONObject object = seasonArray.getJSONObject(i);

                        SeasonModel season = new SeasonModel();
                        season.setId(object.getInt("id"));

                        if (!object.getString("season_number").equals("null") && !object.getString("season_number").isEmpty())
                            season.setTitle(jsonData.getString("original_name") + " " + getResources().getString(R.string.season) + " " + object.getString("season_number"));
                        else
                            season.setTitle(jsonData.getString("original_name"));

                        if (!object.getString("episode_count").equals("null") && !object.getString("episode_count").isEmpty())
                            season.setEpisodeCount(getResources().getString(R.string.episodes) + " " + object.getString("episode_count"));

                        if (!object.getString("air_date").equals("null") && !object.getString("air_date").isEmpty())
                            season.setAirDate(getResources().getString(R.string.airDate) + " " + object.getString("air_date"));

                        if (!object.getString("poster_path").equals("null") && !object.getString("poster_path").isEmpty())
                            season.setPosterPath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + object.getString("poster_path"));

                        seasonList.add(season);
                    }*/

                        //Cast info
                        JSONObject casts = jsonData.getJSONObject("credits");
                        JSONArray castsArray = casts.getJSONArray("cast");
                        castList = new ArrayList<>();
                        for (int i = 0; i < castsArray.length(); i++) {
                            JSONObject object = castsArray.getJSONObject(i);

                            CastModel cast = new CastModel();
                            cast.setId(object.getInt("id"));
                            cast.setName(object.getString("name"));
                            cast.setCharacter(object.getString("character"));
                            if (!object.getString("profile_path").equals("null") && !object.getString("profile_path").isEmpty())
                                cast.setProfilePath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + object.getString("profile_path"));

                            castList.add(cast);
                        }

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (castList.size() == 0) {
                                    noCast = true;
                                    mSlidingTabLayout.disableTabClickListener(1);
                                }

                                // Cast
                                if (isAdded())
                                    tvDetailsCast.setAdapter(castList);
                            }
                        });


                        //Overview
                        final String overview = jsonData.getString("overview");

                        if (!overview.equals("null") && !overview.isEmpty())
                            activity.setText(tvDetailsOverview.getOverview(), overview);
                        else
                            activity.setText(tvDetailsOverview.getOverview(), getResources().getString(R.string.noOverview));


                        // Similar
                        JSONObject similarObj = jsonData.getJSONObject("similar");
                        JSONArray similarArray = similarObj.getJSONArray("results");
                        int similarLen = similarArray.length();
                        if (similarLen > 6)
                            similarLen = 6;

                        if (similarLen == 0)
                            activity.hideView(tvDetailsInfo.getSimilarHolder());
                        else {
                            final ArrayList<SimilarModel> similarList = new ArrayList<>();

                            for (int i = 0; i < similarLen; i++) {
                                JSONObject object = similarArray.getJSONObject(i);

                                SimilarModel similarModel = new SimilarModel();
                                similarModel.setId(object.getInt("id"));
                                similarModel.setTitle(object.getString("name"));
                                if (!object.getString("poster_path").equals("null") && !object.getString("poster_path").isEmpty())
                                    similarModel.setPosterPath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + object.getString("poster_path"));
                                if (!object.getString("first_air_date").equals("null") && !object.getString("first_air_date").isEmpty())
                                    similarModel.setReleaseDate(object.getString("first_air_date"));

                                similarList.add(similarModel);
                            }

                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (isAdded())
                                        tvDetailsInfo.setSimilarList(similarList);
                                }
                            });
                        }


                        return true;
                    }
                }


            } catch (ParseException | IOException | JSONException e) {
                if (conn != null)
                    conn.disconnect();
            } catch (InterruptedException e) {
                
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
            return false;
        }

        /**
         * Fired after doInBackground() has finished
         *
         * @param result true if connection is successful, false if connection has failed.
         */
        protected void onPostExecute(Boolean result) {
            // is added checks if we are still on the same view, if we don't do this check the program will cra
            if (isAdded()) {
                if (!result) {
                    Toast.makeText(getActivity(), R.string.noConnection, Toast.LENGTH_LONG).show();
                    setTimeOut(1);
                    spinner.setVisibility(View.GONE);
                    mViewPager.setVisibility(View.GONE);
                } else {
                    setTimeOut(0);
                    currPos = 0;
                    mViewPager.setCurrentItem(0);
                    spinner.setVisibility(View.GONE);
                    mSlidingTabLayout.setVisibility(View.VISIBLE);
                    mViewPager.setVisibility(View.VISIBLE);
                    mSlidingTabLayout.setOnPageChangeListener(onPageChangeSelected);
                    if (homeIconCheck == 1 && galleryIconCheck == 1) {
                        moreIconCheck = 1;
                        activity.hideView(moreIcon);
                        activity.hideView(tvDetailsInfo.getMoreIcon());
                    } else {
                        moreIconCheck = 0;
                        activity.showView(tvDetailsInfo.getMoreIcon());
                        // set listener on backdrop and poster path click to open gallery
                        if (galleryIconCheck == 0) {
                            tvDetailsInfo.getBackDropPath().setOnClickListener(onGalleryIconClick);
                            tvDetailsInfo.getPosterPath().setOnClickListener(onGalleryIconClick);
                        }
                        adjustIconsPos(homeIcon, galleryIcon);
                        adjustIconsPos(tvDetailsInfo.getHomeIcon(), tvDetailsInfo.getGalleryIcon());
                    }

                }
            } else setTimeOut(1);
        }
    }

    /**
     * We use this key to know if the user has tried to open this movie and the connection failed.
     * So if he tries to load again the same movie we know that the connection has failed and we need to make a new request.
     */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    /**
     * Updates the timeOut value.
     */
    public int getTimeOut() {
        return timeOut;
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
        if (save != null && save.getInt("timeOut") == 1)
            save = null;
        save = null;

        if (save != null) {
            if (tvDetailsCast != null)
                save.putInt("lastVisitedPerson", tvDetailsCast.getLastVisitedPerson());
            outState.putBundle("save", save);
            if (addToBackStack) {
                activity.addTvDetailsBundle(save);
                addToBackStack = false;
            }
        } else {
            Bundle send = new Bundle();
            send.putInt("currentId", currentId);
            if (request != null && request.getStatus() == AsyncTask.Status.RUNNING) {
                timeOut = 1;
                request.cancel(true);
            }
            send.putInt("timeOut", timeOut);
            send.putString("title", title);
            if (timeOut == 0) {
                // HomePage
                send.putInt("homeIconCheck", homeIconCheck);
                if (homeIconCheck == 0)
                    send.putString("homepage", homeIconUrl);

                // Gallery icon
                send.putInt("galleryIconCheck", galleryIconCheck);
                if (galleryIconCheck == 0)
                    send.putStringArrayList("galleryList", galleryList);

                // More icon
                send.putInt("moreIconCheck", moreIconCheck);
                // used to pass the data to the castList view
                send.putParcelableArrayList("seasonList", seasonList);

            }

            // TV details info begins here
            if (tvDetailsInfo != null) {
                // Backdrop path
                send.putInt("backDropCheck", tvDetailsInfo.getBackDropCheck());
                if (tvDetailsInfo.getBackDropCheck() == 0 && tvDetailsInfo.getBackDropPath().getTag() != null)
                    send.putString("backDropUrl", tvDetailsInfo.getBackDropPath().getTag().toString());

                // Poster path url
                if (tvDetailsInfo.getPosterPath().getTag() != null)
                    send.putString("posterPathURL", tvDetailsInfo.getPosterPath().getTag().toString());


                // Rating
                send.putFloat("rating", tvDetailsInfo.getRatingBar().getRating());
                send.putString("voteCount", tvDetailsInfo.getVoteCount().getText().toString());

                // Title
                send.putString("titleText", tvDetailsInfo.getTitle().getText().toString());


                // Status
                send.putString("status", tvDetailsInfo.getStatusText().getText().toString());

                // Type
                send.putString("typeText", tvDetailsInfo.getTypeText().getText().toString());

                // Episode runtime
                send.putString("episodeRuntime", tvDetailsInfo.getEpisodeRuntime().getText().toString());

                // Number of episodes
                send.putString("numberOfEpisodesText", tvDetailsInfo.getNumberOfEpisodesText().getText().toString());

                // Number of seasons
                send.putString("numberOfSeasonsText", tvDetailsInfo.getNumberOfSeasonsText().getText().toString());

                // First air date
                send.putString("firstAirDateText", tvDetailsInfo.getFirstAirDateText().getText().toString());

                // Last air date
                send.putString("lastAirDateText", tvDetailsInfo.getLastAirDateText().getText().toString());

                // Genres
                send.putString("genres", tvDetailsInfo.getGenres().getText().toString());

                // Production countries
                send.putString("productionCountries", tvDetailsInfo.getCountries().getText().toString());

                // Production companies
                send.putString("productionCompanies", tvDetailsInfo.getCompanies().getText().toString());

                // Similar list
                if (tvDetailsInfo.getSimilarList() != null && tvDetailsInfo.getSimilarList().size() > 0)
                    send.putParcelableArrayList("similarList", tvDetailsInfo.getSimilarList());

            }
            // TV details info ends here

            // TV details cast starts here
            if (tvDetailsCast != null) {
                send.putParcelableArrayList("castList", castList);
                send.putInt("lastVisitedPerson", tvDetailsCast.getLastVisitedPerson());
            }
            // TV details cast ends here

            if (tvDetailsOverview != null)
                send.putString("overview", tvDetailsOverview.getOverview().getText().toString());

            outState.putBundle("save", send);
            save = send;
            if (addToBackStack) {
                activity.addTvDetailsBundle(send);
                addToBackStack = false;
            }
        }


    }

    /**
     * Fired when are restoring from backState or orientation has changed.
     *
     * @param args our bundle with saved state.
     */
    private void onOrientationChange(Bundle args) {
        // Home page
        homeIconCheck = args.getInt("homeIconCheck");
        if (homeIconCheck == 0)
            homeIconUrl = args.getString("homepage");

        // Gallery
        galleryIconCheck = args.getInt("galleryIconCheck");
        if (galleryIconCheck == 0) {
            galleryList = new ArrayList<>();
            galleryList = args.getStringArrayList("galleryList");
            if (galleryList.size() == 0)
                activity.hideView(galleryIcon);
        }

        // More icon
        moreIconCheck = args.getInt("moreIconCheck");

        if (homeIconCheck == 1 && galleryIconCheck == 1) {
            moreIconCheck = 1;
            moreIcon.setVisibility(View.GONE);
        } else moreIconCheck = 0;

        mSlidingTabLayout.setOnPageChangeListener(onPageChangeSelected);
        activity.setTVDetailsInfoBundle(save);
        activity.setTVDetailsCastBundle(save);
        activity.setTVDetailsOverviewBundle(save);

        castList = save.getParcelableArrayList("castList");
        if (castList != null && castList.size() == 0) {
            noCast = true;
            mSlidingTabLayout.disableTabClickListener(1);
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                tvDetailsInfo = (TVDetailsInfo) tvDetailsSlideAdapter.getRegisteredFragment(0);
                if (currPos == 0) {
                    moreIcon.setVisibility(View.INVISIBLE);
                } else if (moreIconCheck == 0) {
                    tvDetailsInfo.getMoreIcon().setVisibility(View.INVISIBLE);
                    updateDownPos();
                }
                if (moreIconCheck == 1)
                    tvDetailsInfo.getMoreIcon().setVisibility(View.GONE);
                else {
                    // set listener on backdrop and poster path click to open gallery
                    if (galleryIconCheck == 0 && galleryList.size() > 0) {
                        tvDetailsInfo.getBackDropPath().setOnClickListener(onGalleryIconClick);
                        tvDetailsInfo.getPosterPath().setOnClickListener(onGalleryIconClick);
                    }
                    adjustIconsPos(homeIcon, galleryIcon);
                    adjustIconsPos(tvDetailsInfo.getHomeIcon(), tvDetailsInfo.getGalleryIcon());
                }

                // disable orientation changing, enable nav drawer sliding, show toolbar
                if (galleryIconCheck == 0 && galleryList.size() == 1) {
                    activity.getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(activity, R.color.background_material_light));
                    if (activity.getSupportActionBar() != null)
                        activity.getSupportActionBar().show();
                    activity.getMDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    if (Build.VERSION.SDK_INT >= 19)
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                    // Check orientation and lock to portrait if we are on phone
                    if (getResources().getBoolean(R.bool.portrait_only))
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }

            }
        });

    }


    /**
     * Class which listens when the user has tapped on More icon button.
     */
    public class onMoreIconClick implements View.OnClickListener {
        private boolean key;
        private boolean toolbarHidden;
        private int items;
        private View toolbarView = activity.findViewById(R.id.toolbar);
        private int currScroll;

        public onMoreIconClick() {
            // keep references for your onClick logic
        }

        public boolean getKey() {
            return key;
        }

        public void setKey(boolean key) {
            this.key = key;
        }

        @Override
        public void onClick(View v) {
            items = homeIconCheck + galleryIconCheck;
            toolbarHidden = toolbarView.getTranslationY() == -toolbarView.getHeight();
            currScroll = tvDetailsInfo.getRootView().getScrollY();

            if (!key) {
                iconDirection = 1;
                if (currPos == 0) {
                    // 2 icons
                    if (items == 0) {
                        if (toolbarHidden && currScroll / scale > twoIcons) {
                            iconDirection = -1;
                        } else if (!toolbarHidden && currScroll / scale > twoIconsToolbar) {
                            iconDirection = -1;
                        }
                    }
                    // 1 icon
                    if (items == 1) {
                        if (toolbarHidden && currScroll / scale > oneIcon) {
                            iconDirection = -1;
                        } else if (!toolbarHidden && currScroll / scale > oneIconToolbar) {
                            iconDirection = -1;
                        }
                    }
                }
                if (currPos == 0) {
                    tvDetailsInfo.getMoreIcon().setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_close_white_36dp));
                    showHideImages(View.VISIBLE, tvDetailsInfo.getHomeIcon(), tvDetailsInfo.getGalleryIcon());
                } else {
                    moreIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_close_white_36dp));
                    showHideImages(View.VISIBLE, homeIcon, galleryIcon);
                }
                key = true;
            } else {
                if (currPos == 0) {
                    tvDetailsInfo.getMoreIcon().setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_more_vert_white_36dp));
                    showHideImages(View.INVISIBLE, tvDetailsInfo.getHomeIcon(), tvDetailsInfo.getGalleryIcon());
                } else {
                    moreIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_more_vert_white_36dp));
                    showHideImages(View.INVISIBLE, homeIcon, galleryIcon);
                }
                key = false;
            }
        }
    }

    /**
     * Class which listens when the user has tapped on Home icon button.
     */
    public class onHomeIconClick implements View.OnClickListener {
        public onHomeIconClick() {
            // keep references for your onClick logic
        }


        @Override
        public void onClick(View v) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(homeIcon.getTag().toString()));
            startActivity(i);
        }
    }

    /**
     * Class which listens when the user has tapped on Gallery icon button.
     */
    public class onGalleryIconClick implements View.OnClickListener {
        public onGalleryIconClick() {
            // keep references for your onClick logic
        }


        @Override
        public void onClick(View v) {
            if (activity.getSearchViewCount())
                activity.incSearchTvDetails();
            if (galleryList.size() == 1) {
                setAddToBackStack(true);
                onSaveInstanceState(new Bundle());
                if (activity.getSupportActionBar() != null)
                    activity.getSupportActionBar().hide();
                activity.getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(activity, R.color.black));
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.frame_container, GalleryPreviewDetail.newInstance(MovieDB.imageUrl + getResources().getString(R.string.galleryPreviewImgSize) + galleryList.get(0)));
                // add the current transaction to the back stack:
                transaction.addToBackStack("movieDetails");
                transaction.commit();
            } else {
                try {
                    setAddToBackStack(true);
                    onSaveInstanceState(new Bundle());
                    showInstantToolbar();
                    activity.getGalleryListView().setTitle(getTitle());
                    FragmentManager manager = getFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    Bundle args = new Bundle();
                    args.putStringArrayList("galleryList", galleryList);
                    activity.getGalleryListView().setArguments(args);
                    transaction.replace(R.id.frame_container, activity.getGalleryListView());
                    // add the current transaction to the back stack:
                    transaction.addToBackStack("movieDetails");
                    transaction.commit();
                } catch (java.lang.IllegalStateException e) {
                    GalleryList galleryListView = new GalleryList();
                    galleryListView.setTitle(getTitle());
                    FragmentManager manager = getFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    Bundle args = new Bundle();
                    args.putStringArrayList("galleryList", galleryList);
                    galleryListView.setArguments(args);
                    transaction.replace(R.id.frame_container, galleryListView);
                    // add the current transaction to the back stack:
                    transaction.addToBackStack("movieDetails");
                    transaction.commit();
                }
            }
        }
    }

    /**
     * Class which listens when the user has changed the tap in Movie details
     */
    public class onPageChangeSelected implements ViewPager.OnPageChangeListener {
        private boolean toolbarHidden;
        private View toolbarView = activity.findViewById(R.id.toolbar);

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }


        @Override
        public void onPageSelected(int position) {
            if (noCast) {
                if (position == 1 && currPos == 2) {
                    mViewPager.setCurrentItem(0);
                    return;
                }
                if (position == 1 && currPos == 0) {
                    mViewPager.setCurrentItem(2);
                    return;
                }
            }
            if (toolbarView != null)
                toolbarHidden = toolbarView.getTranslationY() == -toolbarView.getHeight();

            if (position == 0) {
                scrollSpeed = 2.2F;
                if (tvDetailsInfo != null) {
                    if (119 * scale <= (tvDetailsInfo.getScrollView().getChildAt(0).getHeight() - (567 * scale)) && tvDetailsInfo.canScroll()) {
                        if (toolbarHidden) {
                            final ObservableParallaxScrollView scrollView = tvDetailsInfo.getScrollView();
                            if (scrollView.getCurrentScrollY() / scale < 119) {
                                infoTabScrollPosUpdated = true;
                                scrollView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        scrollView.scrollTo(0, (int) (119 * scale));
                                    }
                                });
                            }
                        }
                    } else
                        showInstantToolbar();
                }

            } else scrollSpeed = 1;


            if (position == 1 && tvDetailsCast != null) {
                if (tvDetailsCast.getCastList() != null && tvDetailsCast.getCastList().size() > 0) {
                    final AbsListView listView = tvDetailsCast.getListView();
                    if (tvDetailsCast.canScroll()) {
                        listView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (toolbarHidden && ((Scrollable) listView).getCurrentScrollY() < minThreshold) {
                                    if (phone)
                                        listView.smoothScrollBy((int) (56 * scale), 0);
                                    else
                                        listView.smoothScrollBy((int) (65 * scale), 0);
                                }
                            }
                        });
                    } else {
                        if (toolbarHidden)
                            showInstantToolbar();
                    }
                } else {
                    if (toolbarHidden)
                        showInstantToolbar();
                }
            }

            if (position == 2 && tvDetailsOverview != null) {
                tvDetailsOverview.getScrollView().post(new Runnable() {
                    @Override
                    public void run() {
                        if (toolbarHidden)
                            tvDetailsOverview.getScrollView().scrollTo(0, (int) (56 * scale));
                        else
                            tvDetailsOverview.getScrollView().scrollTo(0, 0);
                    }
                });
            }

            if (moreIconCheck == 0) {
                if (tvDetailsInfo != null) {
                    tvDetailsInfoScrollY = tvDetailsInfo.getRootView().getScrollY();

                    galleryIcon.clearAnimation();
                    homeIcon.clearAnimation();

                    homeIcon.setVisibility(View.INVISIBLE);
                    tvDetailsInfo.getHomeIcon().setVisibility(View.INVISIBLE);

                    galleryIcon.setVisibility(View.INVISIBLE);
                    tvDetailsInfo.getGalleryIcon().setVisibility(View.INVISIBLE);

                    moreIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_more_vert_white_36dp));

                    if (position == 0) {
                        tvDetailsInfo.getMoreIcon().setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_more_vert_white_36dp));
                        moreIcon.setVisibility(View.INVISIBLE);
                    } else {
                        tvDetailsInfo.getMoreIcon().setVisibility(View.INVISIBLE);
                    }

                    onMoreIconClick.setKey(false);

                    if (currPos == 0 && position == 1) {
                        updateUpPos();
                        createDownAnimation();
                        moreIcon.startAnimation(downAnimation);
                    }
                    if (currPos == 0 && position == 2) {
                        updateUpPos();
                        createDownAnimation();
                        moreIcon.startAnimation(downAnimation);
                    }
                    if (currPos == 1 && position == 0) {
                        updateDownPos();
                        // we have a special case here if this is true, this means that we have been in the cast tab
                        // we have hidden the toolbar and we are returning to the info tab where the scrollY was 0
                        // so earlier in this function we updated the scrollY value and now we need to update the
                        // animation value else the icon will "jump"
                        if (infoTabScrollPosUpdated) {
                            infoTabScrollPosUpdated = false;
                            createUpAnimation((119 * scale) - tvDetailsInfo.getScrollView().getCurrentScrollY());
                        } else
                            createUpAnimation(0);
                        moreIcon.startAnimation(upAnimation);
                    }
                    if (currPos == 2 && position == 0) {
                        updateDownPos();
                        if (infoTabScrollPosUpdated) {
                            infoTabScrollPosUpdated = false;
                            createUpAnimation((119 * scale) - tvDetailsInfo.getScrollView().getCurrentScrollY());
                        } else
                            createUpAnimation(0);
                        moreIcon.startAnimation(upAnimation);
                    }
                }
            }

            if (!noCast || position != 1)
                currPos = position;

        }

    }

    /**
     * Listener which updates the icons position after the animation ended.
     */
    private class DownAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            moreIcon.clearAnimation();
            updateDownPos();
            moreIcon.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) moreIcon.getLayoutParams();
            layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            moreIcon.setLayoutParams(layoutParams);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }

    /**
     * Updates the icons position when called.
     */
    public void updateDownPos() {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(moreIcon.getWidth(), moreIcon.getHeight());
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(homeIcon.getWidth(), homeIcon.getHeight());
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(galleryIcon.getWidth(), galleryIcon.getHeight());
        lp.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp1.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp2.addRule(RelativeLayout.ALIGN_PARENT_END);

        lp.setMargins(0, (int) (scale * (496 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 15 + 0.5f), 0);
        moreIcon.setLayoutParams(lp);
        lp1.setMargins(0, (int) (scale * (439 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        homeIcon.setLayoutParams(lp1);
        lp2.setMargins(0, (int) (scale * (383.3 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        galleryIcon.setLayoutParams(lp2);
    }

    /**
     * Listener which updates the icons position after the animation ended.
     */
    private class UpAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            tvDetailsInfo.getMoreIcon().clearAnimation();
            tvDetailsInfo.getMoreIcon().setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }

    /**
     * Updates the icons position when called.
     */
    public void updateUpPos() {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(moreIcon.getWidth(), moreIcon.getHeight());
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(homeIcon.getWidth(), homeIcon.getHeight());
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(galleryIcon.getWidth(), galleryIcon.getHeight());
        lp.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp1.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp2.addRule(RelativeLayout.ALIGN_PARENT_END);

        lp.setMargins(0, (int) (scale * (346 + iconMarginConstant) + 0.5f - tvDetailsInfoScrollY), (int) (scale * 15 + 0.5f), 0);
        moreIcon.setLayoutParams(lp);
    }

    /**
     * Creates animation for the More Icon with down direction.
     */
    public void createDownAnimation() {
        // if this is 300 this means that we are on tablet and orientation is landscape
        // we do this else the animation will go off screen
        // so if we are on landscape we change the direction of the animation
        if (iconMarginLandscape == 300)
            downAnimation = new TranslateAnimation(0, 0, 0, -scale * 150 + 0.5f + tvDetailsInfoScrollY);
        else
            downAnimation = new TranslateAnimation(0, 0, 0, scale * (150 + iconConstantSpecialCase) + 0.5f + tvDetailsInfoScrollY);
        downAnimation.setDuration(500);
        downAnimation.setFillAfter(false);
        downAnimation.setAnimationListener(downAnimationListener);
    }

    /**
     * Creates animation for the More Icon with up direction.
     */
    public void createUpAnimation(float dy) {
        // if this is 300 this means that we are on tablet and orientation is landscape
        // we do this else the animation will go off screen
        // so if we are on landscape we change the direction of the animation
        if (iconMarginLandscape == 300)
            upAnimation = new TranslateAnimation(0, 0, 0, scale * 150 + 0.5f - tvDetailsInfoScrollY - dy);
        else
            upAnimation = new TranslateAnimation(0, 0, 0, -scale * (150 + iconConstantSpecialCase) + 0.5f - tvDetailsInfoScrollY - dy);
        upAnimation.setDuration(500);
        upAnimation.setFillAfter(false);
        upAnimation.setAnimationListener(upAnimationListener);
    }

    public TVDetails.onMoreIconClick getOnMoreIconClick() {
        return onMoreIconClick;
    }

    public Bundle getSave() {
        return save;
    }

    public void setSave(Bundle save) {
        this.save = save;
    }

    public void setAddToBackStack(boolean addToBackStack) {
        this.addToBackStack = addToBackStack;
    }

    /**
     * This method calculates what icons do we have.
     *
     * @param homeIcon    the first icon
     * @param galleryIcon the second icon
     */
    public void adjustIconsPos(CircledImageView homeIcon, CircledImageView galleryIcon) {
        int iconCount[] = {homeIconCheck, galleryIconCheck};
        ArrayList<CircledImageView> circledImageViews = new ArrayList<>();
        circledImageViews.add(homeIcon);
        circledImageViews.add(galleryIcon);

        for (int i = 0; i < iconCount.length; i++) {
            if (iconCount[i] == 1)
                circledImageViews.get(circledImageViews.size() - 1).setVisibility(View.INVISIBLE);
            else {
                CircledImageView temp = circledImageViews.get(0);
                switch (i) {
                    case 0:
                        temp.setOnClickListener(onHomeIconClick);
                        temp.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_home_white_24dp));
                        temp.setTag(homeIconUrl);
                        break;
                    case 1:
                        temp.setOnClickListener(onGalleryIconClick);
                        temp.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_photo_camera_white_24dp));
                        break;

                }
                circledImageViews.remove(0);
            }

        }

    }

    /**
     * Fired from the on More Icon click listeners. Updates the visibility of the gallery and homePage icon.
     * And creates animation for them also.
     *
     * @param visibility  visibility value
     * @param homeIcon    first icon
     * @param galleryIcon second icon
     */
    public void showHideImages(int visibility, CircledImageView homeIcon, CircledImageView galleryIcon) {
        float dy[] = {0.7f, 56.7f};
        float infoTabDy[] = {-2.4f, 53.5f};
        int currDy = 0;
        int delay = 100;
        int iconCount[] = {homeIconCheck, galleryIconCheck};
        ArrayList<CircledImageView> circledImageViews = new ArrayList<>();
        circledImageViews.add(homeIcon);
        circledImageViews.add(galleryIcon);

        if (visibility == View.VISIBLE) {
            if (currPos != 0)
                updateIconDownPos();
            else
                updateIconDownPosInInfoTab();
        } else {
            if (currPos != 0)
                updateIconUpPos();
            else
                updateIconUpPosInInfoTab();
        }

        for (int i = 0; i < iconCount.length; i++) {
            if (iconCount[i] == 1)
                circledImageViews.get(circledImageViews.size() - 1).setVisibility(View.INVISIBLE);
            else {
                CircledImageView temp = circledImageViews.get(0);
                if (visibility == View.VISIBLE) {
                    if (currPos == 0)
                        createIconUpAnimation(infoTabDy[currDy], delay);
                    else
                        createIconUpAnimation(dy[currDy], delay);
                    temp.startAnimation(iconUpAnimation);
                } else {
                    if (currPos == 0)
                        createIconDownAnimation(infoTabDy[currDy]);
                    else
                        createIconDownAnimation(dy[currDy]);
                    temp.startAnimation(iconDownAnimation);
                }
                currDy++;
                delay -= 50;
                temp.setVisibility(visibility);
                circledImageViews.remove(0);
            }

        }

    }

    /**
     * Creates animation for the gallery and homePage Icons with up direction.
     */
    public void createIconUpAnimation(float dy, int delay) {
        iconUpAnimation = new TranslateAnimation(0, 0, 0, (-(scale * 67.3f) + 0.5f - (dy * scale)) * iconDirection);
        iconUpAnimation.setDuration(250);
        iconUpAnimation.setFillAfter(false);
        iconUpAnimation.setStartOffset(delay);
        iconUpAnimation.setAnimationListener(iconUpAnimationListener);
    }

    /**
     * Creates animation for the gallery and homePage Icons with down direction.
     */
    public void createIconDownAnimation(float dy) {
        iconDownAnimation = new TranslateAnimation(0, 0, 0, ((scale * 67.3f) + 0.5f + (dy * scale)) * iconDirection);
        iconDownAnimation.setDuration(250);
        iconDownAnimation.setFillAfter(false);
        iconDownAnimation.setAnimationListener(iconDownAnimationListener);
    }

    /**
     * Listener which updates the icons position after the animation ended.
     */
    private class IconUpAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            if (currPos != 0) {
                updateIconUpPos();
                homeIcon.clearAnimation();
                galleryIcon.clearAnimation();
            } else {
                updateIconUpPosInInfoTab();
                tvDetailsInfo.getHomeIcon().clearAnimation();
                tvDetailsInfo.getGalleryIcon().clearAnimation();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }

    /**
     * Listener which updates the icons position after the animation ended.
     */
    private class IconDownAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {

            if (currPos != 0) {
                updateIconDownPos();
                homeIcon.clearAnimation();
                galleryIcon.clearAnimation();
            } else {
                updateIconDownPosInInfoTab();
                tvDetailsInfo.getHomeIcon().clearAnimation();
                tvDetailsInfo.getGalleryIcon().clearAnimation();

            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }

    /**
     * Updates the icons position when called.
     */
    public void updateIconDownPos() {
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(homeIcon.getWidth(), homeIcon.getHeight());
        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(galleryIcon.getWidth(), galleryIcon.getHeight());
        lp1.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp3.addRule(RelativeLayout.ALIGN_PARENT_END);

        lp1.setMargins(0, (int) (scale * (506 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        homeIcon.setLayoutParams(lp1);
        lp3.setMargins(0, (int) (scale * (506 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        galleryIcon.setLayoutParams(lp3);
    }

    /**
     * Updates the icons position when called.
     */
    public void updateIconUpPos() {
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(homeIcon.getWidth(), homeIcon.getHeight());
        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(galleryIcon.getWidth(), galleryIcon.getHeight());
        lp1.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp3.addRule(RelativeLayout.ALIGN_PARENT_END);

        lp1.setMargins(0, (int) (scale * (439 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        homeIcon.setLayoutParams(lp1);
        lp3.setMargins(0, (int) (scale * (383.3 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        galleryIcon.setLayoutParams(lp3);
    }

    /**
     * Updates the icons position in the movie info tap when called.
     */
    public void updateIconDownPosInInfoTab() {
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) tvDetailsInfo.getGalleryIcon().getLayoutParams();
        p.removeRule(RelativeLayout.ABOVE);
        p.removeRule(RelativeLayout.BELOW);
        p.addRule(RelativeLayout.ABOVE, R.id.moreIcon);
        p.setMargins(0, 0, (int) (23 * scale), (int) (-20 * scale));
        tvDetailsInfo.getGalleryIcon().setLayoutParams(p);
        tvDetailsInfo.getHomeIcon().setLayoutParams(p);
    }

    /**
     * Updates the icons position in the movie info tap when called.
     */
    public void updateIconUpPosInInfoTab() {
        RelativeLayout.LayoutParams p3 = new RelativeLayout.LayoutParams(homeIcon.getWidth(), homeIcon.getHeight());
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(galleryIcon.getWidth(), galleryIcon.getHeight());
        p3.addRule(RelativeLayout.ALIGN_PARENT_END);
        p.addRule(RelativeLayout.ALIGN_PARENT_END);
        if (iconDirection == 1) {
            p3.addRule(RelativeLayout.ABOVE, R.id.moreIcon);
            p3.setMargins(0, 0, (int) (23 * scale), (int) (44 * scale));
            p.addRule(RelativeLayout.ABOVE, R.id.homeIcon);
            p.setMargins(0, 0, (int) (23 * scale), (int) (15.5f * scale));
        } else {
            p3.addRule(RelativeLayout.BELOW, R.id.moreIcon);
            p3.setMargins(0, (int) (16 * scale), (int) (23 * scale), 0);
            p.addRule(RelativeLayout.BELOW, R.id.homeIcon);
            p.setMargins(0, (int) (15.5f * scale), (int) (23 * scale), 0);
        }

        tvDetailsInfo.getHomeIcon().setLayoutParams(p3);
        tvDetailsInfo.getGalleryIcon().setLayoutParams(p);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        float scroll = scrollY;
        if (mViewPager.getCurrentItem() == 0) {
            scroll = (scroll / scrollSpeed);

            if (scrollY / scale >= 0 && onMoreIconClick.getKey())
                onMoreIconClick.onClick(null);
        }

        if (dragging) {
            View toolbarView = getActivity().findViewById(R.id.toolbar);

            if (scroll > oldScrollY) {

                if (upDyKey) {
                    upDy = scroll;
                    upDyKey = false;
                } else {
                    dy = upDy - scroll;

                    if (dy >= -toolbarView.getHeight()) {
                        toolbarView.setTranslationY(dy);
                        mSlidingTabLayout.setTranslationY(dy);
                    } else {
                        toolbarView.setTranslationY(-toolbarView.getHeight());
                        mSlidingTabLayout.setTranslationY(-toolbarView.getHeight());
                    }

                    downDyKey = true;
                }

            }

            if (scroll < oldScrollY) {

                if (downDyKey) {
                    downDy = scroll;
                    downDyTrans = toolbarView.getTranslationY();
                    downDyKey = false;
                } else {

                    dy = (downDyTrans + (downDy - scroll));
                    if (dy <= 0) {
                        toolbarView.setTranslationY(dy);
                        mSlidingTabLayout.setTranslationY(dy);
                    } else {
                        toolbarView.setTranslationY(0);
                        mSlidingTabLayout.setTranslationY(0);
                    }

                    upDyKey = true;

                }
            }


        }

        oldScrollY = scroll;
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        adjustToolbar(scrollState);
    }


    private Scrollable getCurrentScrollable() {
        Fragment fragment = getCurrentFragment();
        if (fragment == null) {
            return null;
        }
        View view = fragment.getView();
        if (view == null) {
            return null;
        }
        switch (mViewPager.getCurrentItem()) {
            case 0:
                return (Scrollable) view.findViewById(R.id.tvdetailsinfo);
            case 1:
                return (Scrollable) view.findViewById(R.id.castList);
            case 2:
                return (Scrollable) view.findViewById(R.id.tvdetailsoverview);
            default:
                return (Scrollable) view.findViewById(R.id.tvdetailsinfo);
        }
    }

    /**
     * Fixes the position of the toolbar
     *
     * @param scrollState
     */
    private void adjustToolbar(ScrollState scrollState) {
        View toolbarView = getActivity().findViewById(R.id.toolbar);
        int toolbarHeight = toolbarView.getHeight();
        final Scrollable scrollable = getCurrentScrollable();
        if (scrollable == null) {
            return;
        }
        int scrollY = scrollable.getCurrentScrollY();
        if (scrollState == ScrollState.DOWN) {
            showToolbar();
        } else if (scrollState == ScrollState.UP) {
            switch (currPos) {
                case 0:
                    if (119 * scale <= scrollY) {
                        hideToolbar();
                    } else {
                        showToolbar();
                    }
                    break;
                case 1:
                    if (toolbarHeight <= scrollY - hideThreshold) {
                        hideToolbar();
                    } else {
                        showToolbar();
                    }
                    break;
                case 2:
                    if (toolbarHeight <= scrollY) {
                        hideToolbar();
                    } else {
                        showToolbar();
                    }
                    break;
            }

        }
    }

    /**
     * Returns the current active fragment for the given position
     */
    private Fragment getCurrentFragment() {
        return tvDetailsSlideAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
    }


    private void showToolbar() {
        animateToolbar(0);
    }

    private void hideToolbar() {
        View toolbarView = getActivity().findViewById(R.id.toolbar);
        animateToolbar(-toolbarView.getHeight());
    }

    /**
     * Animates our toolbar to the given direction
     *
     * @param toY our translation length.
     */
    private void animateToolbar(final float toY) {
        if (activity != null) {
            View toolbarView = activity.findViewById(R.id.toolbar);

            if (toolbarView != null) {
                toolbarView.animate().translationY(toY).setInterpolator(new DecelerateInterpolator(2)).setDuration(200).start();
                mSlidingTabLayout.animate().translationY(toY).setInterpolator(new DecelerateInterpolator(2)).setDuration(200).start();


                if (toY == 0) {
                    upDyKey = true;
                    downDyKey = false;
                    downDy = 9999999;
                } else {
                    downDyKey = true;
                    upDyKey = false;
                    upDy = -9999999;
                }

            }
        }
    }

    /**
     * Instant shows our toolbar. Used when click on movie details from movies list and toolbar is hidden.
     */
    public void showInstantToolbar() {
        if (activity != null) {
            View toolbarView = activity.findViewById(R.id.toolbar);

            if (toolbarView != null) {
                toolbarView.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).setDuration(0).start();
                mSlidingTabLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).setDuration(0).start();


                upDyKey = true;
                downDyKey = false;
                downDy = 9999999;

            }
        }
    }

    /**
     * Fired when fragment is destroyed.
     */
    public void onDestroyView() {
        super.onDestroyView();
        onPageChangeSelected = null;
        onGalleryIconClick = null;
        onMoreIconClick = null;
        onHomeIconClick = null;
        onPageChangeSelected = null;
        downAnimationListener = null;
        upAnimationListener = null;
        iconUpAnimationListener = null;
        iconDownAnimationListener = null;
    }

}