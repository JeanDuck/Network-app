package com.friendster.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.friendster.R;
import com.friendster.adapter.NotificationAdapter;
import com.friendster.model.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;

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

public class NotificationFragment extends Fragment {
    Context context;
    @BindView(R.id.notification_recy)
    RecyclerView notificationRecy;
    @BindView(R.id.defaultTextView)
    TextView defaultTextView;
    Unbinder unbinder;

    NotificationAdapter notificationAdapter;
    List<NotificationModel> notificationModels=new ArrayList<>();


    @Override
    //当Fragment与Activity发生关联时调用。
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

    }

    @Nullable
    @Override
    //创建该Fragment的视图
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        unbinder= ButterKnife.bind(this,view);
        notificationAdapter=new NotificationAdapter(context,notificationModels);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(context);
        notificationRecy.setLayoutManager(linearLayoutManager);

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        getNotificationInFragment();
    }

    private void getNotificationInFragment() {
        UserInterface userInterface= ApiClient.getApiClient().create(UserInterface.class);
        Map<String,String> params=new HashMap<>();
        params.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        Call<List<NotificationModel>> call=userInterface.getNotification(params);
        call.enqueue(new Callback<List<NotificationModel>>() {
            @Override
            public void onResponse(Call<List<NotificationModel>> call, Response<List<NotificationModel>> response) {
                if(response.body().size()>0){
                    notificationModels.addAll(response.body());
                    notificationRecy.setAdapter(notificationAdapter);

                }else{
                    defaultTextView.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFailure(Call<List<NotificationModel>> call, Throwable t) {
                Toast.makeText(context,"something went wrong!",Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        notificationModels.clear();
        notificationAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}