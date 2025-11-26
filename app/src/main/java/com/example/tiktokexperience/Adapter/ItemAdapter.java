package com.example.tiktokexperience.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.tiktokexperience.ViewHolder.ItemViewHolder;
import com.example.tiktokexperience.Bean.PostItem;
import com.example.tiktokexperience.LikeManager;
import com.example.tiktokexperience.R;
import java.util.List;


public class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    private Context context;
    private List<PostItem> postList;
    private LikeManager likeManager;

    public ItemAdapter(Context context, List<PostItem> postList) {
        this.context = context;
        this.postList = postList;
        this.likeManager = new LikeManager(context);
    }

    public void addData(List<PostItem> newPosts) {
        postList.addAll(newPosts);
        notifyDataSetChanged();
    }

    public void refreshData(List<PostItem> newPosts) {
        postList.clear();           //先清除界面在进行添加新帖子
        postList.addAll(newPosts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {         //创建视图构建ViewHolder
        View view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {            //将数据绑定到ViewHolder
        PostItem item = postList.get(position);

        // 1. 获取点赞状态
        boolean isLiked = likeManager.isLiked(item.getId());

        // 2. 计算显示数字
        int currentLikeCount = item.getBaseLikeCount() + (isLiked ? 1 : 0);

        // 3. 绑定数据
        holder.tvTitle.setText(item.getTitle());
        holder.tvUserName.setText(item.getUserName());
        holder.tvLikeCount.setText(String.valueOf(currentLikeCount));

        // 4. 更新 UI 状态
        updateLikeIconUI(holder, isLiked);

        // 5. 图片加载
        Glide.with(context)
                .load(item.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.ivPostImage);

        Glide.with(context)             //加载用户头像
                .load(item.getUserAvatar())
                .transform(new CircleCrop())
                .into(holder.ivAvatar);

        // 6. 点击逻辑
        holder.ivLikeIcon.setOnClickListener(v -> {
            boolean currentStatus = likeManager.isLiked(item.getId());
            boolean newStatus = !currentStatus;

            likeManager.setLiked(item.getId(), newStatus);

            int newCount = item.getBaseLikeCount() + (newStatus ? 1 : 0);
            holder.tvLikeCount.setText(String.valueOf(newCount));
            updateLikeIconUI(holder, newStatus);
        });
    }

    private void updateLikeIconUI(ItemViewHolder holder, boolean isLiked) {
        if (isLiked) {
            holder.ivLikeIcon.setColorFilter(0xFFFF2C55);
            holder.ivLikeIcon.setImageResource(R.drawable.zhan_press);
        } else {
            holder.ivLikeIcon.clearColorFilter();
            holder.ivLikeIcon.setImageResource(R.drawable.zhan);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }


}