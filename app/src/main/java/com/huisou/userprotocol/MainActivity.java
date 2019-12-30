package com.huisou.userprotocol;


import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.huisou.library.banner.BannerView;
import com.huisou.library.banner.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        List<BannerView.BannerData> bannerData = new ArrayList<>();
        bannerData.add(new BannerView.BannerData("https://hdres.huisou.cn/uploads/1/files/20191113/c1b86bc197c4c073622c16ecb457e8bb.mp4",
                "https://hdres.huisou.cn/uploads/1/images/20191113/3e7cc14fbb4ca1007623467e8c32d3a5.jpg"));

        bannerData.add(new BannerView.BannerData("https://hdres.huisou.cn/uploads/1/images/20191113/583ad4ccd1b21eb01dac50f639c82002.jpg"));
        BannerView bannerView = findViewById(R.id.banner);
        bannerView.setImageLoader(new ImageLoader() {
            @Override
            public void loadImage(ImageView target, String url) {
                Glide.with(MainActivity.this).load(url).into(target);
            }
        });

        bannerView.setData(bannerData);
    }

}
