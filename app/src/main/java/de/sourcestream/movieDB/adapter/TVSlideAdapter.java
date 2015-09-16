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

package de.sourcestream.movieDB.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import de.sourcestream.movieDB.R;
import de.sourcestream.movieDB.controller.TVList;

/**
 * The {@link android.support.v4.view.PagerAdapter} used to display pages in this sample.
 * The individual pages are simple and just display two lines of text. The important section of
 * this class is the {@link #getPageTitle(int)} method which controls what is displayed in the
 * {@link de.sourcestream.movieDB.view.SlidingTabLayout}.
 */
public class TVSlideAdapter extends FragmentPagerAdapter {
    private String[] navMenuTitles;
    private FragmentManager manager;
    private FragmentTransaction mCurTransaction = null;
    private Resources res;

    public TVSlideAdapter(FragmentManager fm, Resources res) {
        super(fm);
        this.manager = fm;
        navMenuTitles = res.getStringArray(R.array.tvTabs);
        this.res = res;
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return 4;
    }

    /**
     * Return the Fragment associated with a specified position.
     * @param position Position within this adapter
     * @return Unique identifier for the item at position
     */
    @Override
    public Fragment getItem(int position) {
        String onTheAir = "tv/on_the_air";
        String airingToday = "tv/airing_today";
        String popular = "tv/popular";
        String topRated = "tv/top_rated";
        Bundle args = new Bundle();
        switch (position) {
            case 0:
                args.putString("currentList", "onTheAir");
                TVList onTheAirList = new TVList();
                onTheAirList.setTitle(res.getString(R.string.tvTitle));
                onTheAirList.setArguments(args);
                onTheAirList.setCurrentList(onTheAir);
                return onTheAirList;
            case 1:
                args.putString("currentList", "airingToday");
                TVList airingTodayList = new TVList();
                airingTodayList.setTitle(res.getString(R.string.tvTitle));
                airingTodayList.setArguments(args);
                airingTodayList.setCurrentList(airingToday);
                return airingTodayList;
            case 2:
                args.putString("currentList", "popular");
                TVList popularList = new TVList();
                popularList.setTitle(res.getString(R.string.tvTitle));
                popularList.setArguments(args);
                popularList.setCurrentList(popular);
                return popularList;
            case 3:
                args.putString("currentList", "topRated");
                TVList topRatedList = new TVList();
                topRatedList.setTitle(res.getString(R.string.tvTitle));
                topRatedList.setArguments(args);
                topRatedList.setCurrentList(topRated);
                return topRatedList;
            default:
                return null;
        }

    }

    /**
     * This method may be called by the ViewPager to obtain a title string to describe the specified page.
     * @param position The position of the title requested
     * @return A title for the requested page
     */
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return navMenuTitles[0];
            case 1:
                return navMenuTitles[1];
            case 2:
                return navMenuTitles[2];
            case 3:
                return navMenuTitles[3];
            default:
                return navMenuTitles[1];
        }
    }

    /**
     * @param container - our Viewpager
     * Fired when we are in Movie or TV details and we pressed back button.
     * Recreates our fragments.
     */
    public void reAttachFragments(ViewGroup container) {
        if (mCurTransaction == null) {
            mCurTransaction = manager.beginTransaction();
        }

        for (int i = 0; i < getCount(); i++) {

            final long itemId = getItemId(i);

            // Do we already have this fragment?
            String name = "android:switcher:" + container.getId() + ":" + itemId;
            Fragment fragment = manager.findFragmentByTag(name);

            if (fragment != null) {
                mCurTransaction.detach(fragment);
            }
        }
        mCurTransaction.commit();
        mCurTransaction = null;
    }


}