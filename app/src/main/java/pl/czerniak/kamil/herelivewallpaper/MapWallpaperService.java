package pl.czerniak.kamil.herelivewallpaper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.crash.FirebaseCrash;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.services.api.ServicesException;
import com.mapbox.services.api.staticimage.v1.MapboxStaticImage;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

//TODO: Keep engine from disappearing after update


//Wallpaper service, used (possibly) only for handling the wallpaper itself
public class MapWallpaperService extends android.service.wallpaper.WallpaperService
        implements LocationListener, GoogleApiClient.ConnectionCallbacks, SharedPreferences.OnSharedPreferenceChangeListener {
    private GoogleApiClient mClient;
    private Location mLocation = null;
    private SharedPreferences mShared;
    private PreferenceSet mPreferences;
    private final List<MapEngine> mEngines = new ArrayList<>();
    private Bitmap mLoaded;
    String url = null;

    //Used for returning engine to handle the wallpaper
    @Override
    public MapEngine onCreateEngine() {
        MapEngine mEngine = new MapEngine();
        mEngines.add(mEngine);
        return mEngine;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mShared = PreferenceManager.getDefaultSharedPreferences(this);
        mShared.edit().putBoolean(MainActivity.isStartedKey, true).apply();
        mShared.registerOnSharedPreferenceChangeListener(this);
        mPreferences = new PreferenceSet(mShared.getInt(MainActivity.zoomKey, MainActivity.zoomDefault),
                mShared.getInt(MainActivity.typeKey, MainActivity.typeDefault),
                mShared.getInt(MainActivity.refreshKey, MainActivity.refreshDefault),
                mShared.getFloat(MainActivity.displacementKey, MainActivity.displacementDefault));

        mClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        mClient.connect();
    }


    @Override
    public void onDestroy() {
        mClient.disconnect();
        Log.d(getClass().getSimpleName(), "Service destroyed");
        super.onDestroy();
    }


    private void startLocationUpdates() {
        if (Utilities.checkPermissions(this, 1)) {
            try {
                LocationRequest mLocationRequest = createLocationRequest();
                LocationServices.FusedLocationApi.requestLocationUpdates(mClient, mLocationRequest, this);
                LocationServices.FusedLocationApi.getLastLocation(mClient);
            } catch (SecurityException e) {
                FirebaseCrash.logcat(Log.ERROR, getClass().getSimpleName(), "SecurityException at startLocationUpdates (somehow)");
                //Nothing happens, as it's already handled by checkPermissions()
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null && location != mLocation) {
            mLocation = location;
            updateEngines();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Utilities.checkPermissions(this, 1)) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case MainActivity.displacementKey:
                mPreferences.setDisplacement(sharedPreferences.getFloat(MainActivity.displacementKey, MainActivity.displacementDefault));
                LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
                break;
            case MainActivity.refreshKey:
                mPreferences.setRefresh(sharedPreferences.getInt(MainActivity.refreshKey, MainActivity.refreshDefault));
                LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
                break;
            case MainActivity.typeKey:
                mPreferences.setType(sharedPreferences.getInt(MainActivity.typeKey, MainActivity.typeDefault));
                break;
            case MainActivity.zoomKey:
                mPreferences.setZoom(sharedPreferences.getInt(MainActivity.zoomKey, MainActivity.zoomDefault));
                break;
            default:
                break;
        }
        updateEngines();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }


    private LocationRequest createLocationRequest() {
        return new LocationRequest().setInterval(mPreferences.getRefreshInterval())
                .setFastestInterval(mPreferences.getRefreshInterval()).setSmallestDisplacement(mPreferences.getDisplacement());
    }

    private void updateEngines() {
        for (MapEngine e : mEngines) {
            e.changeWallpaper();
        }
    }

    private class MapEngine extends Engine {
        int mWidth = 0;
        int mHeight = 0;
        boolean isMapOnScreen = false;
        final Target mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (mHeight == 0 || mWidth == 0) {
                    mHeight = getSurfaceHolder().getSurfaceFrame().height();
                    mWidth = getSurfaceHolder().getSurfaceFrame().width();
                }
                if (url != null) {
                    mShared.edit().putString(MainActivity.lastUrlKey, url).apply();
                }
                mLoaded = bitmap;
                synchronized (getSurfaceHolder()) {
                    Canvas mCanvas = MapEngine.this.getSurfaceHolder().lockCanvas();
                    if (!isMapOnScreen) {
                        mCanvas.drawColor(Color.BLACK);
                    }
                    mCanvas.drawBitmap(bitmap, null, MapEngine.this.getSurfaceHolder().getSurfaceFrame(), null);
                    isMapOnScreen = true;
                    MapEngine.this.getSurfaceHolder().unlockCanvasAndPost(mCanvas);
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                if (mHeight == 0 || mWidth == 0) {
                    mHeight = getSurfaceHolder().getSurfaceFrame().height();
                    mWidth = getSurfaceHolder().getSurfaceFrame().width();
                }
                if (errorDrawable != null) {
                    synchronized (getSurfaceHolder()) {
                        Canvas mCanvas = MapEngine.this.getSurfaceHolder().lockCanvas();
                        mCanvas.drawColor(Color.BLACK);
                        Bitmap error = ((BitmapDrawable) errorDrawable).getBitmap();
                        mCanvas.drawBitmap(error, (mWidth - error.getWidth()) / 2,
                                (mHeight - error.getHeight()) / 2, null);
                        MapEngine.this.getSurfaceHolder().unlockCanvasAndPost(mCanvas);
                    }
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (mHeight == 0 || mWidth == 0) {
                    mHeight = getSurfaceHolder().getSurfaceFrame().height();
                    mWidth = getSurfaceHolder().getSurfaceFrame().width();
                }
                if (placeHolderDrawable != null) {
                    synchronized (getSurfaceHolder()) {
                        Canvas mCanvas = MapEngine.this.getSurfaceHolder().lockCanvas();
                        if (!isMapOnScreen) {
                            mCanvas.drawColor(Color.BLACK);
                        }
                        Bitmap placeholder = ((BitmapDrawable) placeHolderDrawable).getBitmap();
                        mCanvas.drawBitmap(placeholder, (mWidth - placeholder.getWidth()) / 2,
                                (mHeight - placeholder.getHeight()) / 2, null);
                        MapEngine.this.getSurfaceHolder().unlockCanvasAndPost(mCanvas);
                    }
                }
            }
        };

        final Target errorTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (mHeight == 0 || mWidth == 0) {
                    mHeight = getSurfaceHolder().getSurfaceFrame().height();
                    mWidth = getSurfaceHolder().getSurfaceFrame().width();
                }
                if (bitmap != null) {
                    synchronized (getSurfaceHolder()) {
                        Canvas mCanvas = MapEngine.this.getSurfaceHolder().lockCanvas();
                        mCanvas.drawColor(Color.BLACK);
                        mCanvas.drawBitmap(bitmap, (mWidth - bitmap.getWidth()) / 2,
                                (mHeight - bitmap.getHeight()) / 2, null);
                        isMapOnScreen = false;
                        MapEngine.this.getSurfaceHolder().unlockCanvasAndPost(mCanvas);
                    }
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                if (mHeight == 0 || mWidth == 0) {
                    mHeight = getSurfaceHolder().getSurfaceFrame().height();
                    mWidth = getSurfaceHolder().getSurfaceFrame().width();
                }
                if (errorDrawable != null) {
                    synchronized (getSurfaceHolder()) {
                        Canvas mCanvas = MapEngine.this.getSurfaceHolder().lockCanvas();
                        mCanvas.drawColor(Color.BLACK);
                        Bitmap error = ((BitmapDrawable) errorDrawable).getBitmap();
                        mCanvas.drawBitmap(error, (mWidth - error.getWidth()) / 2,
                                (mHeight - error.getHeight()) / 2, null);
                        MapEngine.this.getSurfaceHolder().unlockCanvasAndPost(mCanvas);
                    }
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (mHeight == 0 || mWidth == 0) {
                    mHeight = getSurfaceHolder().getSurfaceFrame().height();
                    mWidth = getSurfaceHolder().getSurfaceFrame().width();
                }
                if (placeHolderDrawable != null) {
                    synchronized (getSurfaceHolder()) {
                        Canvas mCanvas = MapEngine.this.getSurfaceHolder().lockCanvas();
                        mCanvas.drawColor(Color.BLACK);
                        Bitmap placeholder = ((BitmapDrawable) placeHolderDrawable).getBitmap();
                        mCanvas.drawBitmap(placeholder, (mWidth - placeholder.getWidth()) / 2,
                                (mHeight - placeholder.getHeight()) / 2, null);
                        MapEngine.this.getSurfaceHolder().unlockCanvasAndPost(mCanvas);
                    }
                }
            }
        };


        @Override
        public void onDestroy() {
            super.onDestroy();
            mEngines.remove(this);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mHeight = height;
            mWidth = width;
            getFromLocal();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                //Trigger wallpaper change when visible and location changes
                changeWallpaper();
            }
            Log.d(getClass().getSimpleName(), "Visibility: " + visible);
            super.onVisibilityChanged(visible);
        }


        void changeWallpaper() {
            if (!Utilities.checkPermissions(MapWallpaperService.this, 1)) {
                //No permission - no change in wallpaper
                return;
            }
            if (!isVisible()) {
                //If not visible at all, then why load?
                //noinspection UnnecessaryReturnStatement
                return;
            } else if (mLocation == null) {
                //In case there is no location
                FirebaseCrash.log("Location in mLocation is null.");
                Toast.makeText(MapWallpaperService.this, "From Here Live Wallpaper: Your device reports no location. Please wait until Google Play Services find one.", Toast.LENGTH_SHORT).show();
                Picasso.with(MapWallpaperService.this).load(R.drawable.wait).placeholder(R.drawable.wait).error(R.drawable.error).into(errorTarget);
            } else if (Utilities.checkPlayServices(MapWallpaperService.this) == ConnectionResult.SUCCESS) {
                //loadFromBing();
                loadFromMapBox();
            } else {
                //In case it's not connected, try to use local cache
                getFromLocal();
            }
        }

        void getFromLocal() {
            if (mLoaded != null) {
                //If there is a loaded bitmap, then load it
                Canvas mCanvas = MapEngine.this.getSurfaceHolder().lockCanvas();
                mCanvas.drawBitmap(mLoaded, null, MapEngine.this.getSurfaceHolder().getSurfaceFrame(), null);
                MapEngine.this.getSurfaceHolder().unlockCanvasAndPost(mCanvas);
            } else {
                //Otherwise, parse it from cache (offline)
                Picasso.with(MapWallpaperService.this).load(mShared.getString(MainActivity.lastUrlKey, "example.net"))
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .error(R.drawable.error)
                        .placeholder(R.drawable.wait)
                        .into(mTarget);
            }
        }

        void loadFromBing() {
            //Create URL
            url = "http://dev.virtualearth.net/REST/V1/Imagery/Map/" + mPreferences.parseTypeBing()
                    + "/" + mLocation.getLatitude() + "%2C" + mLocation.getLongitude() +
                    "/" + mPreferences.getZoom() + "?mapSize=" + mWidth + ","
                    + mHeight + "&format=jpeg&key=" + MainActivity.bingKey;

            //Load the map
            if (!url.equals(mShared.getString(MainActivity.lastUrlKey, ""))) {
                Picasso.with(MapWallpaperService.this)
                        .load(url)
                        .error(R.drawable.error)
                        .placeholder(R.drawable.wait)
                        .into(mTarget);
            } else {
                getFromLocal();
            }
        }

        void loadFromMapBox() {
            Mapbox.getInstance(MapWallpaperService.this, MainActivity.mapBoxKey);
            try {
                //Create URL
                MapboxStaticImage staticImage = new MapboxStaticImage.Builder()
                        .setAccessToken(Mapbox.getAccessToken())
                        .setUsername("mapbox")
                        .setStyleId(mPreferences.parseTypeMapBox())
                        .setLon(mLocation.getLongitude())
                        .setLat(mLocation.getLatitude())
                        .setZoom(mPreferences.getZoom())
                        .setWidth(mWidth)
                        .setHeight(mHeight)
                        .setRetina(true)
                        .build();
                url = staticImage.getUrl().toString();
            } catch (ServicesException e) {
                //Handle possible exceptions from MapBox
                Toast.makeText(MapWallpaperService.this, "Map provider has encountered an exception: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                getFromLocal();
            } finally {
                if (!url.equals(mShared.getString(MainActivity.lastUrlKey, ""))) {
                    //Load the map
                    Picasso.with(MapWallpaperService.this)
                            .load(url)
                            .error(R.drawable.error)
                            .placeholder(R.drawable.wait)
                            .into(mTarget);
                } else {
                    getFromLocal();
                }
            }

        }

    }

}
