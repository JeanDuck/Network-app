package com.friendster.activity;

import android.os.Bundle;

import com.friendster.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;

public class FullImageActivity extends AppCompatActivity {
PhotoView mPhotoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        final String imageUrl=getIntent().getStringExtra("imageUrl");
        mPhotoView=findViewById(R.id.full_image_id);

        if(!imageUrl.isEmpty())
        {
            Picasso.with(FullImageActivity.this).load(imageUrl).networkPolicy(NetworkPolicy.OFFLINE).into(mPhotoView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(FullImageActivity.this).load(imageUrl).into(mPhotoView);

                }
            });
        }
    }
}
