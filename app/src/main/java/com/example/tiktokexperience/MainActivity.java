package com.example.tiktokexperience;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tiktokexperience.Adapter.ItemAdapter;
import com.example.tiktokexperience.Bean.PostItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.example.tiktokexperience.User.UserManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;      //下拉刷新
    private TextView tvHeader; // 顶部标题
    // 用户管理
    private UserManager userManager;
    private ItemAdapter adapter;
    private List<PostItem> dataList = new ArrayList<>();

    // 布局状态标记
    private boolean isStaggered = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userManager = UserManager.getInstance(this);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        initViews();
        initData();
        initListeners();

        // 如果用户未登录，显示提示
        if (!userManager.isLoggedIn()) {
            Toast.makeText(this, "您当前处于访客模式，点赞记录将不会保存", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "欢迎回来，" + userManager.getUsername(), Toast.LENGTH_SHORT).show();
        }
        initViews();
        initData();
        initListeners();
    }


//    private void updateBottomMenuState() {
//        if (navView == null) return;
//
//        Menu menu = navView.getMenu();
//        MenuItem meItem = menu.findItem(R.id.navigation_home); // 确保这里ID和你 menu xml 里的一致
//
//        if (userManager.isLoggedIn()) {
//            meItem.setTitle("用户: " + userManager.getUsername());
//           // meItem.setIcon(R.drawable.ic_user_logged_in); // 可选：登录后换个图标
//        } else {
//            meItem.setTitle("登录"); // 或者 "我的"
//           // meItem.setIcon(R.drawable.ic_user_normal); // 可选：换回默认图标
//        }
//    }


    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        tvHeader = findViewById(R.id.tvHeader);

        // === 核心：设置双列瀑布流布局 ===
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        // 防止 Item 位置在刷新时跳动
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);

        recyclerView.setLayoutManager(layoutManager);

        // 优化缓存
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setHasFixedSize(true);

        adapter = new ItemAdapter(this, dataList);
        recyclerView.setAdapter(adapter);
    }

    // 切换布局逻辑 (单列/双列)
    private void setLayoutManager() {
        if (isStaggered) {      //瀑布流双列
            StaggeredGridLayoutManager layoutManager =
                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
            recyclerView.setLayoutManager(layoutManager);
        } else {                //单列模式
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
        }
    }

    private void initListeners() {
        // 刷新逻辑
        swipeRefreshLayout.setOnRefreshListener(() -> {
            new Handler().postDelayed(() -> {
                // 刷新时生成带 ID 的数据
                adapter.refreshData(generateMockData(10, 0));
                swipeRefreshLayout.setRefreshing(false);
            }, 1000);
        });

        // 加载更多 (简单版)
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (!recyclerView.canScrollVertically(1)) {         //若已滚动到最后
                    // 获取当前最后一条的索引用于生成唯一ID
                    int currentSize = adapter.getItemCount();
                    List<PostItem> more = generateMockData(6, currentSize);
                    adapter.addData(more);
                }
            }
        });

        // 顶部标题点击事件 - 切换布局
        tvHeader.setOnClickListener(v -> {
            isStaggered = !isStaggered; // 切换状态
            setLayoutManager();
            adapter.notifyDataSetChanged(); // 重新绑定视图以适应新布局

        });
    }

    private void initData() {
        adapter.refreshData(generateMockData(20, 0));

    }

    // 生成 Mock 数据 (必须保证 ID 稳定，以便测试持久化)
    // 生成 Mock 数据 (使用 Picsum 生成无限不重复图片)
// 生成 Mock 数据 (中国大陆专用版)
    private List<PostItem> generateMockData(int count, int startIndex) {
        List<PostItem> list = new ArrayList<>();
        Random random = new Random();

        String[] titles = {
                "上海看展｜这个展真的太好拍了！",
                "今日份OOTD，显瘦穿搭分享",
                "沉浸式护肤，又是精致的一天",
                "猫咪迷惑行为大赏 #萌宠",
                "家常菜做法，简单又好吃",
                "深圳周末去哪儿？小众打卡地",
                "这是什么神仙颜值！爱了爱了",
                "打工人日常，今天也要加油鸭",
                "数码博主：iPhone 19 爆料汇总",
                "旅行 Vlog | 去有风的地方"
        };

        for (int i = 0; i < count; i++) {
            int realIndex = startIndex + i;
            // ID 用于持久化点赞状态
            String id = "post_" + realIndex;
            long randomStamp = System.currentTimeMillis() + i;


            String qqNum = String.valueOf(100000000 + random.nextInt(899999999));
            String avatarUrl = "https://q1.qlogo.cn/g?b=qq&nk=" + qqNum + "&s=100";


            String imageUrl;
            if (i % 2 == 0) {
                // 偶数个用 动漫/风景 (横屏或方图居多)
                imageUrl = "https://api.btstu.cn/sjbz/api.php?lx=dongman&v=" + realIndex+ "&t=" + randomStamp;
            } else {
                // 奇数个用 手机壁纸 (竖屏，适合瀑布流)
                imageUrl = "https://api.btstu.cn/sjbz/api.php?method=mobile&format=images&v=" + realIndex+ "&t=" + randomStamp;
            }

            String title = titles[random.nextInt(titles.length)];

            list.add(new PostItem(
                    id,
                    imageUrl,
                    title,
                    avatarUrl,
                    "用户_" + qqNum.substring(0, 4), // 模拟用户名
                    random.nextInt(2000) + 50 // 随机点赞数
            ));
        }
        return list;
    }
}