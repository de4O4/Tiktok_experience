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
    private TextView tvHeader;
    // 用户管理
    private UserManager userManager;
    private ItemAdapter adapter;
    private List<PostItem> dataList = new ArrayList<>();
    private ExecutorService networkExecutor = Executors.newFixedThreadPool(5);

    private boolean isStaggered = true;     // 布局状态标记

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


    }


    private void setupBottomNavigation(BottomNavigationView navView) {
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                return true;
            } else if (itemId == R.id.action_login) {
                if (userManager.isLoggedIn()) {
                    Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 确保用户登录状态在返回时得到正确处理
        if (!userManager.isLoggedIn()) {
            Toast.makeText(this, "您当前处于访客模式，点赞记录将不会保存", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "欢迎回来，" + userManager.getUsername(), Toast.LENGTH_SHORT).show();
        }
    }
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        tvHeader = findViewById(R.id.tvHeader);

        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemViewCacheSize(20);
        recyclerView.setHasFixedSize(true);

        adapter = new ItemAdapter(this, dataList);
        recyclerView.setAdapter(adapter);
    }


    private void setLayoutManager() {           // 切换单列/双列
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

    private void initListeners() {      // 刷新逻辑
        swipeRefreshLayout.setOnRefreshListener(() -> {
            generateMockDataAsync(10, 0, newData -> {
                adapter.refreshData(newData);
                swipeRefreshLayout.setRefreshing(false);
            });
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {        // 获取可见的最后项目位置
                    StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                    int[] lastVisiblePositions = layoutManager.findLastVisibleItemPositions(null);
                    int lastVisiblePos = Math.max(lastVisiblePositions[0], lastVisiblePositions[1]);

                    if (lastVisiblePos >= adapter.getItemCount() - 5) { // 距离底部5个item时开始预加载
                        preloadNextBatchIfNeeded();
                    }
                }
            }
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (!recyclerView.canScrollVertically(1)) {         //若已滚动到最后
                    int currentSize = adapter.getItemCount();
                    generateMockDataAsync(6, currentSize, moreData -> {
                        adapter.addData(moreData);
                    });
                }
            }
        });


        tvHeader.setOnClickListener(v -> {
            isStaggered = !isStaggered;
            setLayoutManager();
            adapter.notifyDataSetChanged();

        });
    }

    private void initData() {
        PreloadManager preloadManager = PreloadManager.getInstance(this);
        List<PostItem> preloadedData = preloadManager.getPreloadedData();

        if (preloadedData != null && !preloadedData.isEmpty()) {
            List<PostItem> dataToUse = new ArrayList<>();
            int count = Math.min(20, preloadedData.size());
            for (int i = 0; i < count; i++) {
                dataToUse.add(preloadedData.get(i));
            }
            adapter.refreshData(dataToUse);

            loadMoreDataInBackground(preloadedData.size());
        } else {
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
    private boolean isPreloadingNextBatch = false; // 防止重复预加载

    private void preloadNextBatchIfNeeded() {
        if (isPreloadingNextBatch) {
            return;
        }
        isPreloadingNextBatch = true;

        PreloadManager preloadManager = PreloadManager.getInstance(this);
        preloadManager.preloadNextBatch(10, data -> {
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


            if (imgPath.startsWith("//")) {
                imgPath = "https:" + imgPath;
            }

            return imgPath;
        } catch (Exception e) {
            e.printStackTrace();
            return "https://c-ssl.duitang.com/uploads/blog/202510/04/OoSz2d9Gs6b8Wg6.jpg";
        }
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

                String id = "post_" + realIndex + random;
                long randomStamp = System.currentTimeMillis() + i;

                String qqNum = String.valueOf(100000000 + random.nextInt(899999999));
                String avatarUrl = "https://q1.qlogo.cn/g?b=qq&nk=" + qqNum + "&s=100";


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

    interface DataCallback {
        void onDataReady(List<PostItem> data);
    }
}