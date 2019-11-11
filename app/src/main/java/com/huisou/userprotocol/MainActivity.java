package com.huisou.userprotocol;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.huisou.library.user_protocol.ProtocolHandler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void show(View view) {
        ProtocolHandler.Builder builder = new ProtocolHandler.Builder(this);
        builder.setAppName(getString(R.string.app_name)).setBtnColor(Color.RED)
                .setPrivacyLink("https://hsim.huisou.cn/app/#/kefu/agreement")
                .setProtocolLink("https://www.huisou.cn/agreement.html")
                .setLinkColor(Color.RED).setListener(new ProtocolHandler.Params.Callback() {
            @Override
            public void onAgree() {
                Toast.makeText(MainActivity.this, "测试通过", Toast.LENGTH_SHORT).show();
            }
        }).send();
    }
}
