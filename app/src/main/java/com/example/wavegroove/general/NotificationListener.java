package com.example.wavegroove.general;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;


public class NotificationListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AudioForegroundService service = ((MainActivity) context).getService();
        switch (Objects.requireNonNull(intent.getAction())) {
            case AudioForegroundService.BUTTON_REWIND:
                service.fastRewindPressed();
                break;
            case AudioForegroundService.BUTTON_PLAYPAUSE:
                service.playPausePressed();
                break;
            case AudioForegroundService.BUTTON_FORWARD:
                service.fastForwardPressed();
                break;
        }
    }
}
