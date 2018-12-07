package com.yuleshchenko.weather.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.yuleshchenko.weather.R;
import com.yuleshchenko.weather.activities.fragments.FragmentDetails;
import com.yuleshchenko.weather.settings.FragmentSettings;

public class ActitityDetails extends AppCompatActivity {

    static final String POSITION_SELECTED = "position";
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // 检查配置更改是否发生，我们现在处于两个窗格模式，然后关闭活动
        // http://developer.android.com/training/multiscreen/adaptui.html#TaskHandleConfigChanges
        if (getResources().getBoolean(R.bool.has_two_panes)) {finish(); return; }
        addToolbar();  // 设置工具栏

        if (savedInstanceState == null) {  // 第一次初始化
            position = getIntent().getExtras().getInt(POSITION_SELECTED);  // 从Intent中获取地区
            addFragmentDetails(position);
        } else {  // 刷新片段
            position = savedInstanceState.getInt(POSITION_SELECTED); // 刷新选择的地区
            FragmentDetails oldFragmentItem =  (FragmentDetails) getSupportFragmentManager().findFragmentByTag("details");
            oldFragmentItem.setItemContent(position);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 设置中读取城市
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String city = sharedPref.getString(FragmentSettings.PREF_CITY, "Beijing");
        // 设置（更新）城市
        android.support.v7.app.ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setTitle(city.toUpperCase());
        }
    }

    // Adds toolbar
    private void addToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_hw06_toolbar_b);
        setSupportActionBar(myToolbar);
        android.support.v7.app.ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setDisplayHomeAsUpEnabled(true);
            myActionBar.setSubtitle(R.string.weather_details);
        }
    }

    // 添加天气预报列表的片段
    private boolean addFragmentDetails(int position) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentDetails newFragmentItem = new FragmentDetails();
        newFragmentItem.setItemContent(position);
        manager.beginTransaction()
                .add(R.id.hw06_fragment_item_b, newFragmentItem, "details")
                .commit();  // 添加片段
        return true;  // always true
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override // 这里选择城市
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(POSITION_SELECTED, position);
        super.onSaveInstanceState(savedInstanceState);
    }
}
