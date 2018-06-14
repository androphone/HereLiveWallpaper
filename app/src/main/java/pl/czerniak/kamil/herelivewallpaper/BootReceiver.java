package pl.czerniak.kamil.herelivewallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

//
//
//Just launch the service at boot if necessary (if you know you have engines)
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean sharedPrefCheck = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(MainActivity.isStartedKey, MainActivity.isStartedDefault);
        boolean actionCheck = intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                || intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED);
        Log.d(this.getClass().getSimpleName(), "BootReceiver check: " + sharedPrefCheck);
        if (sharedPrefCheck && actionCheck) {
            context.startService(new Intent(context, MapWallpaperService.class));
        }
    }
}
