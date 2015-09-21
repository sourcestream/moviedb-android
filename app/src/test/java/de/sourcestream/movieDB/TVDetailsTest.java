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

import de.sourcestream.movieDB.controller.TVDetails;
import de.sourcestream.movieDB.controller.TVDetailsCast;
import de.sourcestream.movieDB.controller.TVDetailsInfo;
import de.sourcestream.movieDB.controller.TVDetailsOverview;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class TVDetailsTest extends InstrumentationTestCase {

    private static final String FRAGMENT_TAG = "fragment";
    private MainActivity activity;
    private TVDetails tvDetailsFragment;
    private View tvDetailsFragmentView;
    private CircledImageView moreIcon;
    private CircledImageView homeIcon;
    private CircledImageView galleryIcon;


    /**
     * Adds the fragment to a new blank activity, thereby fully
     * initializing its view.
     */
    @Before
    public void setUp() {
        tvDetailsFragment = new TVDetails();
        activity = Robolectric.buildActivity(MainActivity.class).create().get();
        FragmentManager manager = activity.getFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt("id", 1);
        tvDetailsFragment.setArguments(bundle);
        manager.beginTransaction().add(tvDetailsFragment, FRAGMENT_TAG).commit();

        tvDetailsFragmentView = tvDetailsFragment.getView();
        moreIcon = (CircledImageView) tvDetailsFragmentView.findViewById(R.id.moreIcon);
        homeIcon = (CircledImageView) tvDetailsFragmentView.findViewById(R.id.homeIcon);
        galleryIcon = (CircledImageView) tvDetailsFragmentView.findViewById(R.id.galleryIcon);
    }

    @Test
    public void testPreconditions() throws Exception {
        assertNotNull("activity is null", activity);
        assertNotNull("tvDetailsFragment is null", tvDetailsFragment);
        assertNotNull("cant find fragment", activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG));
        assertNotNull("tvDetailsFragmentView null", tvDetailsFragmentView);

        assertNotNull("tvDetailsFragment moreIcon is null", moreIcon);
        assertNotNull("tvDetailsFragment homeIcon is null", homeIcon);
        assertNotNull("tvDetailsFragment galleryIcon is null", galleryIcon);
        assertNotNull("tvDetailsFragment progressBar is null", tvDetailsFragmentView.findViewById(R.id.progressBar));

        assertNotNull("tvDetailsFragment slidingTab is null", tvDetailsFragmentView.findViewById(R.id.sliding_tabs));
        ViewPager mViewPager = (ViewPager) tvDetailsFragmentView.findViewById(R.id.tvDetailsPager);
        int expected = 3;
        assertNotNull("tvDetailsFragment mViewPager is null", mViewPager);
        assertNotNull("tvDetailsFragment mViewPager adapter is null", mViewPager.getAdapter());
        assertEquals("mViewPager offScreenLimit is different!", expected, mViewPager.getOffscreenPageLimit());
    }


    @Test
    public void testButtonVisibility() throws Exception {
        int expected = View.INVISIBLE;
        assertEquals("moreIcon visibility is different!", expected, moreIcon.getVisibility());
        assertEquals("homeIcon visibility is different!", expected, homeIcon.getVisibility());
        assertEquals("galleryIcon visibility is different!", expected, galleryIcon.getVisibility());

    }

    @Test
    public void testTVDetailsInfo() throws Exception {
        assertNotNull("rootView is null", tvDetailsFragmentView);

        TVDetailsInfo tvDetailsInfo = new TVDetailsInfo();
        FragmentManager manager = activity.getFragmentManager();
        manager.beginTransaction().add(tvDetailsInfo, "tvDetailsInfo").commit();

        //   CastDetailsInfo castDetailsInfo = (CastDetailsInfo) adapter.getItem(0);
        assertNotNull("tv details info is null", tvDetailsInfo);

        View tvDetailsInfoRoot = tvDetailsInfo.getRootView();
        assertNotNull("tv details info rootView is null", tvDetailsInfoRoot);

        assertNotNull("backDropPath is null", tvDetailsInfoRoot.findViewById(R.id.backDropPath));
        assertNotNull("title is null", tvDetailsInfoRoot.findViewById(R.id.title));
        assertNotNull("posterPath is null", tvDetailsInfoRoot.findViewById(R.id.posterPath));
        assertNotNull("statusText is null", tvDetailsInfoRoot.findViewById(R.id.status));
        assertNotNull("type is null", tvDetailsInfoRoot.findViewById(R.id.type));
        assertNotNull("episodeRuntime is null", tvDetailsInfoRoot.findViewById(R.id.episodeRuntime));
        assertNotNull("numberOfEpisodes is null", tvDetailsInfoRoot.findViewById(R.id.numberOfEpisodes));
        assertNotNull("numberOfSeasons is null", tvDetailsInfoRoot.findViewById(R.id.numberOfSeasons));
        assertNotNull("firstAirDate is null", tvDetailsInfoRoot.findViewById(R.id.firstAirDate));
        assertNotNull("lastAirDate is null", tvDetailsInfoRoot.findViewById(R.id.lastAirDate));
        assertNotNull("genres is null", tvDetailsInfoRoot.findViewById(R.id.genres));
        assertNotNull("countries is null", tvDetailsInfoRoot.findViewById(R.id.countries));
        assertNotNull("companies is null", tvDetailsInfoRoot.findViewById(R.id.companies));
        assertNotNull("ratingBar is null", tvDetailsInfoRoot.findViewById(R.id.ratingBar));
        assertNotNull("voteCount is null", tvDetailsInfoRoot.findViewById(R.id.voteCount));


        CircledImageView moreIcon = (CircledImageView) tvDetailsInfoRoot.findViewById(R.id.moreIcon);
        CircledImageView homeIcon = (CircledImageView) tvDetailsInfoRoot.findViewById(R.id.homeIcon);
        CircledImageView galleryIcon = (CircledImageView) tvDetailsInfoRoot.findViewById(R.id.galleryIcon);

        assertNotNull("moreIcon is null", moreIcon);
        assertNotNull("homeIcon is null", homeIcon);
        assertNotNull("galleryIcon is null", galleryIcon);

        int expected = View.INVISIBLE;
        assertEquals("moreIcon visibility is different!", View.VISIBLE, moreIcon.getVisibility());
        assertEquals("homeIcon visibility is different!", expected, homeIcon.getVisibility());
        assertEquals("galleryIcon visibility is different!", expected, galleryIcon.getVisibility());

        assertNotNull("tvDetailsInfo is null", tvDetailsInfoRoot.findViewById(R.id.tvdetailsinfo));
        assertNotNull("movieDetailsSimilarGrid is null", tvDetailsInfoRoot.findViewById(R.id.tvDetailsSimilarGrid));
        assertNotNull("similarHolder is null", tvDetailsInfoRoot.findViewById(R.id.similarHolder));
        assertNotNull("detailsLayout is null", tvDetailsInfoRoot.findViewById(R.id.detailsLayout));

    }

    @Test
    public void testTVDetailsCast() throws Exception {
        TVDetailsCast tvDetailsCast = new TVDetailsCast();
        FragmentManager manager = activity.getFragmentManager();
        manager.beginTransaction().add(tvDetailsCast, "tvDetailsCast").commit();

        assertNotNull("tv details credits is null", tvDetailsCast);
        assertNotNull("tv details credits view is null", tvDetailsCast.getView());
        assertNotNull("tv details credits listView is null", tvDetailsCast.getListView());
    }

    @Test
    public void testTVDetailsOverview() throws Exception {
        TVDetailsOverview tvDetailsOverview = new TVDetailsOverview();
        FragmentManager manager = activity.getFragmentManager();
        manager.beginTransaction().add(tvDetailsOverview, "tvDetailsOverview").commit();

        assertNotNull("tv details overview is null", tvDetailsOverview);
        View movieDetailsOverviewRoot = tvDetailsOverview.getView();
        assertNotNull("tv details overview is null", movieDetailsOverviewRoot);
        assertNotNull("biographyContent is null", movieDetailsOverviewRoot.findViewById(R.id.overviewContent));
        assertNotNull("scrollView is null", movieDetailsOverviewRoot.findViewById(R.id.tvdetailsoverview));

    }
}
