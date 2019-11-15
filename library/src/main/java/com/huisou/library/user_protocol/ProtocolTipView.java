package com.huisou.library.user_protocol;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.huisou.library.R;

public class ProtocolTipView extends LinearLayout {
    public ProtocolTipView(Context context) {
        super(context);
    }

    public ProtocolTipView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.protocol_layout, this);
    }

    public void init(int color, final String userAgreementLink, final String privacyLink) {
        TextView userAgreement = (TextView) findViewById(R.id.user_agreement);
        userAgreement.setTextColor(color);
        userAgreement.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SimpleWebActivity.class);
                intent.putExtra("title", "用户协议");
                intent.putExtra("url", userAgreementLink);
                getContext().startActivity(intent);
            }
        });


        TextView privacyPolicy = (TextView) findViewById(R.id.privacy_policy);
        privacyPolicy.setTextColor(color);
        privacyPolicy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SimpleWebActivity.class);
                intent.putExtra("title", "隐私协议");
                intent.putExtra("url", privacyLink);
                getContext().startActivity(intent);
            }
        });
    }
}
