package com.example.tiktokexperience.Optimize;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tiktokexperience.Bean.PostItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PreloadManager {
    private static final String TAG = "PreloadManager";
    private static PreloadManager instance;
    private Context context;
    private List<PostItem> preloadData;
    private ExecutorService networkExecutor = Executors.newFixedThreadPool(5);
    private boolean isPreloaded = false;

    private PreloadManager(Context context) {
        this.context = context.getApplicationContext();
        this.preloadData = new ArrayList<>();
    }

    public static synchronized PreloadManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreloadManager(context);
        }
        return instance;
    }

    // 预加载帖子数据
    public void preloadData(int count, PreloadCallback callback) {
        if (isPreloaded) {
            callback.onPreloadComplete(preloadData);
            return;
        }

        networkExecutor.execute(() -> {
            List<PostItem> list = generatePreloadData(count);
            preloadData.clear();
            preloadData.addAll(list);
            isPreloaded = true;
            // 预加载图片
            preloadImages(list);

            // 在主线程回调
            if (context != null) {
                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                mainHandler.post(() -> {
                    callback.onPreloadComplete(list);
                });
            }
        });
    }

    // 生成预加载数据
    private List<PostItem> generatePreloadData(int count) {
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
            int realIndex = i;
            String id = "preload_post_" + realIndex + random.nextInt(10000);
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
                    "用户_" + qqNum.substring(0, 4),
                    random.nextInt(2000) + 50
            ));
        }
        return list;
    }

    // 预加载图片资源
    private void preloadImages(List<PostItem> postItems) {
        Log.d(TAG, "开始预加载图片，数量: " + postItems.size());
        for (PostItem item : postItems) {
            // 预加载帖子图片
            Glide.with(context)
                    .load(item.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .preload();

            // 预加载用户头像
            Glide.with(context)
                    .load(item.getUserAvatar())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .preload();
        }
    }

    // 预加载下一批数据，用于无缝滚动体验
    public void preloadNextBatch(int count, PreloadCallback callback) {
        Log.d(TAG, "开始预加载下一批数据，数量: " + count);
        networkExecutor.execute(() -> {
            List<PostItem> list = generatePreloadData(count);

            // 预加载图片
            preloadImages(list);

            // 在主线程回调
            if (context != null) {
                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                mainHandler.post(() -> {
                    callback.onPreloadComplete(list);
                });
            }
        });
    }

    // 检查特定URL是否已缓存
    public boolean isImageCached(String imageUrl) {
        // 这里可以使用Glide的缓存检查功能
        // 为了简化，返回false，实际应用中可以实现更复杂的缓存检查
        return false;
    }

    // 清除特定缓存
    public void clearCache() {
        if (context != null) {
            Glide.get(context).clearMemory();
            networkExecutor.execute(() -> {
                Glide.get(context).clearDiskCache();
            });
        }
    }

    // 从API获取图片URL
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
            return "https://c-ssl.duitang.com/uploads/blog/202510/04/OoSz2d9Gs6b8Wg6.jpg";
        }
    }

    // 获取预加载的数据
    public List<PostItem> getPreloadedData() {
        return isPreloaded ? new ArrayList<>(preloadData) : null;
    }

    // 检查是否已完成预加载
    public boolean isPreloaded() {
        return isPreloaded;
    }

    // 清除预加载数据
    public void clearPreloadedData() {
        preloadData.clear();
        isPreloaded = false;
    }

    public interface PreloadCallback {
        void onPreloadComplete(List<PostItem> data);
    }
}