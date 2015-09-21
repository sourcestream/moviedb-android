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
import android.view.View;
import android.widget.AbsListView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import de.sourcestream.movieDB.controller.GenresList;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class GenresTest extends InstrumentationTestCase {

    private static final String FRAGMENT_TAG = "fragment";
    private MainActivity activity;
    private GenresList genresList;


    /**
     * Adds the fragment to a new blank activity, thereby fully
     * initializing its view.
     */
    @Before
    public void setUp() throws Exception {
        genresList = new GenresList();
        activity = Robolectric.buildActivity(MainActivity.class).create().get();
        FragmentManager manager = activity.getFragmentManager();
        manager.beginTransaction().add(genresList, FRAGMENT_TAG).commit();
    }

    @Test
    public void testPreconditions() throws Exception {
        assertNotNull("activity is null", activity);
        assertNotNull("DrawerLayout is null", activity.getMDrawerLayout());
        assertNotNull("genresList is null", genresList);
        assertNotNull("cant find fragment", activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG));
    }

    @Test
    public void testViews() throws Exception {
        View genresListRoot = genresList.getView();
        assertNotNull("genresList rootView is null", genresListRoot);
        assertNotNull("progressBar is null", genresListRoot.findViewById(R.id.progressBar));
        AbsListView listView = (AbsListView) genresListRoot.findViewById(R.id.genresList);
        assertNotNull("listView is null", listView);
        assertNotNull("listView adapter is null", listView.getAdapter());
        assertNotNull("listView listener is null", listView.getOnItemClickListener());

    }

}
