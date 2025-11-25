package com.example.tiktokexperience.ViewHolder;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tiktokexperience.R;

public class ItemViewHolder extends RecyclerView.ViewHolder {
    public ImageView ivPostImage;
    public ImageView ivAvatar;
    public ImageView ivLikeIcon;
    public TextView tvTitle;
    public TextView tvUserName;
    public TextView tvLikeCount;

    public ItemViewHolder(@NonNull View itemView) {
        super(itemView);
        // 绑定 item_post.xml 中的控件
        ivPostImage = itemView.findViewById(R.id.ivPostImage);
        tvTitle = itemView.findViewById(R.id.tvTitle);
        ivAvatar = itemView.findViewById(R.id.ivAvatar);
        tvUserName = itemView.findViewById(R.id.tvUserName);
        ivLikeIcon = itemView.findViewById(R.id.ivLikeIcon);
        tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
    }

}
