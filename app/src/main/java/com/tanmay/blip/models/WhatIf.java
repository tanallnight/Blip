/*
 *   Copyright 2015, Tanmay Parikh
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.tanmay.blip.models;

import android.os.Parcel;
import android.os.Parcelable;

public class WhatIf implements Parcelable {

    private String title;
    private String date;
    private String thumbnail;
    private String html;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public WhatIf(){}

    protected WhatIf(Parcel in) {
        title = in.readString();
        date = in.readString();
        thumbnail = in.readString();
        html = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(date);
        dest.writeString(thumbnail);
        dest.writeString(html);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<WhatIf> CREATOR = new Parcelable.Creator<WhatIf>() {
        @Override
        public WhatIf createFromParcel(Parcel in) {
            return new WhatIf(in);
        }

        @Override
        public WhatIf[] newArray(int size) {
            return new WhatIf[size];
        }
    };
}