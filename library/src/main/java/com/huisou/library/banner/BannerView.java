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

import java.util.ArrayList;
import java.util.List;

import cn.jzvd.Jzvd;

/**
 * 描    述：带视频播放banner 暂时只支持用到的功能
 * 创 建 人：范要朋
 * 创建日期：2019/3/18 8:58
 * 邮    箱：1094325366@qq.com
 * 修订历史：
 * 修 改 人：
 *
 * @author 范要朋
 */

public class BannerView extends FrameLayout {
    private ViewPager mPager;
    private int mCurPlayPosition = -1;
    private BannerAdapter mPageAdapter;
    private LinearLayout mIndicatorContainer;
    private AutoPlayHandler autoPlayHandler;
    private Drawable mIndicatorSelectedShape;
    private Drawable mIndicatorUnselectedShape;
    private int mCurPagePosition;
    private ImageLoader mImageLoader;

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

        public AutoPlayHandler(Callback callback) {
            super(callback);
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
                int count = mPageAdapter.getCount();
                mCurPagePosition = position;
                if (position == 0 || position == count - 1) {
                    return;
                }
                List<View> views = mPageAdapter.getAllView();
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
        mPager.setCurrentItem(position + 1);
    }


    public void setData(final List<BannerData> data) {
        Jzvd.resetAllVideos();
        mPageAdapter = new BannerAdapter(getContext(), data);
        mPager.setAdapter(mPageAdapter);
        addIndicator(data.size());
        if (data.size() > 1) {
            mPager.setCurrentItem(1);
            mCurPagePosition = 1;
        } else {
            updateIndicator(0);
            mCurPagePosition = 0;
        }
        registerVideoStatus();
        List<View> views = mPageAdapter.getAllView();
        for (int i = 0; i < views.size(); i++) {
            final int finalI = i;
            View view = views.get(i);
            if (view instanceof HDVideo && fullScreenClickListener != null) {
                ((HDVideo) view).setOnFullScreenClickListener(new HDVideo.OnFullScreenClickListener() {
                    @Override
                    public void onFullScreenClick(long position) {
                        fullScreenClickListener.onFullScreenClick(position);
                    }
                });
            }
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        int index = finalI - 1;
                        if (index < 0) {
                            index = 0;
                        }
                        mItemClickListener.onItemClick(index);
                    }
                }
            });
        }
        if (autoPlayHandler != null) {
            autoPlayHandler.removeMessages(1);
        }
        autoPlayHandler = new AutoPlayHandler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 1) {
                    int count = data.size();
                    mCurPagePosition = mCurPagePosition % (count + 1) + 1;
                    mPager.setCurrentItem(mCurPagePosition);
                    autoPlayHandler.sendEmptyMessageDelayed(1, 2500);
                }
                return false;
            }
        });
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
                    owner.getLifecycle().removeObserver(this);

                }
            });
        }
    }

    private boolean hasVideo() {
        List<View> views = mPageAdapter.getAllView();
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
        List<View> views = mPageAdapter.getAllView();
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
        List<View> views = mPageAdapter.getAllView();
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
        private Context context;
        private List<BannerData> mData;
        private List<View> mView;
        private int originSize;

        BannerAdapter(Context context, List<BannerData> data) {
            this.context = context;
            originSize = data.size();
            int size = originSize + 2;
            mView = new ArrayList<>(size);
            mData = new ArrayList<>(size);
            if (data.size() > 1) {
                for (int i = 0; i < size; i++) {
                    BannerData curData;
                    if (i == 0) {
                        curData = data.get(data.size() - 1);
                    } else if (i == size - 1) {
                        curData = data.get(0);
                    } else {
                        curData = data.get(i - 1);
                    }
                    mData.add(curData);
                }
            } else {
                mData = data;
            }
            for (int i = 0; i < mData.size(); i++) {
                View result;
                if (mData.get(i).video) {
                    result = new HDVideo(context);
                } else {
                    ImageView imageView = new ImageView(context);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    result = imageView;
                }
                mView.add(result);
            }
        }

        List<View> getAllView() {
            return mView;
        }

        @Override
        public int getCount() {
            return mView.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View result = mView.get(position % getCount());
            if (result instanceof ImageView) {
                if (mImageLoader != null) {
                    mImageLoader.loadImage((ImageView) result, mData.get(position).url);
                }
            }
            if (result instanceof HDVideo && position > 0 && position < originSize) {
                HDVideo video = (HDVideo) result;
                video.setUp(mData.get(position).url, "", Jzvd.SCREEN_NORMAL);
                ImageView thumb = video.thumbImageView;
                thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (mImageLoader != null) {
                    mImageLoader.loadImage(thumb, mData.get(position).previewUrl);
                }
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
