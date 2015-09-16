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

package de.sourcestream.movieDB.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Used in MovieDetails similar
 */
public class SimilarModel implements Parcelable {
    private int id;
    private String posterPath, title, releaseDate, mediaType;

    public SimilarModel() {

    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        try {
            this.releaseDate = releaseDate.substring(0, 4);
        } catch (java.lang.StringIndexOutOfBoundsException e) {
            this.releaseDate = null;
        }
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    protected SimilarModel(Parcel in) {
        id = in.readInt();
        posterPath = in.readString();
        title = in.readString();
        releaseDate = in.readString();
        mediaType = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(posterPath);
        dest.writeString(title);
        dest.writeString(releaseDate);
        dest.writeString(mediaType);
    }

    public static final Parcelable.Creator<SimilarModel> CREATOR = new Parcelable.Creator<SimilarModel>() {
        @Override
        public SimilarModel createFromParcel(Parcel in) {
            return new SimilarModel(in);
        }

        @Override
        public SimilarModel[] newArray(int size) {
            return new SimilarModel[size];
        }
    };
}
