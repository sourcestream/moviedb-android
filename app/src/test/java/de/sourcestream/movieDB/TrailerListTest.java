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
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.test.InstrumentationTestCase;
import android.widget.AbsListView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import de.sourcestream.movieDB.controller.TrailerList;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class TrailerListTest extends InstrumentationTestCase {

    private static final String FRAGMENT_TAG = "fragment";
    private MainActivity activity;
    private TrailerList trailerListFragment;


    /**
     * Adds the fragment to a new blank activity, thereby fully
     * initializing its view.
     */
    @Before
    public void setUp() throws Exception {
        trailerListFragment = new TrailerList();
        activity = Robolectric.buildActivity(MainActivity.class).create().get();
        FragmentManager manager = activity.getFragmentManager();
        Bundle args = new Bundle();
        ArrayList<String> trailerList = new ArrayList<>();
        args.putStringArrayList("trailerList", trailerList);
        trailerListFragment.setArguments(args);
        manager.beginTransaction().add(trailerListFragment, FRAGMENT_TAG).commit();
    }

    @Test
    public void testPreconditions() throws Exception {
        assertNotNull("activity is null", activity);
        assertNotNull("trailerListFragment is null", trailerListFragment);
        assertNotNull("cant find fragment", activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG));
    }

    @Test
    public void testViews() throws Exception {
        assertNotNull("trailerListFragment rootView is null", trailerListFragment.getView());
        assertNotNull("trailerListFragment gridView is null", trailerListFragment.getView().findViewById(R.id.gridView));
        AbsListView listView = (AbsListView) trailerListFragment.getView().findViewById(R.id.gridView);
        assertNotNull("no listener added on listView", listView.getOnItemClickListener());
        assertNotNull("no adapter set on listView", listView.getAdapter());
    }

    @Test
    public void testBackgroundColor() throws Exception {
        int expected = ContextCompat.getColor(activity, R.color.background_material_light);
        ColorDrawable actualDrawable = (ColorDrawable) activity.getWindow().getDecorView().getBackground();
        int actual = actualDrawable.getColor();
        assertEquals("Background color is different!", expected, actual);
    }


}
