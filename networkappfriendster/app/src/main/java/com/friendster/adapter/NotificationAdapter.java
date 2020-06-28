package com.friendster.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.friendster.R;
import com.friendster.activity.FullPostActivity;
import com.friendster.activity.ProfileActivity;
import com.friendster.model.NotificationModel;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import util.AgoDateParse;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.Viewholder> {
    Context context;
    List<NotificationModel> notificationModels;

    public NotificationAdapter(Context context, List<NotificationModel> notificationModels){
        this.context=context;
        this.notificationModels=notificationModels;

    }
    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        /*
        * 1. liked your post
        * 2. commented on your post
        * 3. replied on your comment
        * 4. send your friend request
        * 5. accepted your friend request
        * */

        final NotificationModel notificationModel=notificationModels.get(position);

        if(notificationModel.getType().equals("1")){
            holder.notificationTitle.setText(notificationModel.getName()+"liked your post");

        }else if(notificationModel.getType().equals("2")){
            holder.notificationTitle.setText(notificationModel.getName()+"commented on your post");
        }else if(notificationModel.getType().equals("3")){
            holder.notificationTitle.setText(notificationModel.getName()+"replied on your comment");
        }else if(notificationModel.getType().equals("4")){
            holder.notificationTitle.setText(notificationModel.getName()+"send your friend request");
        }else{
            holder.notificationTitle.setText(notificationModel.getName()+"accepted your friend request");
        }

        if(notificationModel.getType().equals("1")||notificationModel.getType().equals("2")||notificationModel.getType().equals("3")){
            holder.notificationBody.setText(notificationModel.getPost()+"");

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(context, FullPostActivity.class);
                    Bundle bundle=new Bundle();
                    //在FullPostActivity中接收isLoadFromNetwork这些数据
                    bundle.putBoolean("isLoadFromNetwork", true);
                    bundle.putString("postId", notificationModel.getPostId());
                    intent.putExtra("postBundle",bundle);
                    context.startActivity(intent);

                }
            });

        }else{
            holder.notificationBody.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //type 为4，5的情况要跳转的页面
                    Intent intent=new Intent(context, ProfileActivity.class);
                    intent.putExtra("uid",notificationModel.getNotificationFrom());
                    context.startActivity(intent);

                }
            });
        }


        if(!notificationModel.getProfileUrl().isEmpty()){

            Picasso.with(context).load(notificationModel.getProfileUrl()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.img_default_user).into(holder.notficationSenderProfile, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(context).load(notificationModel.getProfileUrl()).placeholder(R.drawable.img_default_user).into(holder.notficationSenderProfile);

                }
            });
        }
        try {
            holder.notificationDate.setText(AgoDateParse.getTimeAgo(AgoDateParse.getTimeInMillsecond(notificationModel.getNotificationTime())));
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    @Override
    public int getItemCount() {
        return notificationModels.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        @BindView(R.id.notfication_sender_profile)
        CircleImageView notficationSenderProfile;
        @BindView(R.id.notification_title)
        TextView notificationTitle;
        @BindView(R.id.notification_body)
        TextView notificationBody;
        @BindView(R.id.notification_date)
        TextView notificationDate;

        public Viewholder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


}
