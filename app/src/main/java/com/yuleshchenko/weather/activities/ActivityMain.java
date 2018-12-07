package com.yuleshchenko.weather.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.yuleshchenko.weather.R;
import com.yuleshchenko.weather.activities.fragments.FragmentDetails;
import com.yuleshchenko.weather.activities.fragments.FragmentList;
import com.yuleshchenko.weather.services.ServiceDataLoader;
import com.yuleshchenko.weather.settings.FragmentSettings;
import com.yuleshchenko.weather.util.CallbackInterface;
import com.yuleshchenko.weather.util.ForecastRealm;

import io.realm.Realm;
import io.realm.RealmResults;

public class ActivityMain extends AppCompatActivity implements CallbackInterface {

    public static final String MY_LOG = "LESHCHENKO";
    private static final String POSITION_SELECTED = "position";
    private int position = 0; // 没有position被提供 -> 选择第一个
    private FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        addToolbar();  // 设置工具栏
        manager = getSupportFragmentManager();

        // 这里我们用一些逻辑来在多个布局之间提供平滑的导航。
        if (savedInstanceState == null) {
            // 活动第一个开始
            addFragmentList();  // 将天气预报列表放到相应容器中
            // 后台更新数据
            Intent intentUpdate = new Intent(this, ServiceDataLoader.class);
            this.startService(intentUpdate);
        }
        if (savedInstanceState != null) {
            position = savedInstanceState.getInt(POSITION_SELECTED); // 刷新选择的地区
        }
        // 检查是否在多屏
        if (isInLargeLandMode()) {
            addFragmentDetails(position);  // 添加内容到相应的容器中
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从设置中读取城市
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String city = sharedPref.getString(FragmentSettings.PREF_CITY, "Beijing");
        // 在工具栏设置城市
        android.support.v7.app.ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setSubtitle(city.toUpperCase());
        }
    }

    // 添加工具栏
    private void addToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        android.support.v7.app.ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setIcon(R.mipmap.ic_launcher);
            myActionBar.setTitle(R.string.app_name);
        }
    }

    // 在片段中添加天气预报列表
    private void addFragmentList() {
        FragmentList mList = new FragmentList();
        manager.beginTransaction()
                .add(R.id.list_container, mList)
                .commit();
    }

    // 添加天气预报内容片段到相应的容器中
    private boolean addFragmentDetails(int position) {
        FragmentDetails mDetails = new FragmentDetails();
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ForecastRealm> results = realm.where(ForecastRealm.class).findAll();
        if (results.size() > 0) {
            mDetails.setItemContent(position);  // 显示设置的地区
            manager.beginTransaction()
                    .replace(R.id.details_container, mDetails)
                    .commit();  // 添加相应细节
            return true;  // 片段添加
        } else {
            return false; // 片段没有添加
        }
    }

    @Override
    public void showDetails(int position) {
        FragmentDetails newFragmentItem = new FragmentDetails();
        newFragmentItem.setItemContent(position);
        this.position = position; // 保存地区
        // 检查是否在宽屏模式
        if (!isInLargeLandMode()) {
            Intent intent = new Intent(this, ActitityDetails.class);
            intent.putExtra(POSITION_SELECTED, position);
            startActivity(intent);
        } else {
            manager.beginTransaction()
                    .replace(R.id.details_container, newFragmentItem)
                    .commit();  // 添加细节到容器里
        }
    }

    @Override // 这里选择position
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(POSITION_SELECTED, position);
        super.onSaveInstanceState(savedInstanceState);
    }

    // 返回ture如果机器处于Land-mode并且适于大屏
    private boolean isInLargeLandMode() {
        return getResources().getBoolean(R.bool.has_two_panes);
    }

}