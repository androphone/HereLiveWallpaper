package pl.czerniak.kamil.herelivewallpaper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Utilities from project HereLiveWallpaper.
 * Created by kamil on 21/05/2017.
 */

class Utilities {

    static int checkPlayServices(Context context) {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        switch (status) {
            case ConnectionResult.SUCCESS:
                return status;
            default:
                if (context.getClass() == MapWallpaperService.class) {
                    GoogleApiAvailability.getInstance().showErrorNotification(context, status);
                } else {
                    GoogleApiAvailability.getInstance().showErrorDialogFragment((Activity) context, status, 0);
                }
                return status;
        }
    }

    static boolean checkPermissions(Context context, int mode) {

        //Modes:
        //0: activity is passed, can show permission check
        //1: service is passed, launch activity

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Here Live Wallpaper requires location permission to function properly.", Toast.LENGTH_SHORT).show();
            if (mode == 0) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
            return false;
        } else {
            return true;
        }
    }
}

