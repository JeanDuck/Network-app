package com.friendster.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.friendster.R;
import com.friendster.adapter.PostAdapter;
import com.friendster.model.PostModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class ProfileFragment extends Fragment {
    Context context;
    @BindView(R.id.defaultTextView)
    TextView defaultTextView;
    @BindView(R.id.newsfeed)
    RecyclerView newsfeed;
    @BindView(R.id.newsfeedProgressBar)
    ProgressBar newsfeedProgressBar;
    Unbinder unbinder;

    //limit是一次能展现的信息条数
    int limit=2;
    //offset是偏移量
    int offset=0;
    boolean isFromStart=true;
    PostAdapter postAdapter;
    List<PostModel> postModels=new ArrayList<>();

    String uid="0";
    String current_state="0";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        unbinder= ButterKnife.bind(this,view);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(context);
        newsfeed.setLayoutManager(linearLayoutManager);
        postAdapter=new PostAdapter(context,postModels);

        //在profileviewpageradapter中setarguments
        uid=getArguments().getString("uid","0");
        current_state=getArguments().getString("current_state","0");
        newsfeed.setAdapter(postAdapter);

        newsfeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int visibleItemCount=linearLayoutManager.getChildCount();
                int totalItemCount=linearLayoutManager.getItemCount();
                int passVisibleItems=linearLayoutManager.findFirstCompletelyVisibleItemPosition();

                //如果满足括号中的条件，那么说明我们是在recycling bill的最后
                if(passVisibleItems+visibleItemCount>=(totalItemCount)){
                    //在首个位置的标记设置为false
                    //通过以下方式实现infinite loading
                    isFromStart=false;
                    newsfeedProgressBar.setVisibility(View.VISIBLE);
                    offset=offset+limit;
                    loadProfilePost();

                }
            }
        });
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        isFromStart=true;
        offset=0;
        loadProfilePost();
    }

    private void loadProfilePost() {
        UserInterface userInterface= ApiClient.getApiClient().create(UserInterface.class);
        Map<String,String> params=new HashMap<String, String>();

        params.put("uid",uid);
        params.put("limit",limit+"");
        params.put("offset",offset+"");
        params.put("current_state",current_state);

        Call<List<PostModel>> postModelCall=userInterface.getProfilePosts(params);
        postModelCall.enqueue(new Callback<List<PostModel>>() {
            @Override
            public void onResponse(Call<List<PostModel>> call, Response<List<PostModel>> response) {
                newsfeedProgressBar.setVisibility(View.GONE);
                if(response.body()!=null){
                    postModels.addAll(response.body());
                    if(isFromStart){
                        newsfeed.setAdapter(postAdapter);
                    }else{
                        /*notifyItemRangeInserted（int startPosition,int itemcount）
                          startPosition:起始位置，插入数据的位置。
                        itemcount:数据个数。*/
                        postAdapter.notifyItemRangeInserted(postModels.size(),response.body().size());

                    }
                }
            }

            @Override
            public void onFailure(Call<List<PostModel>> call, Throwable t) {
                newsfeedProgressBar.setVisibility(View.GONE);
                Toast.makeText(context,"something went wrong!",Toast.LENGTH_SHORT).show();


            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        postModels.clear();
        postAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
