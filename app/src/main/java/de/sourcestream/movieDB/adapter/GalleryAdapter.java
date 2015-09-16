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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import java.util.ArrayList;

import de.sourcestream.movieDB.MainActivity;
import de.sourcestream.movieDB.R;
import de.sourcestream.movieDB.model.GalleryModel;


/**
 * Gallery adapter. Used to load gallery images in the gallery list.
 */
public class GalleryAdapter extends ArrayAdapter<GalleryModel> {
    private ArrayList<GalleryModel> galleryList;
    private LayoutInflater vi;
    private int Resource;
    private ViewHolder holder;
    private Context mContext;
    private ImageLoader imageLoader;

    public GalleryAdapter(Context context, int resource, ArrayList<GalleryModel> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        galleryList = objects;
        mContext = context;
        imageLoader = ImageLoader.getInstance();
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     * @param position The position of the item within the adapter's data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view is non-null and of an appropriate type before using.
     * @param parent The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // convert view = design
        View v = convertView;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.filePath = (ImageView) v.findViewById(R.id.filePath);


            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        if (imageLoader.getDiskCache().get(galleryList.get(position).getFilePath()).exists())
            imageLoader.displayImage(galleryList.get(position).getFilePath(), holder.filePath, ((MainActivity) mContext).getOptionsWithoutFade());
        else
            imageLoader.displayImage(galleryList.get(position).getFilePath(), holder.filePath, ((MainActivity) mContext).getOptionsWithFade());


        return v;

    }

    /**
     * Defines gallery list row elements.
     */
    static class ViewHolder {
        public ImageView filePath;
    }


}