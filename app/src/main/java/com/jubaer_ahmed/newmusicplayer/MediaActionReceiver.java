package com.jubaer_ahmed.newmusicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MediaActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case "ACTION_PLAY_PAUSE":

                    if (PlayerActivity.mediaPlayer.isPlaying()) {
                        PlayerActivity.mediaPlayer.pause();
                        PlayerActivity.updateNotification(false);
                    } else {
                        PlayerActivity.mediaPlayer.start();
                        PlayerActivity.updateNotification(true);
                    }
                    break;
                case "ACTION_NEXT":
                    PlayerActivity.staticNextSong();
                    break;
                case "ACTION_PREVIOUS":
                    PlayerActivity.staticPreviousSong();
                    break;
            }
        }
    }
}
