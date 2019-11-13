package com.huisou.library.user_protocol;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.Window;
import android.widget.TextView;


import com.huisou.library.R;

public class ProtocolHandler {
    private Dialog dialog;

    private void show() {
        dialog.show();
    }

    private ProtocolHandler(@NonNull final Context context, final Params params) {
        dialog = new Dialog(context);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setContentView(R.layout.dialog_privacy_desc);
        dialog.setCancelable(false);
        TextView tvTitle = dialog.findViewById(R.id.title);
        TextView tvSubTitle = dialog.findViewById(R.id.sub_title);

        final int linkColor = params.linkColor;
        String appName = params.appName;
        String title = String.format(context.getResources().getString(R.string.privacy_title), appName, appName);
        SpannableStringBuilder titleBuild = new SpannableStringBuilder(title);
        String protocol = "《用户使用协议》";
        int protocolStart = title.indexOf(protocol);
        String privacy = "《用户隐私政策》";
        int privacyStart = title.indexOf(privacy);
        titleBuild.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                //使用协议
                Intent intent = new Intent(context, SimpleWebActivity.class);
                intent.putExtra("title", "用户使用协议");
                intent.putExtra("url", params.protocolLink);
                context.startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(linkColor);
            }
        }, protocolStart, protocolStart + protocol.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleBuild.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                //隐私政策
                Intent intent = new Intent(context, SimpleWebActivity.class);
                intent.putExtra("title", "隐私协议");
                intent.putExtra("url", params.privacyLink);
                context.startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(linkColor);
            }
        }, privacyStart, privacyStart + privacy.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvTitle.setMovementMethod(LinkMovementMethod.getInstance());
        tvTitle.setText(titleBuild);
        tvSubTitle.setText(String.format(context.getResources().getString(R.string.privacy_sub), appName));
        TextView tvAgree = dialog.findViewById(R.id.tv_agree);
        tvAgree.setBackgroundColor(params.btnColor);
        tvAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("HAS_READ_PROTOCOL", true).apply();
                dialog.dismiss();

                Params.Callback callback = params.callback;
                if (callback != null) {
                    callback.onAgree();
                }
            }
        });
        dialog.findViewById(R.id.tv_disagree).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (context instanceof Activity) {
                    ((Activity) context).finish();
                }
            }
        });
    }

    private static boolean hasReadProtocol(Context context) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getBoolean("HAS_READ_PROTOCOL", false);
    }

    public static class Params {
        Context context;
        int linkColor;
        int btnColor;
        String appName;
        String protocolLink;
        String privacyLink;
        Callback callback;

        public interface Callback {

            void onAgree();
        }
    }

    public static class Builder {
        private Params p;

        public Builder setLinkColor(int linkColor) {
            p.linkColor = linkColor;
            return this;
        }

        public Builder setBtnColor(int btnColor) {
            p.btnColor = btnColor;
            return this;
        }

        public Builder setAppName(String appName) {
            p.appName = appName;
            return this;
        }

        public Builder setProtocolLink(String protocolLink) {
            p.protocolLink = protocolLink;
            return this;
        }

        public Builder setPrivacyLink(String privacyLink) {
            p.privacyLink = privacyLink;
            return this;
        }

        public Builder setListener(Params.Callback callback) {
            p.callback = callback;
            return this;
        }

        public Builder(Context context) {
            p = new Params();
            p.context = context;
        }

        public void send() {
            if (hasReadProtocol(p.context)) {
                if (p.callback != null) {
                    p.callback.onAgree();
                }
            } else {
                new ProtocolHandler(p.context, p).show();
            }
        }
    }
}
