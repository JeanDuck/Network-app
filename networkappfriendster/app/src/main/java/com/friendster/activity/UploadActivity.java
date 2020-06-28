package com.friendster.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.friendster.R;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

public class UploadActivity extends AppCompatActivity {
    //private static final String TAG="";
    private static final String TAG="GOOGLEACTIVITY";

    @BindView(R.id.privacy_spinner)
    Spinner privacySpinner;
    @BindView(R.id.postBtnTxt)
    TextView postBtnTxt;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.dialogAvatar)
    CircleImageView dialogAvatar;
    @BindView(R.id.status_edit)
    EditText statusEdit;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.add_image)
    Button addImage;

    String imageUploadUrl="";
    boolean isImageSelected=false;
    ProgressDialog progressDialog;
    File compressedImageFile=null;
    int privacylevel=0;
    /*privacylevel的数字代表不同的私密程度
    *0->friends
    *1->only me
    *2->public
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upload);
        ButterKnife.bind(this);

        //后退键
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_back_white);
        //设置标题
        getSupportActionBar().setTitle("");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading....");
        //privacylevel=0;
        privacySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                privacylevel=position;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                privacylevel=0;

            }
        });
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.create(UploadActivity.this)
                        .folderMode(true)
                        .single()
                        .start();
            }
        });
        postBtnTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Log.d(TAG,"在点击");
                uploadPost();
            }


        });
    }

    private void uploadPost() {
        Log.d(TAG,"？？？？");
        String status=statusEdit.getText().toString();
        String userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        //status.trim().length()获得一个字符串除去空格之后的长度
        if(status.trim().length()>0||isImageSelected) {
            progressDialog.show();

            //文件上传功能
            MultipartBody.Builder builder=new MultipartBody.Builder();
            //传输类型
            builder.setType(MultipartBody.FORM);

            builder.addFormDataPart("post",status);
            builder.addFormDataPart("postUserId",userId);
            builder.addFormDataPart("privacy",privacylevel+"");

            if(isImageSelected)
            {
                builder.addFormDataPart("isImageSelected","1");
                /*multipart/form-data是上传文件的一种方式，是浏览器用表单上传文件的方式
                * */
                builder.addFormDataPart("file",compressedImageFile.getName(), RequestBody.create(MediaType.parse("multipart/form-data"),compressedImageFile));


            }else{
                builder.addFormDataPart("isImageSelected","0");
            }



            MultipartBody multipartBody=builder.build();

            UserInterface userInterface= ApiClient.getApiClient().create(UserInterface.class);

            Call<Integer> call=userInterface.uploadStatus(multipartBody);
            Log.d(TAG,"call end,will enter into enqueue");

            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {

                    Log.d(TAG," enqueue coming");
                    progressDialog.dismiss();
                    if(response.body()!=null&&response.body()==1){
                        Log.d(TAG,"第一次");

                        Toast.makeText(UploadActivity.this,"Post is Successfull",Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(UploadActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();


                    }
                    else
                    {
                        Log.d(TAG,"第二次");
                        Toast.makeText(UploadActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();

                    }

                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(UploadActivity.this," wrong",Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"第三次");

                }
            });


        }else{
            Toast.makeText(UploadActivity.this,"Please write your post first",Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(ImagePicker.shouldHandle(requestCode,resultCode,data))
        {
            // get a single image only
            Image selectedImage=ImagePicker.getFirstImageOrNull(data);
            /*图片压缩
             * 有时候需要在界面展示一张较大的图片，这时候我们应该想到两点
             1.图片是否能够缓存
              2.图片是否能够压缩
            做到了缓存和压缩，才能尽可能低减少内存的负荷，增强app的流畅度*/

            try{
                compressedImageFile=new Compressor(this)
                        .setQuality(75)
                        .compressToFile(new File(selectedImage.getPath()));
                //图片已被选择
                isImageSelected=true;
                //调用placeHolder() 这个方法，在加载出网络图片之前，Picasso将会展示placeholder的图片。
                Picasso.with(UploadActivity.this).load(new File(selectedImage.getPath())).placeholder(R.drawable.default_image_placeholder);

            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
