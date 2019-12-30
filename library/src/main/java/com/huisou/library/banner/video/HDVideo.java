package com.huisou.library.banner.video;

import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.huisou.library.R;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class HDVideo extends JzvdStd {
    private ImageView voice;
    private boolean isInZeroVoice = false;

    public HDVideo(Context context) {
        super(context);
        setupView();
    }

    public HDVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupView();
    }

    private void setupView() {
        voice = new ImageView(getContext());
        voice.setImageResource(R.mipmap.ic_video_voice);
        RelativeLayout root = (RelativeLayout) getChildAt(0);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ABOVE, R.id.layout_bottom);
        params.bottomMargin = dp2px(15);
        params.rightMargin = dp2px(14);
        root.addView(voice, params);
        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaInterface != null) {
                    if (isInZeroVoice) {
                        float system = getSystemVolume();
                        mediaInterface.setVolume(system, system);
                        isInZeroVoice = false;
                        voice.setImageResource(R.mipmap.ic_video_voice);
                    } else {
                        mediaInterface.setVolume(0, 0);
                        isInZeroVoice = true;
                        voice.setImageResource(R.mipmap.ic_no_voice);
                    }
                }
            }
        });
        Context context = getContext();
        if (context instanceof LifecycleOwner) {
            final LifecycleOwner owner = (LifecycleOwner) context;
            owner.getLifecycle().addObserver(new LifecycleObserver() {
                @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                public void onPause() {
                    Jzvd.resetAllVideos();
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                public void onDestroy() {
                    owner.getLifecycle().removeObserver(this);
                }
            });
        }
    }

    @Override
    public void setAllControlsVisiblity(int topCon, int bottomCon, int startBtn, int loadingPro, int thumbImg, int bottomPro, int retryLayout) {
        super.setAllControlsVisiblity(topCon, bottomCon, startBtn, loadingPro, thumbImg, bottomPro, retryLayout);
        voice.setVisibility(bottomCon);
    }

    @Override
    public void dissmissControlView() {
        super.dissmissControlView();
        post(new Runnable() {
            @Override
            public void run() {
                voice.setVisibility(INVISIBLE);
            }
        });
    }

    private float getSystemVolume() {
        AudioManager manager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        int max = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int cur = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return 1 / (float) max * cur;
    }

    private int dp2px(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getContext().getResources().getDisplayMetrics());
    }

    public void pause() {
        startButton.performClick();
    }

    public void start() {
        startButton.performClick();
    }

    @Override
    public void onStatePlaying() {
        super.onStatePlaying();
        if (mListener != null) {
            mListener.onStart();
        }
    }

    @Override
    public void onStatePause() {
        super.onStatePause();
        if (mListener != null) {
            mListener.onPause();
        }
    }

    @Override
    public void updateStartImage() {
        super.updateStartImage();
        if (currentState == CURRENT_STATE_PLAYING) {
            startButton.setImageResource(R.mipmap.ic_video_playing);
        } else if (currentState == CURRENT_STATE_PAUSE) {
            startButton.setImageResource(R.mipmap.ic_video_pause);
        }
    }

    @Override
    public void onStateAutoComplete() {
        super.onStateAutoComplete();
        if (mListener != null) {
            mListener.onComplete();
        }
    }

    @Override
    public void setScreenNormal() {
        super.setScreenNormal();
        fullscreenButton.setImageResource(R.mipmap.video_full_screen);
    }

    @Override
    public void onClick(View v) {
        //业务需求 拦截点击全屏
        if (v == fullscreenButton && mFullScreenClickListener != null) {
            mFullScreenClickListener.onFullScreenClick(getCurrentPositionWhenPlaying());
            return;
        }
        super.onClick(v);
    }

    private OnPlayStatusChangeListener mListener;

    public void seekTo(long position) {
        seekToInAdvance = position;
    }

    public interface OnPlayStatusChangeListener {
        void onStart();

        void onPause();

        void onComplete();
    }

    public void setOnPlayStatusChangeListener(OnPlayStatusChangeListener listener) {
        this.mListener = listener;
    }

    private OnFullScreenClickListener mFullScreenClickListener;

    public void setOnFullScreenClickListener(OnFullScreenClickListener listener) {
        this.mFullScreenClickListener = listener;
    }

    public interface OnFullScreenClickListener {
        void onFullScreenClick(long position);
    }
}
