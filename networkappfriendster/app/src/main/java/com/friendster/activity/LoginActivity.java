package com.friendster.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.friendster.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import rest.ApiClient;
import rest.services.UserInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG="GOOGLEACTIVITY";
    private static final int RC_SIGN_IN=9001;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private SignInButton signInButton;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInButton=findViewById(R.id.sign_in_button);

        // Configure Google Sign In
        //初始化gso，server_client_id为添加的客户端id
        //按照将 Google 登录机制集成到您的 Android 应用页面上的步骤，
        // 将 Google 登录机制集成到您的应用中。 配置 GoogleSignInOptions 对象时，请调用 requestIdToken：
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //初始化Google登录实例,activity为当前activity
        // 在当前活动获得一个登录的访客
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso);

        mFirebaseAuth=FirebaseAuth.getInstance();//获取 FirebaseAuth 对象的共享实例
        // FirebaseAuth是Firebase身份认证的入口，
        // 就是由它来完成身份认证的各种功能 。
        // FirebaseAuth使用了单例模式，
        // 应用的FirebaseAuth对象通过FirebaseAuth.getInstance()获得

        progressDialog=new ProgressDialog(LoginActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Signing you in...Please wait...");
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignIn();
            }
        });
    }
    @Override
    protected void onStart(){
        //初始化您的 Activity 时，请检查用户当前是否已登录
        super.onStart();
        mFirebaseUser=mFirebaseAuth.getCurrentUser();//若没登录，就获取当前用户


        if(mFirebaseUser!=null)
        {
            Log.d(TAG,"USER IS ALREADY LOGGED IN");
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            finish();//如果firebase上之前已经登陆过，那么就直接转到登陆界面中
        }




    }
             private void SignIn(){
                 //如果未授权则可以调用登录，mGoogleSignInClient为初始化好的Google登录实例，
                 // RC_SIGN_IN为随意唯一返回标识码，int即可。
                 Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                 startActivityForResult(signInIntent, RC_SIGN_IN);//启动意图提示用户选择谷歌账号登陆


         }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            progressDialog.show();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                Log.w(TAG, "fist test");
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.w(TAG, "second test");
                firebaseAuthWithGoogle(account);//见下面的类
                Log.w(TAG, "third test");

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //用户成功登录之后，从 GoogleSignInAccount 对象中获取一个 ID 令牌，
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // 用其换取 Firebase 凭据，然后使用此 Firebase 凭据进行 Firebase 身份验证
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            final FirebaseUser user = mFirebaseAuth.getCurrentUser();


                            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this, new OnSuccessListener<InstanceIdResult>() {
                                @Override
                                public void onSuccess(InstanceIdResult instanceIdResult) {
                                    //获取用户的信息
                                    String userToken=instanceIdResult.getToken();
                                    String uid=user.getUid();
                                    String name=user.getDisplayName();
                                    String email=user.getEmail();
                                    String profileUrl=user.getPhotoUrl().toString();
                                    final String coverUrl="";
                                    ////用retrofit加工出对应的接口实例对象
                                    UserInterface userInterface= ApiClient.getApiClient().create(UserInterface.class);
                                    //调用接口函数，获得网络工作对象，userinfo中是要传输的参数
                                    Call<Integer> call=userInterface.singin(new LoginActivity.UserInfo(uid,name,email,profileUrl,coverUrl,userToken));
                                    //用上一步获取的call对象，执行网络请求，把这些信息post出去
                                    Log.d(TAG, "forth test");

                                    call.enqueue(new Callback<Integer>() {
                                        @Override

                                        public void onResponse(Call<Integer> call, Response<Integer> response) {
                                            Log.d(TAG, "fifth test");
                                            progressDialog.dismiss();
                                            if(response.body()==1)
                                            {
                                                Toast.makeText(LoginActivity.this,"Login successful",Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                                finish();

                                            }
                                            else
                                            {
                                                Toast.makeText(LoginActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                                                FirebaseAuth.getInstance().signOut();

                                            }

                                        }

                                        @Override
                                        public void onFailure(Call<Integer> call, Throwable t) {
                                            FirebaseAuth.getInstance().signOut();
                                            progressDialog.dismiss();
                                            Toast.makeText(LoginActivity.this,"Login failed",Toast.LENGTH_SHORT).show();

                                        }
                                    });

                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                        }

                    }
                });
    }

    public class UserInfo{
        String uid,name,email,profileUrl,coverUrl,userToken;

        public UserInfo(String uid, String name, String email, String profileUrl, String coverUrl, String userToken) {
            this.uid = uid;
            this.name = name;
            this.email = email;
            this.profileUrl = profileUrl;
            this.coverUrl = coverUrl;
            this.userToken = userToken;
        }
    }
}
