What is it?
-----------
Movie DB is an Android app with open source code. Movie DB finds information about your favourite movies, TV shows and actors.</br>
Movie DB uses [TMDb API](https://www.themoviedb.org/documentation/api) which has large collection of information about movies, TV shows and actors.
We offer friendly and intuitive user interface. You can view information such as: trailers, images, credits, known for, overview, biography, etc. You can search your favourite actor, movie or TV show. Browse movies by specific genre and more!

<img src="https://raw.githubusercontent.com/sourcestream/moviedb-android/master/images/1.png" width="216" height="384">
<img src="https://raw.githubusercontent.com/sourcestream/moviedb-android/master/images/2.png" width="216" height="384">
<img src="https://raw.githubusercontent.com/sourcestream/moviedb-android/master/images/5.png" width="216" height="384">
<br/>
### Download from Google Play

[![Get it on Google Play](https://developer.android.com/images/brand/en_generic_rgb_wo_45.png)](https://play.google.com/store/apps/details?id=de.sourcestream.movieDB&hl=bg)

Getting started
-------------
1. First you need to have installed Android Studio on your computer. You can download it from [here](https://developer.android.com/sdk/index.html).

2. Download the project
> git clone https://github.com/sourcestream/moviedb-android.git

3. After you download the project open Android Studio. From there choose *Open an existing Android Studio project* or *File -> New -> Import Project..*
4.  Open the Movie DB folder that was just cloned from git.
5.  Wait until Android Studio finishes building and indexing.
6. Proceed to configuration

Configuration
-------------
The configuration file is `MovieDB.java`, located in the package `de.sourcestream.movieDB`:

>     ......
    public static final String url = "https://api.themoviedb.org/3/"; // main url for the app
    public static final String key = "yourTMDBkey"; 
    public static final String imageUrl = "https://image.tmdb.org/t/p/"; // used to load movie, TV and actor images
    public static final String trailerImageUrl = "http://i1.ytimg.com/vi/"; // used to load trailer images
    public static final String youtube = "https://www.youtube.com/watch?v="; // used to load trailer videos
    public static final String analyticsKey = "yourGoogleAnalyticsKey";
    .....


In order to use MovieDB you need to get your own key from TMDb. You can do that by clicking [here](https://www.themoviedb.org/account/signup).
</br>After you get key replace the string `key` with your own key.
</br>Now you can run the app by clicking *Run -> Run 'app'*.
<p>If you plan to use Google Analytics replace the string `analyticsKey` with your own key.
<p>You can find list of the available methods for movies, TV and actors from TMDb API Documentation [here](http://docs.themoviedb.apiary.io/#).


Environment
--------------
[![Build Status](https://api.travis-ci.org/sourcestream/moviedb-android.svg?branch=master)](http://travis-ci.org/sourcestream/moviedb-android)
</br>This project has been build with Android Studio 1.3.2
</br>SDK Build Tools version 23.0.0
</br>Minimum SDK version for the app is 	17 (Android 4.2, 4.2.2).
</br>Target SDK version for the app is 23 (Android 6.0).
</br>Movie DB is supported currently for phones only, but you can run it on tablet.
</br>Supported devices: 4709. You can see the full list [here](https://github.com/sourcestream/moviedb-android/blob/master/documents/supported%20devices.pdf).

Libraries
--------------

 - **[Universal Image Loader](https://github.com/nostra13/Android-Universal-Image-Loader)** for image loading, displaying and caching.
 - **[Text-Justify](https://github.com/bluejamesbond/TextJustify-Android)**
 - **[ObservableScrollView](https://github.com/ksoichiro/Android-ObservableScrollView)** for implementing hiding toolbar feature like in Google Play Store app.
 - **[Parallax Scrolls](https://github.com/nirhart/ParallaxScroll)** for making parallaxed views.
 - **[Robolectric](http://robolectric.org/)** for running Android tests directly from inside your IDE.


Contact
--------------

<img src="https://raw.githubusercontent.com/sourcestream/moviedb-android/master/images/sourcestream.png" width="300" height="227">

</br>This project is developed by [sourcestream GmbH](http://sourcestream.de/).
</br>If you have any questions please do not hesitate to contact us: <a href="mailto:info@sourcestream.de?subject=SweetWords">
info@sourcestream.de</a> 
License
--------------
    Copyright 2015 sourcestream GmbH

	Licensed under the Apache License, Version 2.0 (the "License"); 
	you may not use this file except in compliance with the License. 
	You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software 
	distributed under the License is distributed on an "AS IS" BASIS, 
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
	See the License for the specific language governing permissions and 
	limitations under the License.
