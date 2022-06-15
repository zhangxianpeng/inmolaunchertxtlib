package com.inmoglass.launcher.ui;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.inmoglass.launcher.R;
import com.inmoglass.launcher.adapter.ApplistAdapter;
import com.inmoglass.launcher.adapter.ViewPagerAdapter;
import com.inmoglass.launcher.bean.AppInfo;
import com.inmoglass.launcher.util.AppUtil;
import com.inmoglass.launcher.view.MyDividerItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
public class AppManagerActivity extends AppCompatActivity {
    private static final String TAG = AppManagerActivity.class.getSimpleName();
    private LinearLayout indicatorLl;
    private List<AppInfo> appList;
    private List<String> defaultList = new ArrayList<>();

    private ViewPager appViewPager;
    private LayoutInflater inflater;
    /**
     * 总的页数
     */
    private int pageCount;
    /**
     * 一页显示的数量
     */
    private int pageSize = 10;
    /**
     * 指示器
     */
    private ImageView[] mDotImageView;
    /**
     * 记录当前选中的位置
     */
    private int currentSelectedPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestFullScrren();
        setStatusBarBgColor();
        setContentView(R.layout.activity_app_manager);
        RelativeLayout backRl = findViewById(R.id.rl_back_touch);
        backRl.setOnClickListener(v -> finish());
        TextView titleTv = findViewById(R.id.tv_title);
        titleTv.setText(getResources().getText(R.string.string_app_list));

        indicatorLl = findViewById(R.id.ll_dot);
        appList = new ArrayList<>();
        defaultList.add("com.inmoglass.launcher");
        defaultList.add("com.inmo.settings");
        defaultList.add("com.yulong.coolcamera");
        defaultList.add("com.inmoglass.music");
        defaultList.add("com.koushikdutta.vysor");
        defaultList.add("com.inmo.translation");
        defaultList.add("com.inmo.settings");
        defaultList.add("com.inmolens.inmomemo");
        defaultList.add("com.inmolens.voiceidentify");
        defaultList.add("com.inmoglass.sensorcontrol");
        filterApp();
        initViewPager();
    }

    private void setStatusBarBgColor() {
        Window window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.black));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void filterApp() {
        // 查询launcher非系统应用
        PackageManager pm = getPackageManager();
        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfos) {
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !defaultList.contains(packageInfo.packageName)) {
                AppInfo info = new AppInfo();
                info.appName = packageInfo.applicationInfo.loadLabel(pm).toString();
                info.pkgName = packageInfo.packageName;
                info.appIcon = packageInfo.applicationInfo.loadIcon(pm);
                info.appIntent = pm.getLaunchIntentForPackage(packageInfo.packageName);
                appList.add(info);
            }
        }
    }

    private void initViewPager() {
        appViewPager = findViewById(R.id.vp_app);
        inflater = LayoutInflater.from(this);
        pageCount = (int) Math.ceil(appList.size() * 1.0 / pageSize);
        final List<View> viewList = new ArrayList<>();
        for (int i = 0; i < pageCount; i++) {
            RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.item_viewpager, appViewPager, false);
            GridLayoutManager layoutManager = new GridLayoutManager(this, 5);
            recyclerView.setLayoutManager(layoutManager);
            ApplistAdapter adapter = new ApplistAdapter(this, appList, i, pageSize);
            int leftRightSpace = (int) getResources().getDimension(R.dimen.pt_10);
            int topBottomSpace = (int) getResources().getDimension(R.dimen.pt_10);
            recyclerView.addItemDecoration(new MyDividerItem(leftRightSpace, topBottomSpace));
            adapter.setOnItemClickListener((parent, view, position) -> {
                if (position >= 0) {
                    // 选中效果
                    adapter.setSelecteded((int) adapter.getItemId(position));
                    adapter.notifyDataSetChanged();
//                    AppUtil.getInstance().openApplication(appList.get((int) adapter.getItemId(position)).pkgName);
                }
            });
            recyclerView.setAdapter(adapter);
            viewList.add(recyclerView);
        }
        ViewPagerAdapter adapter = new ViewPagerAdapter(viewList);
        appViewPager.setAdapter(adapter);
        setIndicatorLayout();
    }

    private void setIndicatorLayout() {
        mDotImageView = new ImageView[pageCount];
        for (int i = 0; i < pageCount; i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.layout_dot, null);
            view.findViewById(R.id.dot).setBackgroundResource(R.drawable.dot_selected);
            mDotImageView[i] = new ImageView(this);
            if (i == 0) {
                mDotImageView[i].setBackgroundResource(R.drawable.dot_selected);
            } else {
                mDotImageView[i].setBackgroundResource(R.drawable.dot_normal);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                int left = (int) this.getResources().getDimension(R.dimen.pt_5);
                int right = (int) this.getResources().getDimension(R.dimen.pt_10);
                layoutParams.setMargins(left, 0, right, 0);
                mDotImageView[i].setLayoutParams(layoutParams);
            }
            indicatorLl.addView(mDotImageView[i]);
        }
        appViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setIndicator(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setIndicator(int position) {
        position %= pageCount;
        for (int i = 0; i < pageCount; i++) {
            if (mDotImageView != null) {
                mDotImageView[i].setBackgroundResource(R.drawable.dot_selected);
                if (position != i) {
                    mDotImageView[i].setBackgroundResource(R.drawable.dot_normal);
                }
            }
        }
    }

    private void requestFullScrren() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


}