package com.friendster.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.friendster.R;
import com.friendster.fragment.FriendsFragment;
import com.friendster.fragment.NewsFeedFragment;
import com.friendster.fragment.NotificationFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import util.BottomNavigationViewHelper;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.search)
    ImageView search;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.framelayout)
    FrameLayout framelayout;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigation;

    NewsFeedFragment newsFeedFragment;
    NotificationFragment notificationFragment;
    FriendsFragment friendsFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //在Android开发中，使用ToolBar控件替代ActionBar控件，
        // 需要在java代码中使用setSupportActionBar()方法
        setSupportActionBar(toolbar);
        ////是否显示标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        bottomNavigation.inflateMenu(R.menu.bottom_navigation_main);
        bottomNavigation.setItemBackgroundResource(R.color.colorPrimary);//设置背景颜色
        bottomNavigation.setItemTextColor(ContextCompat.getColorStateList(bottomNavigation.getContext(), R.color.nav_item_colors));//设置文本点击前后的颜色
        bottomNavigation.setItemIconTintList(ContextCompat.getColorStateList(bottomNavigation.getContext(), R.color.nav_item_colors));//图标
        BottomNavigationViewHelper.removeShiftMode(bottomNavigation);//使用该方法后，点击导航栏图标不会发生移位

        newsFeedFragment = new NewsFeedFragment();
        notificationFragment = new NotificationFragment();
        friendsFragment = new FriendsFragment();

        Bundle bundle=getIntent().getExtras();
        String isFromNotification="false";
        if(bundle!=null){
            //isFromNotification和myfirebasemessagingservice中一样
            isFromNotification=getIntent().getExtras().getString("isFromNotification","false");
            if(isFromNotification.equals("true")){
                //user is coming from the notification,如果有点赞之类的消息提示，那么点击弹出的提示框后，就会跳转到notification界面
                bottomNavigation.getMenu().findItem(R.id.profile_notification).setChecked(true);
                setFragment(notificationFragment);
            }else{
                //其他情况下是展现在home界面的
                setFragment(newsFeedFragment);
            }


        }else{
            setFragment(newsFeedFragment);

        }

        //设置最初的界面显示，是哪一个fragment
        setFragment(newsFeedFragment);
        //根据点击的按钮，跳转到不同的fragment
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.newsfeed_fragment:
                        setFragment(newsFeedFragment);
                        break;
                    case R.id.profile_fragment:
                        //putextra中放的是要传递的数据
                        startActivity(new Intent(MainActivity.this, ProfileActivity.class).putExtra("uid", FirebaseAuth.getInstance().getCurrentUser().getUid()));

                        break;
                    case R.id.profile_friends:
                        setFragment(friendsFragment);
                        break;
                    case R.id.profile_notification:
                        setFragment(notificationFragment);
                        break;
                }

                return true;
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UploadActivity.class));

            }
        });
    }

    public void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();//开启一个事务
        fragmentTransaction.replace(R.id.framelayout, fragment);//使用另一个Fragment替换当前的，实际上就是remove()然后add()的合体~
        fragmentTransaction.commit();//提交一个事务
    }


    @OnClick(R.id.search)
    public void onViewClicked() {
        startActivity(new Intent(MainActivity.this,SearchActivity.class));
    }
}
