package com.huisou.library.banner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.huisou.library.R;
import com.huisou.library.banner.video.HDVideo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.jzvd.Jzvd;

/**
 * Describe：带视频播放的无限轮播图
 *
 * @author 范要朋[1094325366@qq.com] at 2020/1/2 8:52
 */
public class BannerView extends FrameLayout {
    private ViewPager mPager;
    private int mCurPlayPosition = -1;
    private BannerAdapter mPageAdapter;
    private LinearLayout mIndicatorContainer;
    private AutoPlayHandler autoPlayHandler;
    private Drawable mIndicatorSelectedShape;
    private Drawable mIndicatorUnselectedShape;
    private ImageLoader mImageLoader;
    private boolean shouldScroll;
    private List<View> mBannerView;
    private List<BannerData> mBannerData;

    public BannerView(Context context) {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.banner_layout, this);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BannerView);
        mIndicatorSelectedShape = a.getDrawable(R.styleable.BannerView_indicator_selected);
        mIndicatorUnselectedShape = a.getDrawable(R.styleable.BannerView_indicator_unselected);
        a.recycle();
        setupView();
    }

    private static class AutoPlayHandler extends Handler {
        private BannerView container;

        AutoPlayHandler(WeakReference<BannerView> container) {
            this.container = container.get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 1) {
                int count = container.mBannerData.size();
                int pos = container.mPager.getCurrentItem();
                pos = pos % (count + 1) + 1;
                container.mPager.setCurrentItem(pos);
                sendEmptyMessageDelayed(1, 2500);
            }
        }
    }

    public void setImageLoader(ImageLoader loader) {
        mImageLoader = loader;
    }

    private void setupView() {
        mPager = findViewById(R.id.vp);
        mIndicatorContainer = findViewById(R.id.ll_indicator_container);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (positionOffset == 0) {
                    int count = mPageAdapter.getCount();
                    if (count > 1) {
                        if (position == 0) {
                            mPager.setCurrentItem(count - 2, false);
                        }
                        if (position == count - 1) {
                            mPager.setCurrentItem(1, false);
                        }
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                int count = mBannerData.size();
                if (position == 0 || position == count - 1) {
                    return;
                }
                List<View> views = mBannerView;
                if (mCurPlayPosition != -1) {
                    HDVideo before = (HDVideo) views.get(mCurPlayPosition);
                    before.pause();
                }
                View cur = views.get(position);
                if (cur instanceof HDVideo) {
                    HDVideo video = (HDVideo) cur;
                    if (video.getTag() != null) {
                        video.start();
                    }
                }
                mIndicatorContainer.setVisibility(cur instanceof HDVideo ? INVISIBLE : VISIBLE);
                updateIndicator(position - 1);
                if (mBannerChangeListener != null) {
                    mBannerChangeListener.onBannerSelected(position - 1);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private OnBannerChangeListener mBannerChangeListener;

    public void setOnBannerChangeListener(OnBannerChangeListener onBannerChangeListener) {
        this.mBannerChangeListener = onBannerChangeListener;
    }

    public interface OnBannerChangeListener {
        void onBannerSelected(int position);
    }


    public void scrollToPosition(int position) {
        if (position > mBannerData.size()) {
            throw new IndexOutOfBoundsException(" your position should not lager than count");
        }
        mPager.setCurrentItem(position + 1);
    }

    private void initView(List<BannerData> data) {
        if (data == null) {
            throw new NullPointerException("the data can not be  null");
        }
        shouldScroll = data.size() > 1;
        mBannerView = new ArrayList<>();
        mBannerData = new ArrayList<>();
        int size = data.size();
        if (shouldScroll) {
            size += 2;
            for (int i = 0; i < size; i++) {
                BannerData curData;
                if (i == 0) {
                    curData = data.get(data.size() - 1);
                } else if (i == size - 1) {
                    curData = data.get(0);
                } else {
                    curData = data.get(i - 1);
                }
                mBannerData.add(curData);
            }
        } else {
            mBannerData = data;
        }
        for (int i = 0; i < mBannerData.size(); i++) {
            View result;
            if (mBannerData.get(i).video) {
                result = new HDVideo(getContext());
            } else {
                ImageView imageView = new ImageView(getContext());
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                result = imageView;
            }
            mBannerView.add(result);
        }
    }

    public void setData(final List<BannerData> data) {
        initView(data);
        Jzvd.resetAllVideos();
        mPageAdapter = new BannerAdapter();
        mPager.setAdapter(mPageAdapter);
        addIndicator(data.size());
        if (data.size() > 1) {
            mPager.setCurrentItem(1);
        } else {
            updateIndicator(0);
        }
        registerVideoStatus();
        if (autoPlayHandler != null) {
            autoPlayHandler.removeMessages(1);
        } else {
            autoPlayHandler = new AutoPlayHandler(new WeakReference<>(this));
        }
        if (data.size() > 1 && !hasVideo()) {
            autoPlayHandler.sendEmptyMessageDelayed(1, 2500);
        }
        Context context = getContext();
        if (context instanceof LifecycleOwner) {
            final LifecycleOwner owner = (LifecycleOwner) context;
            owner.getLifecycle().addObserver(new LifecycleObserver() {

                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                public void onDestroy() {
                    if (autoPlayHandler != null) {
                        autoPlayHandler.removeMessages(1);
                    }
                    Jzvd.resetAllVideos();
                    owner.getLifecycle().removeObserver(this);

                }
            });
        }
    }

    private boolean hasVideo() {
        List<View> views = mBannerView;
        for (View view : views) {
            if (view instanceof HDVideo) {
                return true;
            }
        }
        return false;
    }

    public void seekTo(int page, final long position) {
        if (mPageAdapter == null) {
            return;
        }
        int count = mPageAdapter.getCount();
        if (page < 0 || page > count - 2) {
            return;
        }
        List<View> views = mBannerView;
        if (views.size() > 1) {
            views = views.subList(1, views.size() - 1);
        }
        final View cur = views.get(page);
        if (cur instanceof HDVideo) {
            final HDVideo video = (HDVideo) cur;
            post(new Runnable() {
                @Override
                public void run() {
                    video.start();
                    video.seekTo(position);
                }
            });

        }
    }

    private HDVideo.OnFullScreenClickListener fullScreenClickListener;

    /**
     * 全屏点击的拦截 这个应该在{@link BannerView#setData(List)} 之前调用
     *
     * @param listener l
     */
    public void setFullScreenClickListener(HDVideo.OnFullScreenClickListener listener) {
        fullScreenClickListener = listener;
    }

    private void registerVideoStatus() {
        List<View> views = mBannerView;
        for (int i = 0; i < views.size(); i++) {
            View view = views.get(i);
            if (view instanceof HDVideo) {
                final HDVideo video = (HDVideo) view;
                final int finalI = i;
                video.setOnPlayStatusChangeListener(new HDVideo.OnPlayStatusChangeListener() {
                    @Override
                    public void onStart() {
                        video.setTag("played");
                        mCurPlayPosition = finalI;
                    }

                    @Override
                    public void onPause() {
                        mCurPlayPosition = -1;
                    }

                    @Override
                    public void onComplete() {
                        mCurPlayPosition = -1;
                    }
                });
            }
        }
    }

    private int dp2px(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getContext().getResources().getDisplayMetrics());
    }

    private void addIndicator(int size) {
        mIndicatorContainer.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp2px(8), dp2px(8));
        params.leftMargin = dp2px(5);
        for (int i = 0; i < size; i++) {
            ImageView imageView = new ImageView(getContext());
            imageView.setImageDrawable(mIndicatorSelectedShape);
            mIndicatorContainer.addView(imageView, params);
        }
    }

    private void updateIndicator(int position) {
        for (int i = 0; i < mIndicatorContainer.getChildCount(); i++) {
            ImageView child = (ImageView) mIndicatorContainer.getChildAt(i);
            if (position == i) {
                child.setImageDrawable(mIndicatorSelectedShape);
            } else {
                child.setImageDrawable(mIndicatorUnselectedShape);
            }
        }
    }

    public static class BannerData {
        private String url;
        private boolean video;
        private String previewUrl;

        public BannerData(String url) {
            this(url, null);
        }

        public BannerData(String url, String previewUrl) {
            this.url = url;
            this.previewUrl = previewUrl;
            video = previewUrl != null;
        }

        public String getUrl() {
            return url;
        }

        public boolean isVideo() {
            return video;
        }

        public String getPreviewUrl() {
            return previewUrl;
        }
    }

    private class BannerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mBannerView.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            View result = mBannerView.get(position);
            if (result instanceof ImageView) {
                if (mImageLoader != null) {
                    mImageLoader.loadImage((ImageView) result, mBannerData.get(position).url);
                }

                ImageView imageView = (ImageView) result;
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mItemClickListener.onItemClick(position);
                    }
                });
            }
            if (result instanceof HDVideo) {
                HDVideo video = (HDVideo) result;
                video.setUp(mBannerData.get(position).url, "", Jzvd.SCREEN_NORMAL);
                ImageView thumb = video.thumbImageView;
                thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (mImageLoader != null) {
                    mImageLoader.loadImage(thumb, mBannerData.get(position).previewUrl);
                }

                video.setOnFullScreenClickListener(new HDVideo.OnFullScreenClickListener() {
                    @Override
                    public void onFullScreenClick(long position) {
                        if (fullScreenClickListener != null) {
                            fullScreenClickListener.onFullScreenClick(position);
                        }
                    }
                });
            }
            container.addView(result);
            return result;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    private OnItemClickListener mItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
