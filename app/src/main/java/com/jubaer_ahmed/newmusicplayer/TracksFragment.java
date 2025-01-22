package com.jubaer_ahmed.newmusicplayer;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TracksFragment extends Fragment {

    ListView listViewSong;
    SearchView searchView;
    String[] items;
    customAdapter customAdapter;
    ArrayList<String> mySongPaths;
    ArrayList<String> filteredSongs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tracks, container, false);


        listViewSong = view.findViewById(R.id.listViewSong);
        searchView = view.findViewById(R.id.searchView);


        runtimePermission();

        return view;
    }

    public void runtimePermission() {
        Dexter.withContext(requireContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        displaySongs();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // Handle the permission denial
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    void displaySongs() {
        ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());
        mySongPaths = new ArrayList<>();

        if (mySongs.isEmpty()) {
            items = new String[0];
        } else {
            items = new String[mySongs.size()];
            for (int i = 0; i < mySongs.size(); i++) {
                File songFile = mySongs.get(i);
                mySongPaths.add(songFile.getAbsolutePath());
                items[i] = songFile.getName().replace(".mp3", "").replace(".wav", "");
            }
        }

        filteredSongs = new ArrayList<>(List.of(items));
        customAdapter = new customAdapter();
        listViewSong.setAdapter(customAdapter);

        listViewSong.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String songName = (String) listViewSong.getItemAtPosition(i);
                startActivity(new Intent(getActivity(), PlayerActivity.class)
                        .putExtra("songs", mySongPaths)
                        .putExtra("songname", songName)
                        .putExtra("pos", i));
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                filterSongs(newText);
                return true;
            }
        });
    }


    private void filterSongs(String query) {
        filteredSongs.clear();
        if (query.isEmpty()) {
            filteredSongs.addAll(List.of(items));
        } else {
            for (String song : items) {
                if (song.toLowerCase().contains(query.toLowerCase())) {
                    filteredSongs.add(song);
                }
            }
        }
        customAdapter.notifyDataSetChanged();
    }

    public ArrayList<File> findSong(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File singlefile : files) {
                if (singlefile.isDirectory() && !singlefile.isHidden()) {
                    arrayList.addAll(findSong(singlefile));
                } else {
                    if (singlefile.getName().endsWith(".mp3") || singlefile.getName().endsWith(".wav")) {
                        arrayList.add(singlefile);
                    }
                }
            }
        }
        return arrayList;
    }

    class customAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return filteredSongs.size();
        }

        @Override
        public Object getItem(int i) {
            return filteredSongs.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View myView = getLayoutInflater().inflate(R.layout.list_item_lay, null);
            TextView textSong = myView.findViewById(R.id.textSong);
            textSong.setSelected(true);
            textSong.setText(filteredSongs.get(i));
            return myView;
        }
    }
}
