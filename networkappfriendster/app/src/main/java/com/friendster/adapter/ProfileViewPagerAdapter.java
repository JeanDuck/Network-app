package com.friendster.adapter;

import android.os.Bundle;

import com.friendster.fragment.ProfileFragment;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ProfileViewPagerAdapter extends FragmentPagerAdapter {
    int size=0;
    String uid="0";
    String current_state="0";

    public ProfileViewPagerAdapter(FragmentManager fm,int size,String uid,String current_state) {
        super(fm);
        this.size=size;
        this.uid=uid;
        this.current_state=current_state;
    }

    @Override
    /**
     * 获取给定位置对应的Fragment。
     *
     * @param position 给定的位置
     * @return 对应的Fragment
     */
    public Fragment getItem(int position) {
        switch(position)
        {
            case 0:
                ProfileFragment profileFragment=new ProfileFragment();
                Bundle bundle=new Bundle();
                bundle.putString("uid",uid);
                bundle.putString("current_state",current_state);
                profileFragment.setArguments(bundle);
                return profileFragment;
                //到位置0时，返回一个新的profilefragment页面
               // return new ProfileFragment();
            default:
                return null;

        }

    }

    @Override
    public int getCount() {
        return size;
    }

    @Nullable
    @Override
    //每个页面的标题是通过适配器的getPageTitle(int)函数提供给ViewPager的。
    public CharSequence getPageTitle(int position) {
        switch(position)
        {
            case 0:
                return "Posts";
            default:
                return null;

        }
    }
}
