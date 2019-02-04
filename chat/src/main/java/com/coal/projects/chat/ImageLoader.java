package com.coal.projects.chat;


import android.net.Uri;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public interface ImageLoader {
    void loadImageInto(String url, Target target);

    void loadImageInto(String url, ImageView imageView);

    void invalidateCache(String url);

    void loadImageInto(Uri uri, ImageView imageView);

    void loadImageInto(Uri uri, Target target);

    Picasso getPicasso();
}
