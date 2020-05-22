package com.hriportfolio.speaketh;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {


    private ImageView image_viewer;
    private String imgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        image_viewer = findViewById(R.id.image_viewer);

        imgUrl = getIntent().getStringExtra("url");
        Picasso.get().load(imgUrl).into(image_viewer);
    }
}
