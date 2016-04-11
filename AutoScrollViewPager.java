package hxy.com.autoscrollviewpager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 2016/4/11.
 */
public class AutoScrollViewPager<T> extends FrameLayout{

    //轮播图图片数量
    private int IMAGE_COUNT = 0;
    //当前轮播页
    private int currentItem = 0;

    //指示器ImageView的list
    private List<View> dotViewsList = new ArrayList<>();
    //轮播图片ImageView的list
    private List<ImageView> imageViewsList = new ArrayList<>();
    //适配器ViewPagerAdapter
    private MyPagerAdapter adapter = new MyPagerAdapter();
    //计时器Run+Handler
    private MHandler handler = new MHandler();
    //自动轮播状态
    private boolean isPlay = true;

    //自动轮播的时间间隔
    private long TIME_INTERVAL = 3*1000;
    //自动轮播启用开关
    private boolean isAutoPlay = false;
    //自定义轮播图的资源
    private List<T> imageUrls;
    //自定义占位图
    private int placeImage;

    private Context context;
    private ViewPager viewPager;
    private LinearLayout dotLayout;
    private AutoScrollerViewPagerListener listener;

    public AutoScrollViewPager(Context context) {
        super(context);
        initview(context);
    }

    public AutoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initview(context);
    }

    public AutoScrollViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initview(context);
    }

    private void initview(Context context){
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.autoscrollviewpager, this, true);
        dotLayout = (LinearLayout)findViewById(R.id.AutoScrollViewLayout);
        viewPager = (ViewPager) findViewById(R.id.AutoScrollViewPager);
        viewPager.setOnPageChangeListener(new MyPageChangeListener());
        viewPager.setAdapter(adapter);
    }

    private void setView(){
        if(imageUrls == null || imageUrls.size() == 0) return;
        imageViewsList.clear();
        for (int i = 0; i < imageUrls.size(); i++) {
            ImageView view = new ImageView(context);
            view.setScaleType(ImageView.ScaleType.FIT_XY);
            imageViewsList.add(view);
            final int position = (i==0)?IMAGE_COUNT:(i>IMAGE_COUNT)?1:i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        listener.onPageClicked(position - 1);
                    }
                }
            });
        }
        dotLayout.removeAllViews();
        for (int i = 0; i <IMAGE_COUNT; i++) {
            ImageView dotView =  new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(5, 4, 5, 4);
            dotLayout.addView(dotView, params);
            dotViewsList.add(dotView);
        }
        currentItem = 1;
        handler.removeCallbacks(play);
        adapter.notifyDataSetChanged();
        viewPager.setCurrentItem(currentItem, false);
    }

    private class MyPagerAdapter  extends PagerAdapter {

        @Override
        public void destroyItem(View container, int position, Object object) {
            // TODO Auto-generated method stub
            ((ViewPager)container).removeView(imageViewsList.get(position));
        }

        @Override
        public Object instantiateItem(View container, int position) {
            ImageView iv = imageViewsList.get(position);
            Glide.with(context).load(imageUrls.get(position)).placeholder(placeImage).fitCenter().into(iv);
            ((ViewPager)container).addView(imageViewsList.get(position));
            return imageViewsList.get(position);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return imageViewsList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            // TODO Auto-generated method stub
            return arg0 == arg1;
        }

        @Override
        public int getItemPosition(Object object)   {
            //return super.getItemPosition(object);
            return POSITION_NONE;
        }
    }

    private class MyPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub
//            switch (arg0) {
//                case 1:
//                    isPlay = false;
//                    break;
//                case 2:
//                    isPlay = true;
//                    break;
//                case 0:
//                    // 当前为最后一张，此时从右向左滑，则切换到第一张
//                    if (viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1 && !isAutoPlay) {
//                        viewPager.setCurrentItem(0);
//                    }
//                    // 当前为第一张，此时从左向右滑，则切换到最后一张
//                    else if (viewPager.getCurrentItem() == 0 && !isAutoPlay) {
//                        viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);
//                    }
//                    break;
//            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageSelected(int position) {
            // TODO Auto-generated method stub
            if (position == 0){
                currentItem = IMAGE_COUNT;
                viewPager.setCurrentItem(currentItem, false);
            }if (position > IMAGE_COUNT){
                currentItem = 1;
                viewPager.setCurrentItem(currentItem, false);
            }else {
                currentItem = position;
                setDotLayout(currentItem - 1);
                if (isAutoPlay) {
                    handler.removeCallbacks(play);
                    handler.postDelayed(play, TIME_INTERVAL);
                }
                if (listener != null){
                    listener.onPageSelected(currentItem - 1);
                }
            }
        }
    }

    private void setDotLayout(int position) {
        if (position < 0 || position >= IMAGE_COUNT) return;
        for (int i = 0; i < IMAGE_COUNT; i++) {
            if (i == position) {
                ((View) dotViewsList.get(position)).setBackgroundResource(R.drawable.ic_dot1);
            } else {
                ((View) dotViewsList.get(i)).setBackgroundResource(R.drawable.ic_dot2);
            }
        }
    }

    private Runnable play = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    };

    private class MHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isAutoPlay){
                viewPager.setCurrentItem((currentItem%IMAGE_COUNT)+1, false);
            }
        }
    }

    //公共方法
    public void setImageUrls(List<T> Urls){
        if(Urls == null || Urls.size() == 0) return;
        IMAGE_COUNT = Urls.size();
        if (imageUrls != null){
            imageUrls.clear();
            imageUrls.addAll((List<T>)Urls);
        }else{
            imageUrls = Urls;
        }
        imageUrls.add(0, (T)Urls.get(Urls.size()-1));
        imageUrls.add((T)Urls.get(1));
        setView();
    }

    public void setplaceImage(int resources){
        placeImage = resources;
    }

    public void setTime(long time){
        if (time > 0){
            TIME_INTERVAL = time;
        }
    }

    public void start(){
        if (!isAutoPlay && imageUrls != null && imageUrls.size() > 0){
            isAutoPlay = true;
            handler.postDelayed(play, TIME_INTERVAL);
        }
    }

    public void stop(){
        isAutoPlay = false;
        handler.removeCallbacks(play);
    }

    public boolean isPlay(){
        return isAutoPlay;
    }

    public int getCount(){
        return IMAGE_COUNT;
    }

    public int getCurrentItem(){
        return currentItem - 1;
    }

    public void setAutoPagerListener(AutoScrollerViewPagerListener listener){
        try{
            this.listener = listener;
        }catch (Exception e){

        }
    }

    //事件监听
    public interface AutoScrollerViewPagerListener{
        public void onPageSelected(int position);
        public void onPageClicked(int position);
    }

}
