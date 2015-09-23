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
import android.support.v4.view.ViewPager;
import android.test.InstrumentationTestCase;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;


import de.sourcestream.movieDB.controller.MovieSlideTab;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class MovieSlideTabTest extends InstrumentationTestCase {

    private static final String FRAGMENT_TAG = "fragment";
    private MainActivity activity;
    private MovieSlideTab movieSlideTab;


    /**
     * Adds the fragment to a new blank activity, thereby fully
     * initializing its view.
     */
    @Before
    public void setUp() throws Exception {
        movieSlideTab = new MovieSlideTab();
        activity = Robolectric.buildActivity(MainActivity.class).create().get();
        FragmentManager manager = activity.getFragmentManager();
        manager.beginTransaction().add(movieSlideTab, FRAGMENT_TAG).commit();
    }

    @Test
    public void testPreconditions() throws Exception {
        assertNotNull("activity is null", activity);
        assertNotNull("DrawerLayout is null", activity.getMDrawerLayout());
        assertNotNull("movieSlideTab is null", movieSlideTab);
        assertNotNull("cant find fragment", activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG));
    }

    @Test
    public void testViews() throws Exception {
        View movieSlideTabRoot = movieSlideTab.getView();
        assertNotNull("movieSlideTab rootView is null", movieSlideTabRoot);
        assertNotNull("movieSlideTab slidingTab is null", movieSlideTabRoot.findViewById(R.id.sliding_tabs));
        ViewPager mViewPager = (ViewPager) movieSlideTabRoot.findViewById(R.id.moviePager);
        int expected = 1;
        assertNotNull("movieSlideTab mViewPager is null", mViewPager);
        assertNotNull("movieSlideTab mViewPager adapter is null", mViewPager.getAdapter());
        assertEquals("mViewPager offScreenLimit is different!", expected, mViewPager.getOffscreenPageLimit());
    }

}
