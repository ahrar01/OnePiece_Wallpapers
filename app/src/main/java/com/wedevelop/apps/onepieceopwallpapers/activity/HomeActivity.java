package com.wedevelop.apps.onepieceopwallpapers.activity;


import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.wedevelop.apps.onepieceopwallpapers.HintServiceImpl;
import com.wedevelop.apps.onepieceopwallpapers.R;
import com.wedevelop.apps.onepieceopwallpapers.adapter.TabLayoutAdapter;
import com.wedevelop.apps.onepieceopwallpapers.models.Hint;

public class HomeActivity extends AppCompatActivity {
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    TabLayoutAdapter adapter;
    private TabLayout tabLayout;
    private int[] tabIcons = {
            R.drawable.ic_tab_new,
            R.drawable.ic_random,
            R.drawable.ic_favorite
    };
    boolean doubleTap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        ViewPager viewPager = findViewById(R.id.viewpager);
        prefs = getSharedPreferences("SharedPreference", 0); // 0 - for private mode
        editor = prefs.edit();
        if (prefs.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            editor.putBoolean("firstrun", false).apply();
            setTabsandShowHints();
        }
        // setting up the adapter, adapter tells which fragment to load
        adapter = new TabLayoutAdapter(getSupportFragmentManager());// call of consturctor
        setupViewPager(viewPager);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tabIconColor = ContextCompat.getColor(getBaseContext(), R.color.selectedTabColor); // change color of selected tab
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int tabIconColor = ContextCompat.getColor(getBaseContext(), R.color.tabUnselectedColor);
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }
    private void setupViewPager(ViewPager viewPager) {
        adapter = new TabLayoutAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
    }

    public void onBackPressed() {
        if (doubleTap) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Please Back again to Exit", Toast.LENGTH_SHORT).show();
            doubleTap = true;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleTap = false;
                }
            }, 500);
        }
    }




    public void showHint(View view,String title,String desc) {
        HintServiceImpl hintService = new HintServiceImpl();
        hintService.addHint(new Hint(view, title,desc));
            hintService.showHint(this);
    }

    public void setTabsandShowHints(){
        tabLayout.getTabAt(0).select();

    }

}
