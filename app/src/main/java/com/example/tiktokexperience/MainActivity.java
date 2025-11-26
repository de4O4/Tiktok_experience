package com.example.tiktokexperience;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tiktokexperience.Adapter.ItemAdapter;
import com.example.tiktokexperience.Bean.PostItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;      //下拉刷新

    private ItemAdapter adapter;
    private List<PostItem> dataList = new ArrayList<>();

    // 布局状态标记
    private boolean isStaggered = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initData();
        initListeners();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

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
        if (isStaggered) {
            StaggeredGridLayoutManager layoutManager =
                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
            recyclerView.setLayoutManager(layoutManager);
        } else {
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
                if (!recyclerView.canScrollVertically(1)) {
                    // 获取当前最后一条的索引用于生成唯一ID
                    int currentSize = adapter.getItemCount();
                    List<PostItem> more = generateMockData(6, currentSize);
                    adapter.addData(more);
                }
            }
        });

        // 布局切换按钮点击事件
      //  fabSwitchLayout.setOnClickListener(v -> {
         //   isStaggered = !isStaggered; // 切换状态
         //   setLayoutManager();
          //  adapter.notifyDataSetChanged(); // 重新绑定视图以适应新布局
       // });
    }

    private void initData() {
        adapter.refreshData(generateMockData(20, 0));
    }

    // 生成 Mock 数据 (必须保证 ID 稳定，以便测试持久化)
    private List<PostItem> generateMockData(int count, int startIndex) {
        List<PostItem> list = new ArrayList<>();
        Random random = new Random();

        String[] imageUrls = {
                "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&q=80",
                "https://images.unsplash.com/photo-1539683255143-d456bf564b72?w=500&q=80",
                "https://images.unsplash.com/photo-1481349518771-20055b2a7b24?w=500&q=80",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500&q=80"
        };
        String[] titles = {
                "上海看展｜这个展真的太好拍了！",
                "今日份OOTD，显瘦穿搭分享",
                "沉浸式护肤，又是精致的一天",
                "猫咪迷惑行为大赏 #萌宠",
                "家常菜做法，简单又好吃"
        };

        for (int i = 0; i < count; i++) {
            int realIndex = startIndex + i;
            // ID 使用 "post_" + 索引，确保重启 App 后 ID 一致，从而能读取到 SP 中的状态
            String id = "post_" + realIndex;

            // 为了视觉效果，随机取图和标题，但在真实项目中应由 ID 决定内容
            String img = imageUrls[random.nextInt(imageUrls.length)];
            String title = titles[random.nextInt(titles.length)];

            list.add(new PostItem(
                    id,
                    img,
                    title,
                    "https://i.pravatar.cc/150?u=" + id, // 根据 ID 生成头像
                    "用户_" + realIndex,
                    100 + realIndex // 基础点赞数
            ));
        }
        return list;
    }
}