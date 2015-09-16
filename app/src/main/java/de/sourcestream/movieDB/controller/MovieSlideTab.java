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
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;

import de.sourcestream.movieDB.MainActivity;
import de.sourcestream.movieDB.R;
import de.sourcestream.movieDB.adapter.MovieSlideAdapter;
import de.sourcestream.movieDB.helper.ObservableScrollViewCallbacks;
import de.sourcestream.movieDB.helper.ScrollState;
import de.sourcestream.movieDB.helper.Scrollable;
import de.sourcestream.movieDB.view.SlidingTabLayout;

/**
 * A basic sample which shows how to use {@link de.sourcestream.movieDB.view.SlidingTabLayout}
 * to display a custom {@link android.support.v4.view.ViewPager} title strip which gives continuous feedback to the user
 * when scrolling.
 * This class configures the ViewPager in the Movies and has some custom methods for the sliding toolbar
 */
public class MovieSlideTab extends Fragment implements ObservableScrollViewCallbacks {

    /**
     * A custom {@link android.support.v4.view.ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    private SlidingTabLayout mSlidingTabLayout;

    /**
     * A {@link android.support.v4.view.ViewPager} which will be used in conjunction with the {@link SlidingTabLayout} above.
     */
    private ViewPager mViewPager;
    private MovieSlideAdapter adapter;
    private onPageChangeSelected onPageChangeSelected;
    private MainActivity activity;
    private Bundle savedInstanceState;
    private int currPos;
    private int oldScrollY;
    private float dy;
    private float upDy;
    private float downDy;
    private float downDyTrans;
    private boolean upDyKey;
    private boolean downDyKey;
    private float scale;
    private boolean phone;
    private int hideThreshold;
    private int minThreshold;

    /**
     * Inflates the {@link android.view.View} which will be displayed by this {@link Fragment}, from the app's
     * resources.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.moviepager, container, false);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        adapter = new MovieSlideAdapter(getFragmentManager(), getResources());
        mViewPager = (ViewPager) view.findViewById(R.id.moviePager);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setAdapter(adapter);

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        onPageChangeSelected = new onPageChangeSelected();
        activity = ((MainActivity) getActivity());
        mSlidingTabLayout.setOnPageChangeListener(onPageChangeSelected);
        mSlidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(activity, R.color.tabSelected));
        phone = getResources().getBoolean(R.bool.portrait_only);
        scale = getResources().getDisplayMetrics().density;
        if (phone) {
            hideThreshold = (int) (-105 * scale);
            minThreshold = (int) (-49 * scale);
        } else {
            hideThreshold = (int) (-100 * scale);
            minThreshold = (int) (-42 * scale);
        }
        return view;
    }


    /**
     * This is called after the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
     * Here we can pick out the {@link View}s we need to configure from the content view.
     * <p/>
     * We set the {@link ViewPager}'s adapter to be an instance of {@link MovieSlideAdapter}. The
     * {@link SlidingTabLayout} is then given the {@link ViewPager} so that it can populate itself.
     *
     * @param view View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.savedInstanceState = savedInstanceState;
        if (activity.getReAttachMovieFragments()) {
            adapter.reAttachFragments(mViewPager);
            activity.setReAttachMovieFragments(false);
        }

        currPos = activity.getCurrentMovViewPagerPos();
        mViewPager.setCurrentItem(currPos);
        onPageChangeSelected.onPageSelected(currPos);
        activity.setMovieSlideTab(this);

        showInstantToolbar();
    }

    /**
     * Class which listens when the user has changed the tap in Cast details
     */
    public class onPageChangeSelected implements ViewPager.OnPageChangeListener {
        MovieList fragment;

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }


        @Override
        public void onPageSelected(int position) {
            // Finds the current fragment and updates the list if needed
            // On orientation change loses reference to fragments, that's why every time we find our fragment.
            if (MainActivity.getMaxMem() / 1048576 <= 20) {
                if (currPos != position) {
                    // Find the old fragment and clear the list to save up memory
                    MovieList oldFragment = (MovieList) getFragmentManager().findFragmentByTag(getFragmentTag(currPos));
                    oldFragment.cleanUp();
                    oldFragment.setFragmentActive(false);
                }
            }
            currPos = position;
            activity.setCurrentMovViewPagerPos(position);
            boolean load = true;
            if (savedInstanceState != null)
                load = savedInstanceState.getBoolean("load");

            fragment = (MovieList) getFragmentManager().findFragmentByTag(getFragmentTag(position));
            if (fragment != null) {
                fragment.setFragmentActive(true);
                if (fragment.getMoviesList() != null && fragment.getMoviesList().size() > 0) {
                    final AbsListView listView = fragment.getListView();
                    final View toolbarView = activity.findViewById(R.id.toolbar);
                    if (listView != null) {
                        listView.post(new Runnable() {
                            @Override
                            public void run() {
                                // check if toolbar is hidden and minThreshold to scroll
                                if (toolbarView.getTranslationY() == -toolbarView.getHeight() && ((Scrollable) listView).getCurrentScrollY() < minThreshold) {
                                    if (phone)
                                        listView.smoothScrollBy((int) (56 * scale), 0);
                                    else
                                        listView.smoothScrollBy((int) (59 * scale), 0);
                                }
                            }
                        });
                    }
                }

                if (load) {
                    if (fragment.getBackState() == 0)
                        fragment.updateList();
                    else
                        fragment.setAdapter();

                } else savedInstanceState.putBoolean("load", true);
            }


        }
    }

    /**
     * This is used to get the fragment id. This way we know which fragments are active.
     *
     * @param pos the position we are looking for.
     * @return id of the fragment.
     */
    public String getFragmentTag(int pos) {
        return "android:switcher:" + R.id.moviePager + ":" + pos;
    }

    /**
     * Returns the current position of the Viewpager.
     */
    public int getCurrPos() {
        return currPos;
    }

    /**
     * Called to ask the fragment to save its current dynamic state,
     * so it can later be reconstructed in a new instance of its process is restarted.
     *
     * @param outState Bundle in which to place your saved state.
     */
    // Prevent onPageSelected event to be fired on orientation change
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        boolean load;
        if (currPos != 0)
            load = false;
        else
            load = true;

        outState.putBoolean("load", load);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        if (dragging) {
            View toolbarView = getActivity().findViewById(R.id.toolbar);

            if (scrollY > oldScrollY) {

                if (upDyKey) {
                    upDy = scrollY;
                    upDyKey = false;
                } else {
                    dy = upDy - scrollY;

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

            if (scrollY < oldScrollY) {

                if (downDyKey) {
                    downDy = scrollY;
                    downDyTrans = toolbarView.getTranslationY();
                    downDyKey = false;
                } else {

                    dy = (downDyTrans + (downDy - scrollY));
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

        oldScrollY = scrollY;
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
                return (Scrollable) view.findViewById(R.id.movieslist);
            case 1:
                return (Scrollable) view.findViewById(R.id.nowplaying);
            case 2:
                return (Scrollable) view.findViewById(R.id.popular);
            case 3:
                return (Scrollable) view.findViewById(R.id.toprated);
            default:
                return (Scrollable) view.findViewById(R.id.movieslist);
        }
    }

    /**
     * Fixes the position of the toolbar
     *
     * @param scrollState
     */
    private void adjustToolbar(ScrollState scrollState) {
        View toolbarView = activity.findViewById(R.id.toolbar);
        int toolbarHeight = toolbarView.getHeight();
        final Scrollable scrollable = getCurrentScrollable();
        if (scrollable == null) {
            return;
        }
        int scrollY = scrollable.getCurrentScrollY();
        if (scrollState == ScrollState.DOWN) {
            showToolbar();
        } else if (scrollState == ScrollState.UP) {
            if (toolbarHeight <= scrollY - hideThreshold) {
                hideToolbar();
            } else {
                showToolbar();
            }
        }
    }

    /**
     * Returns the current active fragment for the given position
     */
    private Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentByTag(getFragmentTag(mViewPager.getCurrentItem()));
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
    }


}