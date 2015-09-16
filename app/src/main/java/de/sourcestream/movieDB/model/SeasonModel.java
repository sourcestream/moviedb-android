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
 * Season model class.
 * Used in Movie details -> Cast list.
 */
public class SeasonModel implements Parcelable {
    private String title;
    private String posterPath;
    private int id;
    private String episodeCount;
    private String airDate;

    public SeasonModel() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(String episodeCount) {
        this.episodeCount = episodeCount;
    }

    public String getAirDate() {
        return airDate;
    }

    public void setAirDate(String airDate) {
        this.airDate = airDate;
    }

    protected SeasonModel(Parcel in) {
        title = in.readString();
        posterPath = in.readString();
        id = in.readInt();
        episodeCount = in.readString();
        airDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(posterPath);
        dest.writeInt(id);
        dest.writeString(episodeCount);
        dest.writeString(airDate);
    }

    public static final Parcelable.Creator<SeasonModel> CREATOR = new Parcelable.Creator<SeasonModel>() {
        @Override
        public SeasonModel createFromParcel(Parcel in) {
            return new SeasonModel(in);
        }

        @Override
        public SeasonModel[] newArray(int size) {
            return new SeasonModel[size];
        }
    };

}
