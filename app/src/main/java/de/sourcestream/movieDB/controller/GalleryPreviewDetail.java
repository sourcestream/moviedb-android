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
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import de.sourcestream.movieDB.MainActivity;
import de.sourcestream.movieDB.R;

/**
 * This fragment is the gallery preview for every image.
 */
public class GalleryPreviewDetail extends Fragment {
    private MainActivity activity;
    private String currImg;
    private ImageView mImageView;
    private ProgressBar progressBar;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private ImageLoadingListener imageLoadingListener;
    private int mUIFlag = View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;


    /**
     * Factory method to generate a new instance of the fragment given an image number.
     *
     * @param imageUrl The image url to load
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static GalleryPreviewDetail newInstance(String imageUrl) {
        final GalleryPreviewDetail f = new GalleryPreviewDetail();

        final Bundle args = new Bundle();
        args.putString("currImg", imageUrl);
        f.setArguments(args);

        return f;
    }

    public GalleryPreviewDetail() {

    }

    /**
     * Populate image using a url from extras, use the convenience factory method
     * {@link #newInstance(String)} to create this fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currImg = getArguments() != null ? getArguments().getString("currImg") : null;
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                // Bitmaps in RGB_565 consume 2 times less memory than in ARGB_8888.
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(false)
                .showImageOnLoading(null)
                .showImageForEmptyUri(null)
                .showImageOnFail(null)
                .cacheOnDisk(true)
                .build();
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           sets the layout for the current view.
     * @param container          the container which holds the current view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     *                           Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        imageLoadingListener = new ImageLoadingListener();
        OnImageClick onImageClick = new OnImageClick();

        // Inflate and locate the main ImageView
        final View v = inflater.inflate(R.layout.gallerypreviewdetail, container, false);
        activity = ((MainActivity) getActivity());
        mImageView = (ImageView) v.findViewById(R.id.galleryPreviewImgHolder);
        mImageView.setOnClickListener(onImageClick);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        activity.getMDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (activity.getSupportActionBar() != null && activity.getSupportActionBar().isShowing())
            activity.getSupportActionBar().hide();

        if (Build.VERSION.SDK_INT >= 19) {
            mUIFlag ^= View.SYSTEM_UI_FLAG_IMMERSIVE;
        }

        if (this.isVisible()) {
            // Check orientation and lock to portrait if we are on phone
            if (getResources().getBoolean(R.bool.portrait_only)) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        }
        imageLoader.displayImage(currImg, mImageView, options, imageLoadingListener);
    }

    /**
     * Class which listens when an image has been loaded, so we can hide our progress bar.
     */
    private class ImageLoadingListener extends SimpleImageLoadingListener {

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * This class listens for click events on the image.
     * If there is a click event we hide or show the navigation and system bars.
     */
    public class OnImageClick implements View.OnClickListener {
        public OnImageClick() {
            // keep references for your onClick logic
        }

        @Override
        public void onClick(View v) {
            int uiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility();
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (uiOptions == mUIFlag) {
                    activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    if (Build.VERSION.SDK_INT >= 19) {
                        Window w = activity.getWindow();
                        w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                        w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    }
                } else
                    activity.getWindow().getDecorView().setSystemUiVisibility(mUIFlag);

            }
        }
    }

    /**
     * Set empty adapter to free memory when this fragment is inactive
     */
    public void onDestroyView() {
        super.onDestroyView();
        mImageView.setImageDrawable(null);
    }


}


