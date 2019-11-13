package com.huisou.userprotocol;



import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.huisou.library.user_protocol.ProtocolHandler;
import com.huisou.library.user_protocol.ProtocolTipView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ProtocolTipView tipView = findViewById(R.id.tip);
        tipView.init(Color.RED, "https://hsim.huisou.cn/app/#/kefu/agreement", "https://www.huisou.cn/agreement.html");
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