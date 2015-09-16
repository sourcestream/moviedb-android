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
import android.content.res.Resources;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import de.sourcestream.movieDB.MovieDB;
import de.sourcestream.movieDB.R;
import de.sourcestream.movieDB.controller.GalleryPreviewDetail;


/**
 * The main adapter that backs the ViewPager. A subclass of FragmentStatePagerAdapter as there
 * could be a large number of items in the ViewPager and we don't want to retain them all in
 * memory at once but create/destroy them on the fly.
 */

public class GalleryPreviewSlideAdapter extends FragmentStatePagerAdapter {
    private final int mSize;
    private Resources res;
    private ArrayList<String> galleryList;

    public GalleryPreviewSlideAdapter(FragmentManager fm, Resources res, ArrayList<String> galleryList) {
        super(fm);
        this.res = res;
        this.galleryList = galleryList;
        mSize = galleryList.size();


    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return mSize;
    }

    /**
     * Return the Fragment associated with a specified position.
     * @param position Position within this adapter
     * @return Unique identifier for the item at position
     */
    @Override
    public Fragment getItem(int position) {
        return GalleryPreviewDetail.newInstance(MovieDB.imageUrl + res.getString(R.string.galleryPreviewImgSize) + galleryList.get(position));
    }
}
