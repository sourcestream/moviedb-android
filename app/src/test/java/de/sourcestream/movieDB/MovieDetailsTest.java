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

package de.sourcestream.movieDB;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.wearable.view.CircledImageView;
import android.test.InstrumentationTestCase;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import de.sourcestream.movieDB.controller.MovieDetails;
import de.sourcestream.movieDB.controller.MovieDetailsCast;
import de.sourcestream.movieDB.controller.MovieDetailsInfo;
import de.sourcestream.movieDB.controller.MovieDetailsOverview;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class MovieDetailsTest extends InstrumentationTestCase {

    private static final String FRAGMENT_TAG = "fragment";
    private MainActivity activity;
    private MovieDetails movieDetailsFragment;
    private View movieDetailsFragmentView;
    private CircledImageView moreIcon;
    private CircledImageView homeIcon;
    private CircledImageView galleryIcon;
    private CircledImageView trailerIcon;


    /**
     * Adds the fragment to a new blank activity, thereby fully
     * initializing its view.
     */
    @Before
    public void setUp() {
        movieDetailsFragment = new MovieDetails();
        activity = Robolectric.buildActivity(MainActivity.class).create().get();
        FragmentManager manager = activity.getFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt("id", 1);
        movieDetailsFragment.setArguments(bundle);
        manager.beginTransaction().add(movieDetailsFragment, FRAGMENT_TAG).commit();

        movieDetailsFragmentView = movieDetailsFragment.getView();
        moreIcon = (CircledImageView) movieDetailsFragmentView.findViewById(R.id.moreIcon);
        homeIcon = (CircledImageView) movieDetailsFragmentView.findViewById(R.id.homeIcon);
        galleryIcon = (CircledImageView) movieDetailsFragmentView.findViewById(R.id.galleryIcon);
        trailerIcon = (CircledImageView) movieDetailsFragmentView.findViewById(R.id.trailerIcon);
    }

    @Test
    public void testPreconditions() throws Exception {
        assertNotNull("activity is null", activity);
        assertNotNull("movieDetailsFragment is null", movieDetailsFragment);
        assertNotNull("cant find fragment", activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG));
        assertNotNull("movieDetailsFragmentView null", movieDetailsFragmentView);

        assertNotNull("movieDetailsFragment moreIcon is null", moreIcon);
        assertNotNull("movieDetailsFragment homeIcon is null", homeIcon);
        assertNotNull("movieDetailsFragment galleryIcon is null", galleryIcon);
        assertNotNull("movieDetailsFragment progressBar is null", movieDetailsFragmentView.findViewById(R.id.progressBar));

        assertNotNull("movieDetailsFragment slidingTab is null", movieDetailsFragmentView.findViewById(R.id.sliding_tabs));
        ViewPager mViewPager = (ViewPager) movieDetailsFragmentView.findViewById(R.id.movieDetailsPager);
        int expected = 3;
        assertNotNull("movieDetailsFragment mViewPager is null", mViewPager);
        assertNotNull("movieDetailsFragment mViewPager adapter is null", mViewPager.getAdapter());
        assertEquals("mViewPager offScreenLimit is different!", expected, mViewPager.getOffscreenPageLimit());
    }


    @Test
    public void testButtonVisibility() throws Exception {
        int expected = View.INVISIBLE;
        assertEquals("moreIcon visibility is different!", expected, moreIcon.getVisibility());
        assertEquals("homeIcon visibility is different!", expected, homeIcon.getVisibility());
        assertEquals("galleryIcon visibility is different!", expected, galleryIcon.getVisibility());
        assertEquals("trailerIcon visibility is different!", expected, trailerIcon.getVisibility());

    }

    @Test
    public void testMovieDetailsInfo() throws Exception {
        assertNotNull("rootView is null", movieDetailsFragmentView);

        MovieDetailsInfo movieDetailsInfo = new MovieDetailsInfo();
        FragmentManager manager = activity.getFragmentManager();
        manager.beginTransaction().add(movieDetailsInfo, "movieDetailsInfo").commit();

        //   CastDetailsInfo castDetailsInfo = (CastDetailsInfo) adapter.getItem(0);
        assertNotNull("movie details info is null", movieDetailsInfo);

        View movieDetailsInfoRoot = movieDetailsInfo.getRootView();
        assertNotNull("movie details info rootView is null", movieDetailsInfoRoot);

        assertNotNull("backDropPath is null", movieDetailsInfoRoot.findViewById(R.id.backDropPath));
        assertNotNull("title is null", movieDetailsInfoRoot.findViewById(R.id.title));
        assertNotNull("releaseDate is null", movieDetailsInfoRoot.findViewById(R.id.releaseDate));
        assertNotNull("posterPath is null", movieDetailsInfoRoot.findViewById(R.id.posterPath));
        assertNotNull("tagline is null", movieDetailsInfoRoot.findViewById(R.id.tagline));
        assertNotNull("status is null", movieDetailsInfoRoot.findViewById(R.id.status));
        assertNotNull("runtime is null", movieDetailsInfoRoot.findViewById(R.id.runtime));
        assertNotNull("genres is null", movieDetailsInfoRoot.findViewById(R.id.genres));
        assertNotNull("countries is null", movieDetailsInfoRoot.findViewById(R.id.countries));
        assertNotNull("companies is null", movieDetailsInfoRoot.findViewById(R.id.companies));
        assertNotNull("ratingBar is null", movieDetailsInfoRoot.findViewById(R.id.ratingBar));
        assertNotNull("voteCount is null", movieDetailsInfoRoot.findViewById(R.id.voteCount));

        CircledImageView moreIcon = (CircledImageView) movieDetailsInfoRoot.findViewById(R.id.moreIcon);
        CircledImageView homeIcon = (CircledImageView) movieDetailsInfoRoot.findViewById(R.id.homeIcon);
        CircledImageView galleryIcon = (CircledImageView) movieDetailsInfoRoot.findViewById(R.id.galleryIcon);
        CircledImageView trailerIcon = (CircledImageView) movieDetailsInfoRoot.findViewById(R.id.trailerIcon);

        assertNotNull("moreIcon is null", moreIcon);
        assertNotNull("homeIcon is null", homeIcon);
        assertNotNull("galleryIcon is null", galleryIcon);
        assertNotNull("trailerIcon is null", trailerIcon);

        int expected = View.INVISIBLE;
        assertEquals("moreIcon visibility is different!", View.VISIBLE, moreIcon.getVisibility());
        assertEquals("homeIcon visibility is different!", expected, homeIcon.getVisibility());
        assertEquals("galleryIcon visibility is different!", expected, galleryIcon.getVisibility());
        assertEquals("trailerIcon visibility is different!", expected, trailerIcon.getVisibility());

        assertNotNull("moviedetailsinfo is null", movieDetailsInfoRoot.findViewById(R.id.moviedetailsinfo));
        assertNotNull("movieDetailsSimilarGrid is null", movieDetailsInfoRoot.findViewById(R.id.movieDetailsSimilarGrid));
        assertNotNull("similarHolder is null", movieDetailsInfoRoot.findViewById(R.id.similarHolder));
        assertNotNull("detailsLayout is null", movieDetailsInfoRoot.findViewById(R.id.detailsLayout));

    }

    @Test
    public void testMovieDetailsCast() throws Exception {
        MovieDetailsCast movieDetailsCast = new MovieDetailsCast();
        FragmentManager manager = activity.getFragmentManager();
        manager.beginTransaction().add(movieDetailsCast, "movieDetailsCast").commit();

        assertNotNull("movie details credits is null", movieDetailsCast);
        assertNotNull("movie details credits view is null", movieDetailsCast.getView());
        assertNotNull("movie details credits listView is null", movieDetailsCast.getListView());
    }

    @Test
    public void testMovieDetailsOverview() throws Exception {
        MovieDetailsOverview movieDetailsOverview = new MovieDetailsOverview();
        FragmentManager manager = activity.getFragmentManager();
        manager.beginTransaction().add(movieDetailsOverview, "movieDetailsOverview").commit();

        assertNotNull("movie details overview is null", movieDetailsOverview);
        View movieDetailsOverviewRoot = movieDetailsOverview.getView();
        assertNotNull("movie details overview is null", movieDetailsOverviewRoot);
        assertNotNull("biographyContent is null", movieDetailsOverviewRoot.findViewById(R.id.overviewContent));
        assertNotNull("scrollView is null", movieDetailsOverviewRoot.findViewById(R.id.moviedetailsoverview));

    }
}
