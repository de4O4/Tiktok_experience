package com.example.tiktokexperience;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import com.example.tiktokexperience.User.UserProfileActivity;
import androidx.annotation.NonNull;
import android.content.Intent;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import com.example.tiktokexperience.Optimize.PreloadManager;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;      //下拉刷新
    private TextView tvHeader; // 顶部标题
    // 用户管理
    private UserManager userManager;
    private ItemAdapter adapter;
    private List<PostItem> dataList = new ArrayList<>();
    private ExecutorService networkExecutor = Executors.newFixedThreadPool(5);
    // 布局状态标记
    private boolean isStaggered = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userManager = UserManager.getInstance(this);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        setupBottomNavigation(navView);
        initViews();
        initData();
        initListeners();

        // 如果用户未登录，显示提示
        if (!userManager.isLoggedIn()) {
            Toast.makeText(this, "您当前处于访客模式，点赞记录将不会保存", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "欢迎回来，" + userManager.getUsername(), Toast.LENGTH_SHORT).show();
        }

    }


    private void setupBottomNavigation(BottomNavigationView navView) {
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                // 首页，什么都不做，保持当前页面
                return true;
            } else if (itemId == R.id.action_login) {
                // 点击"我的"，根据登录状态决定跳转
                if (userManager.isLoggedIn()) {
                    // 已登录，跳转到用户个人中心
                    Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                } else {
                    // 未登录，跳转到登录界面
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                return true;
            }
            return false;
        });
    }


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
            generateMockDataAsync(10, 0, newData -> {
                adapter.refreshData(newData);
                swipeRefreshLayout.setRefreshing(false);
            });
        });

        // 加载更多 (简单版)
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 获取可见的最后项目位置
                if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                    StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                    int[] lastVisiblePositions = layoutManager.findLastVisibleItemPositions(null);
                    int lastVisiblePos = Math.max(lastVisiblePositions[0], lastVisiblePositions[1]);

                    // 当滚动到接近底部时预加载下一批数据
                    if (lastVisiblePos >= adapter.getItemCount() - 5) { // 距离底部5个item时开始预加载
                        preloadNextBatchIfNeeded();
                    }
                }
            }
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (!recyclerView.canScrollVertically(1)) {         //若已滚动到最后
                    // 获取当前最后一条的索引用于生成唯一ID
                    int currentSize = adapter.getItemCount();
                    generateMockDataAsync(6, currentSize, moreData -> {
                        adapter.addData(moreData);
                    });
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
        // 检查是否有预加载的数据
        PreloadManager preloadManager = PreloadManager.getInstance(this);
        List<PostItem> preloadedData = preloadManager.getPreloadedData();

        if (preloadedData != null && !preloadedData.isEmpty()) {
            // 使用预加载的数据
            List<PostItem> dataToUse = new ArrayList<>();
            // 取前20条数据，或者全部预加载的数据（如果少于20条）
            int count = Math.min(20, preloadedData.size());
            for (int i = 0; i < count; i++) {
                dataToUse.add(preloadedData.get(i));
            }
            adapter.refreshData(dataToUse);

            // 同时在后台继续加载更多数据
            loadMoreDataInBackground(preloadedData.size());
        } else {
            // 如果没有预加载数据，使用原来的逻辑
            generateMockDataAsync(20, 0, newData -> {
                adapter.refreshData(newData);
            });
        }
    }

    private void loadMoreDataInBackground(int startIndex) {
        generateMockDataAsync(20, startIndex, moreData -> {
            adapter.addData(moreData);
        });
    }

    // 预加载下一批数据（如果需要）
    private boolean isPreloadingNextBatch = false; // 防止重复预加载

    private void preloadNextBatchIfNeeded() {
        if (isPreloadingNextBatch) {
            return; // 防止重复预加载
        }

        isPreloadingNextBatch = true;

        PreloadManager preloadManager = PreloadManager.getInstance(this);
        preloadManager.preloadNextBatch(10, data -> {
            // 将预加载的数据添加到适配器中，为后续滚动做准备
            adapter.addData(data);
            isPreloadingNextBatch = false;
        });
    }
    private String fetchImageUrlFromAPI() {
        try {
            URL url = new URL("https://img.xjh.me/random_img.php?return=json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();

            JSONObject jsonResponse = new JSONObject(response.toString());
            String imgPath = jsonResponse.getString("img");

            // 如果imgPath以//开头，需要添加https:
            if (imgPath.startsWith("//")) {
                imgPath = "https:" + imgPath;
            }

            return imgPath;
        } catch (Exception e) {
            e.printStackTrace();
            // 如果API调用失败，返回一个默认图片URL
           // return "https://api.btstu.cn/sjbz/api.php?lx=dongman&v=1&t=" + System.currentTimeMillis();
            return "https://c-ssl.duitang.com/uploads/blog/202510/04/OoSz2d9Gs6b8Wg6.jpg";
        }
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
            String id = "post_" + realIndex+random;
            long randomStamp = System.currentTimeMillis() + i;


            String qqNum = String.valueOf(100000000 + random.nextInt(899999999));
            String avatarUrl = "https://q1.qlogo.cn/g?b=qq&nk=" + qqNum + "&s=100";


            // 使用新的API获取图片URL
            String imageUrl = fetchImageUrlFromAPI();

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
    private void generateMockDataAsync(int count, int startIndex, DataCallback callback) {
        networkExecutor.execute(() -> {
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
                String id = "post_" + realIndex + random;
                long randomStamp = System.currentTimeMillis() + i;

                String qqNum = String.valueOf(100000000 + random.nextInt(899999999));
                String avatarUrl = "https://q1.qlogo.cn/g?b=qq&nk=" + qqNum + "&s=100";

                // 从API获取图片URL
                String imageUrl = fetchImageUrlFromAPI();

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

            runOnUiThread(() -> callback.onDataReady(list));
        });
    }

    // 回调接口
    interface DataCallback {
        void onDataReady(List<PostItem> data);
    }
}