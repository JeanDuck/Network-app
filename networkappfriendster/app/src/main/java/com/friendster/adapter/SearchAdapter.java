package com.friendster.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.friendster.R;
import com.friendster.activity.ProfileActivity;
import com.friendster.model.User;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    Context context;
    List<User> users;
    public SearchAdapter(Context context, List<User>users){
        this.context=context;
        this.users=users;

    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_list, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user=users.get(position);
        //将列表相应位置的文本改成这个user的name
        holder.userName.setText(user.getName());
        //如果用户的profileurl不是空的，那么下载响应图片，利用picasso
        if(!user.getProfileUrl().isEmpty()){
            Picasso.with(context).load(user.getProfileUrl()).placeholder(R.drawable.default_image_placeholder).networkPolicy(NetworkPolicy.OFFLINE).into(holder.userImage,new com.squareup.picasso.Callback(){
                @Override
                public void onSuccess() {


                }

                @Override
                public void onError() {

                }
            });

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager im=(InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                //隐藏键盘
                im.hideSoftInputFromWindow(v.getWindowToken(),0);
                //点击某一user那一栏后，跳转到相应的activity
                context.startActivity(new Intent(context, ProfileActivity.class).putExtra("uid",user.getUid()));
            }
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_image)
        ImageView userImage;
        @BindView(R.id.user_name)
        TextView userName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


}
