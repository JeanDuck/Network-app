package com.friendster.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.friendster.R;
import com.friendster.adapter.PostAdapter;
import com.friendster.fragment.bottomsheets.CommentBottomSheet;
import com.friendster.model.PostModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import rest.ApiClient;
import rest.services.UserInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import util.AgoDateParse;

public class FullPostActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.post_user_image)
    ImageView postUserImage;
    @BindView(R.id.post_user_name)
    TextView postUserName;
    @BindView(R.id.privacy)
    ImageView privacy;
    @BindView(R.id.post_date)
    TextView postDate;
    @BindView(R.id.status)
    TextView status;
    @BindView(R.id.post_image)
    ImageView postImage;
    @BindView(R.id.top_rel)
    RelativeLayout topRel;
    @BindView(R.id.like_img)
    ImageView likeImg;
    @BindView(R.id.like_txt)
    TextView likeTxt;
    @BindView(R.id.like_section)
    LinearLayout likeSection;
    @BindView(R.id.comment_txt)
    TextView commentTxt;
    @BindView(R.id.comment_section)
    LinearLayout commentSection;
    @BindView(R.id.reaction_card)
    CardView reactionCard;
    @BindView(R.id.comment)
    EditText comment;
    @BindView(R.id.comment_send)
    ImageView commentSend;
    @BindView(R.id.comment_send_wrapper)
    RelativeLayout commentSendWrapper;
    @BindView(R.id.comment_bottom_part)
    LinearLayout commentBottomPart;
    @BindView(R.id.top_hide_show)
    RelativeLayout topHideShow;

    PostModel postModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //在itemView中点击项目，跳转到该activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_post);
        ButterKnife.bind(this);
        //postadapter中有put
        postModel= Parcels.unwrap(getIntent().getBundleExtra("postBundle").getParcelable("postModel"));
        boolean isLoadFromNetwork= getIntent().getBundleExtra("postBundle").getBoolean("isLoadFromNetwork",false);
        String postId= getIntent().getBundleExtra("postBundle").getString("postId","0");
        if(isLoadFromNetwork){
            getPostDataDetails(postId);


        }else{
            postModel= Parcels.unwrap(getIntent().getBundleExtra("postBundle").getParcelable("postModel"));
            setData(postModel);
        }

        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void getPostDataDetails(String postId) {
        UserInterface userInterface=ApiClient.getApiClient().create(UserInterface.class);
        Map<String,String> params=new HashMap<>();
        params.put("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
        params.put("postId",postId);
        Call<PostModel> call=userInterface.getPostDetails(params);
        call.enqueue(new Callback<PostModel>() {
            @Override
            public void onResponse(Call<PostModel> call, Response<PostModel> response) {
                setData(response.body());

            }

            @Override
            public void onFailure(Call<PostModel> call, Throwable t) {
                Toast.makeText(FullPostActivity.this,"something went wrong!",Toast.LENGTH_SHORT).show();
                FullPostActivity.super.onBackPressed();


            }
        });
    }

    private void operationLike(PostModel postModel) {
        likeImg.setImageResource(R.drawable.icon_like_selected);
        int count=Integer.parseInt(postModel.getLikeCount());
        //执行此操作后，说明对该条内容的喜欢的人数增加了
        count++;
        if(count==0||count==1){
            likeTxt.setText(count+"Like");
        }else{
            likeTxt.setText(count+"Likes");
        }
        //更新postmodels中的count值
        postModel.setLikeCount(count+"");
        postModel.setLiked(true);
    }

    private void operationUnlike(PostModel postModel) {
        likeImg.setImageResource(R.drawable.icon_like);
        int count=Integer.parseInt(postModel.getLikeCount());
        //执行此操作后，说明对该条内容的喜欢的人数增加了
        count--;
        if(count==0||count==1){
            likeTxt.setText(count+"Like");
        }else{
            likeTxt.setText(count+"Likes");
        }
        //更新postmodels中的count值
        postModel.setLikeCount(count+"");
        postModel.setLiked(false);

    }

    private void setData(final PostModel postModel) {
        if(postModel.getCommentCount().equals("0")||postModel.getCommentCount().equals("1")){
            commentTxt.setText(postModel.getCommentCount()+"comment");
        }else{
            commentTxt.setText(postModel.getCommentCount()+"comments");

        }
        if(postModel.isLiked()){
            likeImg.setImageResource(R.drawable.icon_like_selected);
        }else {
            likeImg.setImageResource(R.drawable.icon_like);
        }

        if(postModel.getLikeCount().equals("0")||postModel.getLikeCount().equals("1")){
            likeTxt.setText(postModel.getLikeCount()+"Like");
        }else{
            likeTxt.setText(postModel.getLikeCount()+"Likes");

        }
        commentSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialogFragment bottomSheetDialogFragment=new CommentBottomSheet();
                Bundle bundle=new Bundle();
                bundle.putParcelable("postModel",Parcels.wrap(postModel));
                bottomSheetDialogFragment.setArguments(bundle);
                //当前活动的fragmentactivity
                FragmentActivity fragmentActivity= (FragmentActivity) FullPostActivity.this;
                bottomSheetDialogFragment.show(fragmentActivity.getSupportFragmentManager(),"commentFragment");
            }
        });

        likeSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //已经点击后，则使得like选择按钮为false
                likeSection.setEnabled(false);
                if(!postModel.isLiked()){
                    //like operation in here
                    operationLike(postModel);

                    //执行上述操作后，要将结果更新到数据库
                    UserInterface userInterface=ApiClient.getApiClient().create(UserInterface.class);
                    Call<Integer> call=userInterface.likeUlike(new PostAdapter.AddLike(FirebaseAuth.getInstance().getCurrentUser().getUid(),postModel.getPostId(),postModel.getPostUserId(),1));
                    call.enqueue(new Callback<Integer>() {
                        @Override
                        public void onResponse(Call<Integer> call, Response<Integer> response) {
                            likeSection.setEnabled(true);
                            if(response.body().equals("0")){
                                //如果出现了一些错误，那么就需要roll back返回之前的状态，因此在这里用operationUnlike来应对错误情况
                                operationUnlike(postModel);
                                Toast.makeText(FullPostActivity.this,"something went wrong!",Toast.LENGTH_SHORT).show();



                            }
                        }

                        @Override
                        public void onFailure(Call<Integer> call, Throwable t) {
                            likeSection.setEnabled(true);
                            operationUnlike(postModel);
                            Toast.makeText(FullPostActivity.this,"something went wrong!",Toast.LENGTH_SHORT).show();


                        }
                    });



                }else{
                    //unlike operation in here
                    operationUnlike(postModel);

                    UserInterface userInterface=ApiClient.getApiClient().create(UserInterface.class);
                    Call<Integer> call=userInterface.likeUlike(new PostAdapter.AddLike(FirebaseAuth.getInstance().getCurrentUser().getUid(),postModel.getPostId(),postModel.getPostUserId(),0));
                    call.enqueue(new Callback<Integer>() {
                        @Override
                        public void onResponse(Call<Integer> call, Response<Integer> response) {
                            likeSection.setEnabled(true);
                            if(response.body()==null){
                                //如果出现了一些错误，那么就需要roll back返回之前的状态，因此在这里用operationlike来应对错误情况
                                operationLike(postModel);
                                Toast.makeText(FullPostActivity.this,"something went wrong!",Toast.LENGTH_SHORT).show();



                            }
                        }

                        @Override
                        public void onFailure(Call<Integer> call, Throwable t) {
                            likeSection.setEnabled(true);
                            operationLike(postModel);
                            Toast.makeText(FullPostActivity.this,"something went wrong!",Toast.LENGTH_SHORT).show();


                        }
                    });
                }
            }
        });
        postUserName.setText(postModel.getName());
        status.setText(postModel.getPost());
        if(!postModel.getProfileUrl().isEmpty()){
            //getUserProfile()在数据库中是绝对路径
            Picasso.with(FullPostActivity.this).load(postModel.getProfileUrl()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image_placeholder).into(postUserImage, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(FullPostActivity.this).load(postModel.getProfileUrl()).placeholder(R.drawable.default_image_placeholder).error(R.drawable.default_image_placeholder).into(postUserImage);
                }
            });

        }
        if(!postModel.getStatusImage().isEmpty()){
            Picasso.with(FullPostActivity.this).load(ApiClient.BASE_URL_1+postModel.getStatusImage()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image_placeholder).into(postImage, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(FullPostActivity.this).load(ApiClient.BASE_URL_1+postModel.getStatusImage()).placeholder(R.drawable.default_image_placeholder).error(R.drawable.default_image_placeholder).into(postImage);
                }
            });

        }else{
            postImage.setVisibility(View.GONE);
        }
        try {
            postDate.setText(AgoDateParse.getTimeAgo(AgoDateParse.getTimeInMillsecond(postModel.getStatusTime())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(postModel.getPrivacy().equalsIgnoreCase("0")){
            privacy.setImageResource(R.drawable.icon_friends);

        }else if(postModel.getPrivacy().equalsIgnoreCase("1")) {
            privacy.setImageResource(R.drawable.icon_onlyme);
        }else{
            privacy.setImageResource(R.drawable.icon_public);
        }
    }
}
