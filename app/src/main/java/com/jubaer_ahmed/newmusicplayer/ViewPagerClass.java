package com.jubaer_ahmed.newmusicplayer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ViewPagerClass extends FragmentPagerAdapter {


    public ViewPagerClass(@NonNull FragmentManager fm) {
        super(fm);
    }

    public ViewPagerClass(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @Override
    public Fragment getItem(int position) {

        if (position == 0){

            return new TracksFragment();
        } else if (position == 1) {

            return new PlaylistFragment();
        } else {

            return new AlbumFragment();
        }

    }

    @Override
    public int getCount() {
        return 3;
    }


    @Override
    public CharSequence getPageTitle(int position) {

        if (position == 0){

            return "Tracks";
        } else if (position == 1) {

            return "Playlists";

        } else {

            return "Album";
        }

    }
}
