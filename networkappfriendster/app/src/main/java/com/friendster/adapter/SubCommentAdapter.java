package com.friendster.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.friendster.R;
import com.friendster.model.CommentModel;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import util.AgoDateParse;

public class SubCommentAdapter extends RecyclerView.Adapter< SubCommentAdapter.ViewHolder> {
    Context context;
    List<CommentModel.Comment> comments;
   
    public  SubCommentAdapter (Context context, List<CommentModel.Comment> comments){
        this.context=context;
        this.comments=comments;
       
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_single_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final CommentModel.Comment result=comments.get(position);
        holder.commentPerson.setText(result.getName());
        holder.commentBody.setText(result.getComment());
        if(!result.getProfilUrl().equals("")){
            Picasso.with(context).load(result.getProfilUrl()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.img_default_user).into(holder.commentProfile, new com.squareup.picasso.Callback(){
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(context).load(result.getProfilUrl()).placeholder(R.drawable.img_default_user).into((holder.commentProfile));

                }
            });
        }
        try {
            holder.commentDate.setText(AgoDateParse.getTimeAgo(AgoDateParse.getTimeInMillsecond(result.getCommentDate())));
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return comments.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.comment_profile)
        ImageView commentProfile;
        @BindView(R.id.comment_person)
        TextView commentPerson;
        @BindView(R.id.option_id)
        ImageView optionId;
        @BindView(R.id.comment_body)
        TextView commentBody;
        @BindView(R.id.comment_date)
        TextView commentDate;
        @BindView(R.id.reply_txt)
        TextView replyTxt;
        @BindView(R.id.more_comments)
        TextView moreComments;
        @BindView(R.id.sub_comment_profile)
        ImageView subCommentProfile;
        @BindView(R.id.sub_comment_person)
        TextView subCommentPerson;
        @BindView(R.id.sub_comment_body)
        TextView subCommentBody;
        @BindView(R.id.sub_comment_date)
        TextView subCommentDate;
        @BindView(R.id.sub_comment_section)
        LinearLayout subCommentSection;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
