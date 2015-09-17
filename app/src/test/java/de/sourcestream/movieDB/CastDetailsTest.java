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

import de.sourcestream.movieDB.adapter.CastDetailsSlideAdapter;
import de.sourcestream.movieDB.controller.CastDetails;
import de.sourcestream.movieDB.controller.CastDetailsInfo;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class CastDetailsTest extends InstrumentationTestCase {

    private static final String FRAGMENT_TAG = "fragment";
    private MainActivity activity;
    private CastDetails castDetailsFragment;
    private View castDetailsFragmentView;
    private CircledImageView moreIcon;
    private CircledImageView homeIcon;
    private CircledImageView galleryIcon;


    /**
     * Adds the fragment to a new blank activity, thereby fully
     * initializing its view.
     */
    @Before
    public void setUp() {
        castDetailsFragment = new CastDetails();
        activity = Robolectric.buildActivity(MainActivity.class).create().get();
        FragmentManager manager = activity.getFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt("id", 1);
        castDetailsFragment.setArguments(bundle);
        manager.beginTransaction().add(castDetailsFragment, FRAGMENT_TAG).commit();

        castDetailsFragmentView = castDetailsFragment.getView();
        moreIcon = (CircledImageView) castDetailsFragmentView.findViewById(R.id.moreIcon);
        homeIcon = (CircledImageView) castDetailsFragmentView.findViewById(R.id.homeIcon);
        galleryIcon = (CircledImageView) castDetailsFragmentView.findViewById(R.id.galleryIcon);

    }

    @Test
    public void testPreconditions() throws Exception {
        assertNotNull("activity is null", activity);
        assertNotNull("castDetailsFragment is null", castDetailsFragment);
        assertNotNull("cant find fragment", activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG));
        assertNotNull("castDetailsFragmentView null", castDetailsFragmentView);

        assertNotNull("castDetailsFragment moreIcon is null", moreIcon);
        assertNotNull("castDetailsFragment galleryIcon is null", homeIcon);
        assertNotNull("castDetailsFragment galleryIcon is null", galleryIcon);
    }


    @Test
    public void testButtonVisibility() throws Exception {
        int expected = View.INVISIBLE;
        assertEquals("moreIcon visibility is different!", expected, moreIcon.getVisibility());
        assertEquals("homeIcon visibility is different!", expected, homeIcon.getVisibility());
        assertEquals("galleryIcon visibility is different!", expected, galleryIcon.getVisibility());

    }

    @Test
    public void testCastDetailsInfo() throws Exception {
        ViewPager mViewPager = (ViewPager) castDetailsFragmentView.findViewById(R.id.castDetailsPager);
        CastDetailsSlideAdapter adapter = (CastDetailsSlideAdapter) mViewPager.getAdapter();

        assertNotNull("rootView is null", castDetailsFragmentView);
        assertNotNull("mViewPager is null", mViewPager);
        assertNotNull("adapter is null", adapter);

        CastDetailsInfo castDetailsInfo = new CastDetailsInfo();
        FragmentManager manager = activity.getFragmentManager();
        manager.beginTransaction().add(castDetailsInfo, "castDetailsInfo").commit();

     //   CastDetailsInfo castDetailsInfo = (CastDetailsInfo) adapter.getItem(0);
        assertNotNull("cast details info is null", castDetailsInfo);
        assertNotNull("cast details info rootView is null", castDetailsInfo.getRootView());

    }


}
