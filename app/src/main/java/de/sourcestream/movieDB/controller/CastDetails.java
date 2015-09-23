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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.sourcestream.movieDB.MainActivity;
import de.sourcestream.movieDB.MovieDB;
import de.sourcestream.movieDB.R;
import de.sourcestream.movieDB.adapter.CastDetailsSlideAdapter;
import de.sourcestream.movieDB.helper.ObservableScrollViewCallbacks;
import de.sourcestream.movieDB.helper.ScrollState;
import de.sourcestream.movieDB.helper.Scrollable;
import de.sourcestream.movieDB.model.MovieModel;
import de.sourcestream.movieDB.model.SimilarModel;
import de.sourcestream.movieDB.view.MovieDetailsSlidingTabLayout;
import de.sourcestream.movieDB.view.ObservableParallaxScrollView;

/**
 * Cast details
 */
public class CastDetails extends Fragment implements ObservableScrollViewCallbacks {

    private MainActivity activity;
    private View rootView;
    private int currentId;
    private int timeOut;
    private HttpURLConnection conn;
    private String title;
    private Bundle save;


    private ArrayList<MovieModel> moviesList;
    private MovieModel movieModel;

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
    private CastDetailsSlideAdapter castDetailsSlideAdapter;
    private onPageChangeSelected onPageChangeSelected;
    private TranslateAnimation downAnimation;
    private DownAnimationListener downAnimationListener;
    private TranslateAnimation upAnimation;
    private UpAnimationListener upAnimationListener;
    private TranslateAnimation iconUpAnimation;
    private IconUpAnimationListener iconUpAnimationListener;
    private TranslateAnimation iconDownAnimation;
    private IconDownAnimationListener iconDownAnimationListener;
    private CastDetailsInfo castDetailsInfo;
    private CastDetailsCredits castDetailsCredits;
    private CastDetailsBiography castDetailsBiography;
    private int castDetailsInfoScrollY;
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
    private boolean noCredits;

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
        movieModel = new MovieModel();
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
            rootView = inflater.inflate(R.layout.castdetails, container, false);
            spinner = (ProgressBar) rootView.findViewById(R.id.progressBar);

            homeIcon = (CircledImageView) rootView.findViewById(R.id.homeIcon);
            homeIcon.bringToFront();
            homeIcon.setVisibility(View.INVISIBLE);

            galleryIcon = (CircledImageView) rootView.findViewById(R.id.galleryIcon);
            galleryIcon.bringToFront();
            galleryIcon.setVisibility(View.INVISIBLE);

            moreIcon = (CircledImageView) rootView.findViewById(R.id.moreIcon);
            moreIcon.bringToFront();
        }
        moreIcon.setOnClickListener(onMoreIconClick);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (activity.getCastDetailsBundle().size() > 0 && activity.getRestoreMovieDetailsState()) {
            save = activity.getCastDetailsBundle().get(activity.getCastDetailsBundle().size() - 1);
            activity.removeCastDetailsBundle(activity.getCastDetailsBundle().size() - 1);
            if (activity.getSearchViewCount())
                activity.decSearchCastDetails();
            activity.setRestoreMovieDetailsState(false);
        }
        if (save != null && save.getInt("timeOut") == 1)
            activity.setRestoreMovieDetailsAdapterState(true);
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        castDetailsSlideAdapter = new CastDetailsSlideAdapter(getChildFragmentManager(), getResources(), activity);
        if (mViewPager != null)
            currPos = mViewPager.getCurrentItem();
        mViewPager = (ViewPager) rootView.findViewById(R.id.castDetailsPager);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(castDetailsSlideAdapter);
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
                        request.execute(MovieDB.url + "person/" + currentId + "?append_to_response=combined_credits%2Cimages&api_key=" + MovieDB.key).get(10000, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException | ExecutionException | InterruptedException | CancellationException e) {
                        request.cancel(true);
                        // we abort the http request, else it will cause problems and slow connection later
                        if (conn != null)
                            conn.disconnect();
                        if (spinner != null)
                            activity.hideView(spinner);
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
        activity.setCastDetailsFragment(this);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                castDetailsInfo = (CastDetailsInfo) castDetailsSlideAdapter.getRegisteredFragment(0);
                castDetailsCredits = (CastDetailsCredits) castDetailsSlideAdapter.getRegisteredFragment(1);
                castDetailsBiography = (CastDetailsBiography) castDetailsSlideAdapter.getRegisteredFragment(2);
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
        t.setScreenName("CastDetails - " + getTitle());
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    /**
     * This class handles the connection to our backend server.
     * If the connection is successful we set information on our views.
     */
    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {

        /**
         * Called before doInBackground()
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Here we establish connection. If everything is ok we update the views.
         *
         * @param urls the url to load.
         * @return true if sucess, false for fail.
         */
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
                    while (castDetailsInfo == null) {
                        Thread.sleep(200);
                    }
                    if (isAdded() && castDetailsInfo != null) {
                        // Name
                        activity.setText(castDetailsInfo.getName(), jsonData.getString("name"));

                        // Profile image
                        if (!jsonData.getString("profile_path").equals("null") && !jsonData.getString("profile_path").isEmpty()) {
                            activity.setBackDropImage(castDetailsInfo.getProfilePath(), jsonData.getString("profile_path"));
                            activity.setImageTag(castDetailsInfo.getProfilePath(), jsonData.getString("profile_path"));
                        }

                        // Birth information begins here.
                        String birthInfoData = "";
                        if (!jsonData.getString("place_of_birth").equals("null") && !jsonData.getString("place_of_birth").isEmpty())
                            birthInfoData += jsonData.getString("place_of_birth");

                        if (!jsonData.getString("birthday").equals("null") && !jsonData.getString("birthday").isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            try {
                                Date date = sdf.parse(jsonData.getString("birthday"));
                                String formattedDate = activity.getDateFormat().format(date);
                                birthInfoData += " " + formattedDate;
                            } catch (java.text.ParseException e) {
                            }
                        }


                        if (!jsonData.getString("deathday").equals("null") && !jsonData.getString("deathday").isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            try {
                                Date date = sdf.parse(jsonData.getString("deathday"));
                                String formattedDate = activity.getDateFormat().format(date);
                                birthInfoData += " - " + formattedDate;
                            } catch (java.text.ParseException e) {
                            }
                        }

                        if (!birthInfoData.equals("null") && !birthInfoData.isEmpty())
                            activity.setText(castDetailsInfo.getBirthInfo(), birthInfoData);
                        else
                            activity.hideTextView(castDetailsInfo.getBirthInfo());
                        // Birth information ends here.


                        // Also Known as
                        JSONArray alsoKnownAsArray = jsonData.getJSONArray("also_known_as");
                        String alsoKnownAsData = "";
                        for (int i = 0; i < alsoKnownAsArray.length(); i++) {
                            if (i + 1 == alsoKnownAsArray.length())
                                alsoKnownAsData += alsoKnownAsArray.get(i);
                            else
                                alsoKnownAsData += alsoKnownAsArray.get(i) + ", ";
                        }
                        if (!alsoKnownAsData.equals("null") && !alsoKnownAsData.isEmpty())
                            activity.setText(castDetailsInfo.getAlsoKnownAs(), getResources().getString(R.string.alsoKnownAs) + " " + alsoKnownAsData);
                        else
                            activity.hideTextView(castDetailsInfo.getAlsoKnownAs());

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
                        JSONArray galleryProfilesArray = galleryObject.getJSONArray("profiles");
                        galleryList = new ArrayList<>();
                        if (galleryProfilesArray.length() > 0) {
                            for (int i = 0; i < galleryProfilesArray.length(); i++) {
                                JSONObject object = galleryProfilesArray.getJSONObject(i);
                                galleryList.add(object.getString("file_path"));
                            }
                            galleryIconCheck = 0;
                        } else {
                            activity.invisibleView(galleryIcon);
                            galleryIconCheck = 1;
                        }

                        //Credits, here we load cast
                        JSONObject credits = jsonData.getJSONObject("combined_credits");
                        JSONArray creditsArray = credits.getJSONArray("cast");
                        moviesList = new ArrayList<>();
                        final ArrayList<SimilarModel> similarList = new ArrayList<>();

                        for (int i = 0; i < creditsArray.length(); i++) {
                            JSONObject object = creditsArray.getJSONObject(i);

                            MovieModel movie = new MovieModel();
                            movie.setId(object.getInt("id"));

                            if (object.getString("media_type").equals("movie")) {
                                movie.setTitle(object.getString("title"));
                                if (!object.getString("release_date").equals("null") && !object.getString("release_date").isEmpty())
                                    movie.setReleaseDate(object.getString("release_date"));
                            }

                            if (object.getString("media_type").equals("tv")) {
                                movie.setTitle(object.getString("name"));
                                if (!object.getString("first_air_date").equals("null") && !object.getString("first_air_date").isEmpty())
                                    movie.setReleaseDate(object.getString("first_air_date"));
                            }

                            movie.setCharacter(object.getString("character"));
                            // is added checks if we are still on the same view, if we don't do this check the program will crash
                            if (isAdded()) {
                                if (!object.getString("poster_path").equals("null") && !object.getString("poster_path").isEmpty())
                                    movie.setPosterPath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + object.getString("poster_path"));
                            }

                            movie.setMediaType(object.getString("media_type"));

                            moviesList.add(movie);
                        }

                        // Crew
                        JSONArray crewArray = credits.getJSONArray("crew");
                        for (int i = 0; i < crewArray.length(); i++) {
                            JSONObject object = crewArray.getJSONObject(i);

                            String departmentAndJob = "";
                            MovieModel movie = new MovieModel();
                            movie.setId(object.getInt("id"));

                            if (object.getString("media_type").equals("movie")) {
                                movie.setTitle(object.getString("title"));
                                if (!object.getString("release_date").equals("null") && !object.getString("release_date").isEmpty())
                                    movie.setReleaseDate(object.getString("release_date"));
                            }

                            if (object.getString("media_type").equals("tv")) {
                                movie.setTitle(object.getString("name"));
                                if (!object.getString("first_air_date").equals("null") && !object.getString("first_air_date").isEmpty())
                                    movie.setReleaseDate(object.getString("first_air_date"));
                            }

                            // is added checks if we are still on the same view, if we don't do this check the program will crash
                            if (isAdded()) {
                                if (!object.getString("poster_path").equals("null") && !object.getString("poster_path").isEmpty())
                                    movie.setPosterPath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + object.getString("poster_path"));
                            }


                            if (!object.getString("department").equals("null") && !object.getString("department").isEmpty())
                                departmentAndJob += object.getString("department");

                            if (!object.getString("job").equals("null") && !object.getString("job").isEmpty())
                                departmentAndJob += " / " + object.getString("job");

                            movie.setDepartmentAndJob(departmentAndJob);

                            movie.setMediaType(object.getString("media_type"));

                            moviesList.add(movie);
                        }

                        Collections.sort(moviesList, movieModel);
                        ArrayList<MovieModel> movieListNoDuplicates = removeDuplicates(moviesList);
                        int simLen = 6;
                        if (movieListNoDuplicates.size() < 6)
                            simLen = movieListNoDuplicates.size();
                        for (int i = 0; i < simLen; i++) {
                            SimilarModel simMov = new SimilarModel();
                            simMov.setId(movieListNoDuplicates.get(i).getId());
                            simMov.setMediaType(movieListNoDuplicates.get(i).getMediaType());
                            simMov.setTitle(movieListNoDuplicates.get(i).getTitle());

                            if (movieListNoDuplicates.get(i).getReleaseDate() != null)
                                simMov.setReleaseDate(movieListNoDuplicates.get(i).getReleaseDate());

                            // is added checks if we are still on the same view, if we don't do this check the program will crash
                            if (isAdded()) {
                                if (movieListNoDuplicates.get(i).getPosterPath() != null)
                                    simMov.setPosterPath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + movieListNoDuplicates.get(i).getPosterPath());
                            }

                            similarList.add(simMov);
                        }

                        if (similarList.size() == 0)
                            activity.hideView(castDetailsInfo.getKnownHolder());
                        if (movieListNoDuplicates.size() < 7)
                            activity.hideView(castDetailsInfo.getShowMoreButton());

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (moviesList.size() == 0) {
                                    noCredits = true;
                                    mSlidingTabLayout.disableTabClickListener(1);
                                }

                                // Cast
                                if (isAdded()) {
                                    castDetailsInfo.setKnownList(similarList);
                                    castDetailsCredits.setAdapter(moviesList);
                                }
                            }
                        });

                        // Biography
                        final String biography = jsonData.getString("biography");

                        if (!biography.equals("null") && !biography.isEmpty())
                            activity.setText(castDetailsBiography.getBiography(), biography);
                        else
                            activity.setText(castDetailsBiography.getBiography(), getResources().getString(R.string.noBiography));


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
                        activity.hideView(castDetailsInfo.getMoreIcon());
                    } else {
                        moreIconCheck = 0;
                        activity.showView(castDetailsInfo.getMoreIcon());
                        // set listener on backdrop click to open gallery
                        if (galleryIconCheck == 0)
                            castDetailsInfo.getProfilePath().setOnClickListener(onGalleryIconClick);
                        adjustIconsPos(homeIcon, galleryIcon);
                        adjustIconsPos(castDetailsInfo.getHomeIcon(), castDetailsInfo.getGalleryIcon());
                    }
                }
            } else setTimeOut(1);
        }
    }

    /**
     * We use this key to know if the user has tried to open this movie and the connection failed.
     * So if he tries to load again the same movie we know that the connection has failed and we need to make a new request.
     */
    public int getTimeOut() {
        return timeOut;
    }

    /**
     * Updates the timeOut value.
     */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
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

        if (save != null) {
            outState.putBundle("save", save);
            if (addToBackStack) {
                activity.addCastDetailsBundle(save);
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

                // Cast details info begins here
                if (castDetailsInfo != null) {
                    // Name
                    send.putString("name", castDetailsInfo.getName().getText().toString());

                    // Poster path url
                    if (castDetailsInfo.getProfilePath().getTag() != null)
                        send.putString("profilePathURL", castDetailsInfo.getProfilePath().getTag().toString());

                    // Birth info
                    send.putString("birthInfo", castDetailsInfo.getBirthInfo().getText().toString());

                    // Also known as
                    send.putString("alsoKnownAs", castDetailsInfo.getAlsoKnownAs().getText().toString());

                    // Known list
                    if (castDetailsInfo.getKnownList() != null && castDetailsInfo.getKnownList().size() > 0)
                        send.putParcelableArrayList("knownList", castDetailsInfo.getKnownList());

                }
                // Cast details info ends here

                // Credits  starts here
                if (castDetailsCredits != null)
                    send.putParcelableArrayList("moviesList", moviesList);

                // Credits ends here

                // Overview
                if (castDetailsBiography != null)
                    send.putString("biography", castDetailsBiography.getBiography().getText().toString());

            }

            outState.putBundle("save", send);
            save = send;
            if (addToBackStack) {
                activity.addCastDetailsBundle(send);
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
        activity.setCastDetailsInfoBundle(save);
        activity.setCastDetailsCreditsBundle(save);
        activity.setCastDetailsBiographyBundle(save);

        moviesList = save.getParcelableArrayList("moviesList");
        if (moviesList != null && moviesList.size() == 0) {
            noCredits = true;
            mSlidingTabLayout.disableTabClickListener(1);
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                castDetailsInfo = (CastDetailsInfo) castDetailsSlideAdapter.getRegisteredFragment(0);
                castDetailsCredits = (CastDetailsCredits) castDetailsSlideAdapter.getRegisteredFragment(1);
                if (currPos == 0) {
                    moreIcon.setVisibility(View.INVISIBLE);
                } else if (moreIconCheck == 0) {
                    castDetailsInfo.getMoreIcon().setVisibility(View.INVISIBLE);
                    updateDownPos();
                }
                if (moreIconCheck == 1)
                    castDetailsInfo.getMoreIcon().setVisibility(View.GONE);
                else {
                    // set listener on backdrop click to open gallery
                    if (galleryIconCheck == 0 && galleryList.size() > 0)
                        castDetailsInfo.getProfilePath().setOnClickListener(onGalleryIconClick);
                    adjustIconsPos(homeIcon, galleryIcon);
                    adjustIconsPos(castDetailsInfo.getHomeIcon(), castDetailsInfo.getGalleryIcon());
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

                if (castDetailsCredits.getMoviesList().size() < 7)
                    activity.hideView(castDetailsInfo.getShowMoreButton());

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
            currScroll = castDetailsInfo.getRootView().getScrollY();

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
                    castDetailsInfo.getMoreIcon().setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_close_white_36dp));
                    showHideImages(View.VISIBLE, castDetailsInfo.getHomeIcon(), castDetailsInfo.getGalleryIcon());
                } else {
                    moreIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_close_white_36dp));
                    showHideImages(View.VISIBLE, homeIcon, galleryIcon);
                }
                key = true;
            } else {
                if (currPos == 0) {
                    castDetailsInfo.getMoreIcon().setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_more_vert_white_36dp));
                    showHideImages(View.INVISIBLE, castDetailsInfo.getHomeIcon(), castDetailsInfo.getGalleryIcon());
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
                activity.incSearchCastDetails();
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
     * Class which listens when the user has changed the tap in Cast details
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
            if (noCredits) {
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
                if (castDetailsInfo != null) {
                    if (119 * scale <= (castDetailsInfo.getScrollView().getChildAt(0).getHeight() - (567 * scale)) && castDetailsInfo.canScroll()) {
                        if (toolbarHidden) {
                            final ObservableParallaxScrollView scrollView = castDetailsInfo.getScrollView();
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


            if (position == 1 && castDetailsCredits != null) {
                if (castDetailsCredits.getMoviesList() != null && castDetailsCredits.getMoviesList().size() > 0) {
                    final AbsListView listView = castDetailsCredits.getListView();
                    if (castDetailsCredits.canScroll()) {
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

            if (position == 2 && castDetailsBiography != null) {
                castDetailsBiography.getScrollView().post(new Runnable() {
                    @Override
                    public void run() {
                        if (toolbarHidden)
                            castDetailsBiography.getScrollView().scrollTo(0, (int) (56 * scale));
                        else
                            castDetailsBiography.getScrollView().scrollTo(0, 0);
                    }
                });
            }

            if (moreIconCheck == 0) {
                if (castDetailsInfo != null) {
                    castDetailsInfoScrollY = castDetailsInfo.getRootView().getScrollY();

                    galleryIcon.clearAnimation();
                    homeIcon.clearAnimation();

                    homeIcon.setVisibility(View.INVISIBLE);
                    castDetailsInfo.getHomeIcon().setVisibility(View.INVISIBLE);

                    galleryIcon.setVisibility(View.INVISIBLE);
                    castDetailsInfo.getGalleryIcon().setVisibility(View.INVISIBLE);

                    moreIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_more_vert_white_36dp));

                    if (position == 0) {
                        castDetailsInfo.getMoreIcon().setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_more_vert_white_36dp));
                        moreIcon.setVisibility(View.INVISIBLE);
                    } else {
                        castDetailsInfo.getMoreIcon().setVisibility(View.INVISIBLE);
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
                            createUpAnimation((119 * scale) - castDetailsInfo.getScrollView().getCurrentScrollY());
                        } else
                            createUpAnimation(0);
                        moreIcon.startAnimation(upAnimation);
                    }
                    if (currPos == 2 && position == 0) {
                        updateDownPos();
                        if (infoTabScrollPosUpdated) {
                            infoTabScrollPosUpdated = false;
                            createUpAnimation((119 * scale) - castDetailsInfo.getScrollView().getCurrentScrollY());
                        } else
                            createUpAnimation(0);
                        moreIcon.startAnimation(upAnimation);
                    }
                }
            }

            if (!noCredits || position != 1)
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
            castDetailsInfo.getMoreIcon().clearAnimation();
            castDetailsInfo.getMoreIcon().setVisibility(View.VISIBLE);
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

        lp.setMargins(0, (int) (scale * (346 + iconMarginConstant) + 0.5f - castDetailsInfoScrollY), (int) (scale * 15 + 0.5f), 0);
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
            downAnimation = new TranslateAnimation(0, 0, 0, -scale * 150 + 0.5f + castDetailsInfoScrollY);
        else
            downAnimation = new TranslateAnimation(0, 0, 0, scale * (150 + iconConstantSpecialCase) + 0.5f + castDetailsInfoScrollY);
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
            upAnimation = new TranslateAnimation(0, 0, 0, scale * 150 + 0.5f - castDetailsInfoScrollY - dy);
        else
            upAnimation = new TranslateAnimation(0, 0, 0, -scale * (150 + iconConstantSpecialCase) + 0.5f - castDetailsInfoScrollY - dy);
        upAnimation.setDuration(500);
        upAnimation.setFillAfter(false);
        upAnimation.setAnimationListener(upAnimationListener);
    }

    public CastDetails.onMoreIconClick getOnMoreIconClick() {
        return onMoreIconClick;
    }

    public void setAddToBackStack(boolean addToBackStack) {
        this.addToBackStack = addToBackStack;
    }

    public Bundle getSave() {
        return save;
    }

    public void setSave(Bundle save) {
        this.save = save;
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
                castDetailsInfo.getHomeIcon().clearAnimation();
                castDetailsInfo.getGalleryIcon().clearAnimation();
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
                castDetailsInfo.getHomeIcon().clearAnimation();
                castDetailsInfo.getGalleryIcon().clearAnimation();

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
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) castDetailsInfo.getGalleryIcon().getLayoutParams();
        p.removeRule(RelativeLayout.ABOVE);
        p.removeRule(RelativeLayout.BELOW);
        p.addRule(RelativeLayout.ABOVE, R.id.moreIcon);
        p.setMargins(0, 0, (int) (23 * scale), (int) (-20 * scale));
        castDetailsInfo.getGalleryIcon().setLayoutParams(p);
        castDetailsInfo.getHomeIcon().setLayoutParams(p);
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

        castDetailsInfo.getHomeIcon().setLayoutParams(p3);
        castDetailsInfo.getGalleryIcon().setLayoutParams(p);
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
                return (Scrollable) view.findViewById(R.id.castdetailsinfo);
            case 1:
                return (Scrollable) view.findViewById(R.id.castdetailscredits);
            case 2:
                return (Scrollable) view.findViewById(R.id.castdetailsbiography);
            default:
                return (Scrollable) view.findViewById(R.id.castdetailsinfo);
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
        return castDetailsSlideAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
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

    public ViewPager getmViewPager() {
        return mViewPager;
    }

    private ArrayList<MovieModel> removeDuplicates(List<MovieModel> l) {

        Set<MovieModel> set = new TreeSet<>(new Comparator<MovieModel>() {

            @Override
            public int compare(MovieModel o1, MovieModel o2) {
                if (o1.getId() == o2.getId())
                    return 0;
                else return 1;
            }
        });
        set.addAll(l);
        ArrayList<MovieModel> movieListWithoutDuplicates = new ArrayList<>();
        movieListWithoutDuplicates.addAll(set);
        return movieListWithoutDuplicates;
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
        movieModel = null;
    }

}
