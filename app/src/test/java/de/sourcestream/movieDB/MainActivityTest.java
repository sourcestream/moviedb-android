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

import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import de.sourcestream.movieDB.controller.GalleryList;
import de.sourcestream.movieDB.controller.GenresList;
import de.sourcestream.movieDB.controller.MovieSlideTab;
import de.sourcestream.movieDB.controller.TVSlideTab;
import de.sourcestream.movieDB.controller.TrailerList;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity activity;
    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private MovieSlideTab movieSlideTab;
    private TVSlideTab tvSlideTab;
    private GenresList genresList;
    private GalleryList galleryList;
    private TrailerList trailerList;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(MainActivity.class).create().get();
        mDrawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) activity.findViewById(R.id.list_slidermenu);
        toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        movieSlideTab = activity.getMovieSlideTab();
        tvSlideTab = activity.getTvSlideTab();
        genresList = activity.getGenresList();
        galleryList = activity.getGalleryListView();
        trailerList = activity.getTrailerListView();
    }

    @Test
    public void testPreconditions() throws Exception {
        assertNotNull("activity is null", activity);
        assertNotNull("mDrawerLayout is null", mDrawerLayout);
        assertNotNull("mDrawerList is null", mDrawerList);
        assertNotNull("toolbar is null", toolbar);
        assertNotNull("movieSlideTab is null", movieSlideTab);
        assertNotNull("tvSlideTab is null", tvSlideTab);
        assertNotNull("genresList is null", genresList);

        assertNotNull("galleryList is null", galleryList);
        assertNotNull("trailerList is null", trailerList);
    }

    @Test
    public void testToolbar_same() throws Exception {
        Toolbar expected = toolbar;
        Toolbar actual = activity.getToolbar();
        assertEquals("Toolbar is different!", expected.hashCode(), actual.hashCode());
    }

    @Test
    public void testToolbar_title() throws Exception {
        String expected[] = activity.getResources().getStringArray(R.array.nav_drawer_items);
        String actual = toolbar.getTitle().toString();
        assertEquals("Toolbar title is different!", expected[0], actual);
    }

    @Test
    public void testDrawerList_count() throws Exception {
        int expected = 5;
        int expectedHeader = 1;
        assertEquals("Drawer list items conflict!", expected, mDrawerList.getCount());
        assertEquals("Drawer list header different value!", expectedHeader, mDrawerList.getHeaderViewsCount());
    }

    @Test
    public void testDrawerList_itemClickListener() throws Exception {
        assertNotNull(mDrawerList.getOnItemClickListener());
    }

    @Test
    public void testDrawerList_adapter() throws Exception {
        assertNotNull(mDrawerList.getAdapter());
    }

    @Test
    public void testValues() throws Exception {
        int expected = 0, expectedOldPos = 1;
        assertEquals("Value in Movie pager is different!", expected, activity.getCurrentMovViewPagerPos());
        assertEquals("Value in TV pager is different!", expected, activity.getCurrentTVViewPagerPos());
        assertEquals("Value oldPos is different", expectedOldPos, activity.getOldPos());
        assertEquals("Value lastVisitedSimMovie is different", expected, activity.getLastVisitedSimMovie());
        assertEquals("Value lastVisitedSimTV is different", expected, activity.getLastVisitedSimTV());
        assertEquals("Value lastVisitedMovieInCredits is different", expected, activity.getLastVisitedMovieInCredits());
    }


}