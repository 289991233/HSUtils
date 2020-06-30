package com.huisou.userprotocol;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.huisou.library.banner.BannerView;
import com.huisou.library.banner.ImageLoader;
import com.huisou.library.user_protocol.ProtocolHandler;

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
        bannerData.add(new BannerView.BannerData("https://t8.baidu.com/it/u=3571592872,3353494284&fm=79&app=86&size=h300&n=0&g=4n&f=jpeg?sec=1578533411&t=8e75ad1447376dda8b9addcd3f555e6b"));
        BannerView bannerView = findViewById(R.id.banner);
        bannerView.setImageLoader(new ImageLoader() {
            @Override
            public void loadImage(ImageView target, String url) {
                Glide.with(MainActivity.this).load(url).into(target);
            }
        });

        bannerView.setData(bannerData);
//        SharedPreferences preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putBoolean("HAS_READ_PROTOCOL", false).apply();
        ProtocolHandler.Builder builder = new ProtocolHandler.Builder(this);
        builder.setAppName(getString(R.string.app_name)).setBtnColor(getResources().getColor(R.color.colorAccent))
                .setPrivacyLink("https://hdwebapp.huisou.cn/privacyPolicy?appsign=1&project_id=1")
                .setProtocolLink("https://hdwebapp.huisou.cn/userAgreement?appsign=1&project_id=1")
                .setLinkColor(getResources().getColor(R.color.colorAccent)).setListener(new ProtocolHandler.Params.Callback() {
            @Override
            public void onAgree() {
            }
        }).send();
    }

}
