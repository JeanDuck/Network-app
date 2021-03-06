package com.friendster.fragment.bottomsheets;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.friendster.R;
import com.friendster.adapter.CommentAdapter;
import com.friendster.model.CommentModel;
import com.friendster.model.PostModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rest.ApiClient;
import rest.services.UserInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentBottomSheet extends BottomSheetDialogFragment {
    Context context;
    @BindView(R.id.comments_txt)
    TextView commentsTxt;
    @BindView(R.id.top_section)
    LinearLayout topSection;
    @BindView(R.id.comment_recy)
    RecyclerView commentRecy;
    @BindView(R.id.comment_edittext)
    EditText commentEdittext;
    @BindView(R.id.comment_send)
    ImageView commentSend;
    @BindView(R.id.comment_send_wrapper)
    RelativeLayout commentSendWrapper;
    @BindView(R.id.comment_top_wrapper)
    LinearLayout commentTopWrapper;
    Unbinder unbinder;

    boolean isFlagZero=true;
    PostModel postModel;

    CommentAdapter commentAdapter;
    List<CommentModel.Result> results=new ArrayList<>();
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
    // @SuppressLint("RestrictApi")

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        /*
        * 在其setupDialog方法中我们就可以自由定制动作条的视图及行为了,
        * 同时也需要监听动作条的状态以实现在hidden状态下的自动dismiss
        *
        * */
        super.setupDialog(dialog, style);

        View view = View.inflate(context, R.layout.bottom_sheet_layout, null);
        postModel= Parcels.unwrap(getFragmentManager().findFragmentByTag("commentFragment").getArguments().getParcelable("postModel"));
        unbinder= ButterKnife.bind(this,view);
        //设置动作条视图
        dialog.setContentView(view);
        View view1=(View) view.getParent();
        //设置控件遮罩为透明
        view1.setBackgroundColor(Color.TRANSPARENT);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog dialog1=(BottomSheetDialog) dialog;
                FrameLayout bottomsheet=dialog1.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                //设置控件为展开状态
                BottomSheetBehavior.from(bottomsheet).setState(BottomSheetBehavior.STATE_EXPANDED);


            }
        });
        commentAdapter=new CommentAdapter(context,results,postModel);

        if(postModel.getCommentCount().equals("0")||postModel.getCommentCount().equals("1")){
            commentsTxt.setText(postModel.getCommentCount()+"comment");
        }else{
            commentsTxt.setText(postModel.getCommentCount()+"comments");

        }

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(context);
        commentRecy.setLayoutManager(linearLayoutManager);

        retriveComments();
        commentEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Drawable img1=getResources().getDrawable(R.drawable.icon_before_comment_send);
                Drawable img2=getResources().getDrawable(R.drawable.icon_after_comment_send);

                if(charSequence.toString().trim().length()==0){
                    isFlagZero=true;
                    commentSendWrapper.setBackgroundResource(R.drawable.icon_background_before_comment);
                    loadImageWithAnimation(context,img1);
                }else if(charSequence.toString().trim().length()!=0){
                    isFlagZero=false;
                    commentSendWrapper.setBackgroundResource(R.drawable.icon_background_after_comment);
                    loadImageWithAnimation(context,img2);

                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        commentSendWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFlagZero){
                    //当评论内容为空时，我们不返回什么
                    return;
                }
                final String comment=commentEdittext.getText().toString().trim();
                commentEdittext.setText("");
                //隐藏键盘
                ((InputMethodManager)getContext().getSystemService(getContext().INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0);
                UserInterface userInterface= ApiClient.getApiClient().create(UserInterface.class);
                AddComment addComment=new AddComment(comment, FirebaseAuth.getInstance().getCurrentUser().getUid(),"0",postModel.getPostId(),"0","0",postModel.getPostUserId(),"");
                Call<CommentModel> call=userInterface.postComment(addComment);
                call.enqueue(new Callback<CommentModel>() {
                    @Override
                    public void onResponse(Call<CommentModel> call, Response<CommentModel> response) {
                        if(response.body().getResult().size()>0){
                            Toast.makeText(context,"Comment successful!",Toast.LENGTH_SHORT).show();
                            int commentCount=Integer.parseInt(postModel.getCommentCount());
                            commentsTxt.setText(commentCount+"Comments");

                            //发送完评论后，我们希望我们的界面也能显示出评论，get(0)是因为我们只retrive one commment from our list of comment
                            results.add(response.body().getResult().get(0));
                            int position=results.indexOf(response.body().getResult().get(0));
                            //适配器发现插入信息的变化，并改变
                            commentAdapter.notifyItemInserted(position);
                            //定位到指定项
                            commentRecy.scrollToPosition(position);


                        }else{
                            Toast.makeText(context,"something went wrong!",Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(Call<CommentModel> call, Throwable t) {
                        Toast.makeText(context,"something went wrong!",Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });

    }

    private void retriveComments() {
        UserInterface userInterface=ApiClient.getApiClient().create(UserInterface.class);
        Map<String,String> params=new HashMap<String, String>();
        params.put("postId",postModel.getPostId());
        Call<CommentModel> call=userInterface.retriveTopComments(params);
        call.enqueue(new Callback<CommentModel>() {
            @Override
            public void onResponse(Call<CommentModel> call, Response<CommentModel> response) {
                if(response.body().getResult().size()>0){
                    results.addAll(response.body().getResult());
                    commentRecy.setAdapter(commentAdapter);


                }else{
                    Toast.makeText(context,"No Comments Found!",Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onFailure(Call<CommentModel> call, Throwable t) {
                Toast.makeText(context,"something went wrong!",Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void loadImageWithAnimation(Context c,final Drawable img1){
        final Animation anim_out= AnimationUtils.loadAnimation(c,R.anim.zoom_out);
        final Animation anim_in= AnimationUtils.loadAnimation(c,R.anim.zoom_in);

        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //动画结束时，将img下载到commentsend中
                commentSend.setImageDrawable(img1);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                commentSend.startAnimation(anim_in);

            }
        });
        commentSend.startAnimation(anim_out);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
    public static class AddComment{
       String comment;
       String commentBy;
       String superParentId;
       String parentId;
       String hasSubComment;
       String level;
       String postUserId;
       String commentUserId;

        public AddComment(String comment, String commentBy, String superParentId, String parentId, String hasSubComment, String level, String postUserId, String commentUserId) {
            this.comment = comment;
            this.commentBy = commentBy;
            this.superParentId = superParentId;
            this.parentId = parentId;
            this.hasSubComment = hasSubComment;
            this.level = level;
            this.postUserId = postUserId;
            this.commentUserId = commentUserId;
        }
    }
}
