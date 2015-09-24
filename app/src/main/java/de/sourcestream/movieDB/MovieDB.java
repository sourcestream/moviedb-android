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

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class MovieDB extends Application {
    public static GoogleAnalytics analytics;
    public static Tracker tracker;
    public static final String url = "https://api.themoviedb.org/3/";
    public static final String key = "yourTMDBkey";
    public static final String imageUrl = "https://image.tmdb.org/t/p/";
    /**
     * Example URL:
     * http://i1.ytimg.com/vi/TDFAYRtrYuk/hqdefault.jpg
     * For more info:
     * http://stackoverflow.com/questions/2068344/how-do-i-get-a-youtube-video-thumbnail-from-the-youtube-api
     */
    public static final String trailerImageUrl = "http://i1.ytimg.com/vi/";
    public static final String youtube = "https://www.youtube.com/watch?v=";
    public static final String appId = "95a38b92c5cb4bbfd779c0e2fcaef5a6";
    public static final String analyticsKey = "yourGoogleAnalyticsKey";

    @Override
    public void onCreate() {
        super.onCreate();
        analytics = GoogleAnalytics.getInstance(this);
        tracker = analytics.newTracker(analyticsKey);
    }

    public Tracker getTracker() {
        return tracker;
    }
}