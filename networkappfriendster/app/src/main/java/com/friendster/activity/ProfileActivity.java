package com.friendster.activity;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.friendster.R;
import com.friendster.adapter.ProfileViewPagerAdapter;
import com.friendster.model.User;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rest.ApiClient;
import rest.services.UserInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {

    @BindView(R.id.profile_cover)
    ImageView profileCover;
    @BindView(R.id.profile_image)
    CircleImageView profileImage;
    @BindView(R.id.profile_option_btn)
    Button profileOptionBtn;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.ViewPager_profile)
    ViewPager ViewPagerProfile;

    ProfileViewPagerAdapter profileViewPagerAdapter;
    /*
     * 0=profile is still loading
     * 1=两个人是朋友
     * 2=this people has sent friend request to another friend（cancel sent request）
     * 3=this people has received friend request from another friend（reject or accept request）
     * 4=people are unknown（you can send request）
     * 5=own profile
     * */
    int current_state = 0;
    String profileUrl = "", coverUrl = "";

    ProgressDialog progressDialog;
    int imageUploadType = 0;


    String uid = "0";
    File compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //for hiding status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_profile);
        //获取putextra中的信息
        uid = getIntent().getStringExtra("uid");

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("loading....");
        progressDialog.show();
        ButterKnife.bind(this);



        //返回按钮
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_back_white);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));

            }
        });

        //用适配器把页面布局加载进来


        //equalsIgnoreCase是忽略了大小写的比较
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equalsIgnoreCase(uid)) {
            //uid is matched,we are going to load our own profile
            current_state = 5;
            profileOptionBtn.setText("Edit Profile");
            loadProfile();
        } else {
            otherOthersProfile();
            //load others profile here

        }
        profileOptionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //为了作后面的发送好友请求的功能，在这里先设置这个按钮不可点击
                profileOptionBtn.setEnabled(false);
                if (current_state == 5) {
                    CharSequence options[] = new CharSequence[]{"Change Cover Profile", "Change Profile picture", "View cover Picture", "View Profile Picture"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setOnDismissListener(ProfileActivity.this);
                    builder.setTitle("Choose Options");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            //position位置代表了不同按键
                            if (position == 0) {
                                imageUploadType = 1;
                                ImagePicker.create(ProfileActivity.this)
                                        .folderMode(true)
                                        .single()
                                        .toolbarFolderTitle("choose a folder")
                                        .toolbarImageTitle("Select a Image")
                                        .start();
                                //change cover part
                            } else if (position == 1) {
                                imageUploadType = 0;
                                ImagePicker.create(ProfileActivity.this)
                                        .folderMode(true)
                                        .single()
                                        .toolbarFolderTitle("choose a folder")
                                        .toolbarImageTitle("Select a Image")
                                        .start();
                                //change profile part
                            } else if (position == 2) {
                                //coverUrl是cover处图片的url，将这个地址和对应的view传入自定义函数中，实现场景跳转
                                viewFullImage(profileCover,coverUrl);

                                //view cover profile

                            } else {
                                viewFullImage(profileImage,profileUrl);
                                //view profile picture
                            }

                        }
                    });
                    builder.show();

                }else if(current_state==4){
                    profileOptionBtn.setText("Processing...");
                    CharSequence options[] = new CharSequence[]{"Send Friend Request"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setOnDismissListener(ProfileActivity.this);
                    builder.setTitle("Choose Options");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            //position位置代表了不同按键
                            if (position == 0) {
                                performAction(current_state);

                            }

                        }
                    });
                    builder.show();
                }else if(current_state==2){
                    profileOptionBtn.setText("Processing...");
                    CharSequence options[] = new CharSequence[]{"Cancel Friend Request"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setOnDismissListener(ProfileActivity.this);
                    builder.setTitle("Choose Options");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            //position位置代表了不同按键
                            if (position == 0) {
                                performAction(current_state);

                            }

                        }
                    });
                    builder.show();

                }else if(current_state==3){
                    profileOptionBtn.setText("Processing...");
                    CharSequence options[] = new CharSequence[]{"Accept Friend Request"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setOnDismissListener(ProfileActivity.this);
                    builder.setTitle("Choose Options");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            //position位置代表了不同按键
                            if (position == 0) {
                                performAction(current_state);

                            }

                        }
                    });
                    builder.show();

                }else if(current_state==1)
                {
                    profileOptionBtn.setText("Processing...");
                    //点击按钮以后弹出的小框框内容
                    CharSequence options[] = new CharSequence[]{"UnFriend this User"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setOnDismissListener(ProfileActivity.this);
                    builder.setTitle("Choose Options");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            //position位置代表了不同按键
                            if (position == 0) {
                                performAction(current_state);

                            }

                        }
                    });
                    builder.show();

                }
            }
        });


    }

    private void performAction(int i) {
        UserInterface userInterface=ApiClient.getApiClient().create(UserInterface.class);
        Call<Integer> call=userInterface.performAction(new PerformAction(i+"",FirebaseAuth.getInstance().getCurrentUser().getUid(),uid));
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                profileOptionBtn.setEnabled(true);
                if(response.body()==1){
                    if(i==4)
                    {
                        current_state=2;
                        profileOptionBtn.setText("Request Sent");
                        Toast.makeText(ProfileActivity.this,"Request Sent Successfully",Toast.LENGTH_SHORT).show();

                    }else if(i==2){
                        //当一个人发送好友请求后打算取消好友请求时，那么状态就变回4，两个人互不相识的状态
                        current_state=4;
                        profileOptionBtn.setText("Send Request");
                        Toast.makeText(ProfileActivity.this,"Request cancelled Successfully",Toast.LENGTH_SHORT).show();
                    }else if(i==3){
                        current_state=1;
                        profileOptionBtn.setText("Friends");
                        Toast.makeText(ProfileActivity.this,"you are friends now!",Toast.LENGTH_SHORT).show();
                    }else if(i==1){
                        current_state=4;
                        profileOptionBtn.setText("Send Request");
                        Toast.makeText(ProfileActivity.this,"you are no more friends",Toast.LENGTH_SHORT).show();


                    }

                }else{
                    profileOptionBtn.setEnabled(false);
                    profileOptionBtn.setText("Error...");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {

            }
        });

    }

    private void otherOthersProfile() {
        UserInterface userInterface=ApiClient.getApiClient().create(UserInterface.class);
        Map<String,String> params=new HashMap<String, String>();
        params.put("userId",FirebaseAuth.getInstance().getCurrentUser().getUid());
        params.put("profileId",uid);

        Call<User> call=userInterface.loadOtherProfile(params);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                progressDialog.dismiss();
                if(response.body()!=null)
                {
                    //显示了他人的界面按钮text文本
                     showUserData(response.body());

                     if(response.body().getState().equalsIgnoreCase("1")){
                         profileOptionBtn.setText("Friends");
                         current_state=1;
                     }else if(response.body().getState().equalsIgnoreCase("2")){
                         profileOptionBtn.setText("Cancel Request");
                         current_state=2;
                     }else if(response.body().getState().equalsIgnoreCase("3")){
                         profileOptionBtn.setText("Accept Request");
                         current_state=3;
                     }else if(response.body().getState().equalsIgnoreCase("4")){
                         profileOptionBtn.setText("Send Request");
                         current_state=4;
                     }
                     else{
                         profileOptionBtn.setText("Error");
                         current_state=0;
                     }

                     current_state=4;
                     profileOptionBtn.setText("Send Request");

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this,"Something went wrong...please try later",Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void showUserData(User user) {

        profileViewPagerAdapter = new ProfileViewPagerAdapter(getSupportFragmentManager(), 1,user.getUid(),user.getState());
        ViewPagerProfile.setAdapter(profileViewPagerAdapter);

        profileUrl = user.getProfileUrl();
        coverUrl = user.getCoverUrl();
        collapsingToolbar.setTitle(user.getName());

        if (!profileUrl.isEmpty()) {
            Picasso.with(ProfileActivity.this).load(profileUrl).networkPolicy(NetworkPolicy.OFFLINE).into(profileImage, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ProfileActivity.this).load(profileUrl).into(profileImage);

                }
            });

        }
        if (!coverUrl.isEmpty()) {
            Picasso.with(ProfileActivity.this).load(coverUrl).networkPolicy(NetworkPolicy.OFFLINE).into(profileCover, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ProfileActivity.this).load(coverUrl).into(profileCover);

                }
            });

        }
        addImageCoverClick();


    }

    private void viewFullImage(View view, String link) {
        /*
        * FullImageActivity中通过getStringExtra("imageUrl")，获得image的url
        *
        * */
        Intent intent=new Intent(ProfileActivity.this,FullImageActivity.class);

        //putExtra("A",B)中，AB为键值对，第一个参数为键名，第二个参数为键对应的值
        intent.putExtra("imageUrl",link);

        /*Build.VERSION.SDK_INT常量代表了Android设备的版本号
         *Build.VERSION_CODES.LOLLIPOP,指API 21级
         * 使用以下版本可以使高版本实现更好的功能，低版本也可以安全运行
         */
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
        {
            /*
            * Pair是一个容器，作用是轻便地对两个对象组成的元素组进行传递。
            这个对象提供了一个合理的equals()方法，
            如果两个对象的first和second值相等则返回true
            *
            * 使用transitionName来标记共有view(目标activity的xml也需要)
            * 使用ActivityOptions添加共有view，实现跳转
            *
            *当有多个共有元素时，可以用以下方式：
            * 多个共有元素
			Pair[] pairs = new Pair[2];
			pairs[0] = Pair.create(mViewContent, "text");
			pairs[1] = Pair.create(mViewImage, "image");
			* 本次用到的共有元素明明为shared
            * */

            Pair[] pairs=new Pair[1];
            //shared和xml文件中的transitionname对应
            pairs[0]=new Pair<View,String>(view,"shared");
            ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation(ProfileActivity.this,pairs);
            startActivity(intent,options.toBundle());

        }else
        {
            startActivity(intent);
        }

    }

    private void loadProfile() {
        UserInterface userInterface = ApiClient.getApiClient().create(UserInterface.class);
        Map<String, String> params = new HashMap<>();
        params.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        Call<User> call = userInterface.loadownProfile(params);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                   showUserData(response.body());
                } else {
                    Toast.makeText(ProfileActivity.this, "Something went wrong...Please try later", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Something went wrong...Please try later", Toast.LENGTH_SHORT).show();

            }
        });


    }
    /*
    * 点击图片后实现图片查看功能
    * */

    private void addImageCoverClick() {
        profileCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFullImage(profileCover,coverUrl);
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFullImage(profileImage,profileUrl);
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        profileOptionBtn.setEnabled(true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // get a single image only
            Image selectedImage = ImagePicker.getFirstImageOrNull(data);
            /*图片压缩
             * 有时候需要在界面展示一张较大的图片，这时候我们应该想到两点
             1.图片是否能够缓存
              2.图片是否能够压缩
            做到了缓存和压缩，才能尽可能低减少内存的负荷，增强app的流畅度*/

            try {
                compressedImageFile = new Compressor(this)
                        .setQuality(75)
                        .compressToFile(new File(selectedImage.getPath()));

                uploadFile(compressedImageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadFile(final File compressedImageFile) {

        progressDialog.setTitle("Loading...");
        progressDialog.show();

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("postUserId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        builder.addFormDataPart("imageUploadType", imageUploadType + "");
        builder.addFormDataPart("file",compressedImageFile.getName(), RequestBody.create(MediaType.parse("multipart/form-data"),compressedImageFile));


        MultipartBody multipartBody = builder.build();

        UserInterface userInterface = ApiClient.getApiClient().create(UserInterface.class);
        Call<Integer> call = userInterface.uploadImage(multipartBody);

        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {

                // Log.d(TAG," enqueue coming");
                progressDialog.dismiss();
                if (response.body() != null && response.body() == 1) {
                    // Log.d(TAG,"第一次");
                    if(imageUploadType==0){
                       Picasso.with(ProfileActivity.this).load(compressedImageFile).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image_placeholder).into(profileImage,new com.squareup.picasso.Callback(){
                           @Override
                           public void onSuccess() {

                           }

                           @Override
                           public void onError() {
                               Picasso.with(ProfileActivity.this).load(compressedImageFile).placeholder(R.drawable.default_image_placeholder).into(profileImage);
                           }
                       });

                       Toast.makeText(ProfileActivity.this,"Profile Picture Changed Successfully",Toast.LENGTH_LONG).show();


                    }
                    else{
                        Picasso.with(ProfileActivity.this).load(compressedImageFile).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image_placeholder).into(profileImage,new com.squareup.picasso.Callback(){
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(ProfileActivity.this).load(compressedImageFile).placeholder(R.drawable.default_image_placeholder).into(profileImage);
                            }
                        });

                        Toast.makeText(ProfileActivity.this,"Cover Picture Changed Successfully",Toast.LENGTH_LONG).show();




                    }

                    Toast.makeText(ProfileActivity.this, "Post is Successfull", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();


                } else {
                    //Log.d(TAG,"第二次");
                    Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                }


            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, " wrong", Toast.LENGTH_SHORT).show();
            }

        });
    }
    public static class PerformAction{
        String operationType,userId,profileid;

        public PerformAction(String operationType,String userId,String profileid) {
            this.operationType = operationType;
            this.userId=userId;
            this.profileid=profileid;
        }
    }
}


