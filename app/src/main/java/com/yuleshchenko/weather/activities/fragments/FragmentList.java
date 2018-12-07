package com.yuleshchenko.weather.activities.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.yuleshchenko.weather.R;
import com.yuleshchenko.weather.services.ServiceDataLoader;
import com.yuleshchenko.weather.settings.Settings;
import com.yuleshchenko.weather.util.ForecastRealm;
import com.yuleshchenko.weather.util.RecycAdapter;

import io.realm.Realm;
import io.realm.RealmResults;

public class FragmentList extends Fragment {

    private Realm realm;
    private RecyclerView recyclerView;
    private RecycAdapter adapter;
    private ResponseReceiver responseReceiver;
    private android.support.v4.widget.SwipeRefreshLayout swiperefresh;

    public FragmentList() {
        /* Required empty public constructor */
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);  // Enable Toolbar menu
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(getActivity(), Settings.class);
                startActivity(intent);
                break;
            case R.id.action_refresh:
                swiperefresh.setRefreshing(true);
                updateDB();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false); // Inflate the Fragment
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swiperefresh = (android.support.v4.widget.SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateDB();
                    }
                }
        );
        recyclerView = (RecyclerView) view.findViewById(R.id.listview);
        recyclerView.setHasFixedSize(true);  // this setting should improve performance
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext()); // using a linear layout manager
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Following BroadcastReceiver listens to the Service that loads the data
        IntentFilter mStatusIntentFilter = new IntentFilter(ResponseReceiver.BROADCAST_ACTION);
        responseReceiver = new ResponseReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(responseReceiver, mStatusIntentFilter);

        // Get realm
        realm = Realm.getDefaultInstance();  // get RealmInstance for this thread
        RealmResults<ForecastRealm> results = realm.where(ForecastRealm.class).findAll();

        // Specifying the adapter
        adapter = new RecycAdapter(getContext(), results);  // automatic update - true
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        // 注销接收
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(responseReceiver);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (realm != null) { realm.close(); }
    }

    private void updateDB() {
        // 开始服务来下载新的信息
        Intent intent = new Intent(getActivity(), ServiceDataLoader.class);
        getActivity().startService(intent);
    }

    // BroadcastReceiver listens to ServiceDataLoader broadcast
    public class ResponseReceiver extends BroadcastReceiver {
        public static final String BROADCAST_ACTION = "com.yuleshchenko.weather.util.BROADCAST";
        public static final String STATUS = "com.yuleshchenko.weather.util.STATUS";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(STATUS, false)) {
                adapter.notifyDataSetChanged();
                swiperefresh.setRefreshing(false);
            }
        }
    }

}