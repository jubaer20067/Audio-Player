package com.jubaer_ahmed.newmusicplayer;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout; // Import the correct TabLayout

public class MainActivity extends AppCompatActivity {

    TabLayout tab_lay;
    ViewPager view_pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tab_lay = findViewById(R.id.tab_lay);
        view_pager = findViewById(R.id.view_pager);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPagerClass pageAdapter = new ViewPagerClass(getSupportFragmentManager());
        view_pager.setAdapter(pageAdapter);


        tab_lay.setupWithViewPager(view_pager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home_scrn) {
            Toast.makeText(this, "Added to Home Screen", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.add_widget) {
            Toast.makeText(this, "Added Widget", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.resume_ply) {
            Toast.makeText(this, "Resume Play", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.settings) {
            Toast.makeText(this, "Setting!", Toast.LENGTH_SHORT).show();
        }

        return true;
    }
}
