package com.friendster.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.friendster.R;
import com.friendster.adapter.SearchAdapter;
import com.friendster.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import rest.ApiClient;
import rest.services.UserInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search_recy)
    RecyclerView searchRecy;
    SearchAdapter searchAdapter;
    List<User> users=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_back_white);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this,MainActivity.class));
            }
        });

        searchAdapter=new SearchAdapter(SearchActivity.this,users);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(SearchActivity.this);
        searchRecy.setLayoutManager(layoutManager);
        searchRecy.setAdapter(searchAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*
        在Activity类中有一个来getMenuInflater()的函数用来返回源这个Activity的MenuInflater，
        并通过MenuInflater对象知来设置menu XML里的menu作为该Activity的菜单道
        * */
        getMenuInflater().inflate(R.menu.search_view,menu);
        SearchView searchView=(SearchView) menu.findItem(R.id.search).getActionView();
        //设置搜索框直接展开显示。左侧有放大镜(在搜索框中) 右侧有叉叉 可以关闭搜索框
        searchView.setIconified(false);
        ((EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text)).setTextColor(getResources().getColor(R.color.hint_color));
        ((EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text)).setHintTextColor(getResources().getColor(R.color.hint_color));
        ((ImageView) searchView.findViewById(androidx.appcompat.R.id.search_close_btn)).setImageResource(R.drawable.icon_clear);
        searchView.setQueryHint("search people");
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFromDb(query,true);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if(query.length()>2){
                    searchFromDb(query,false);

                }else{
                    users.clear();
                    /*
                    * notifyDataSetChanged方法通过一个外部的方法控制
                    * 如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容,
                    * 可以实现动态的刷新列表的功能。*/
                    searchAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });
        return true;

    }

    private void searchFromDb(String query, boolean b) {
        UserInterface userInterface= ApiClient.getApiClient().create(UserInterface.class);
        Map<String,String> params=new HashMap<String, String>();
        params.put("keyword",query);

        Call<List<User>> call=userInterface.search(params);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                //响应时，先将原有的数据清空
               users.clear();
               //将新的数据添加到list中
               users.addAll(response.body());
               //刷新列表
               searchAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {

            }
        });
    }
}
