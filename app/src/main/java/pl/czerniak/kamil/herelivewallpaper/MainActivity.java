package pl.czerniak.kamil.herelivewallpaper;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;

public class MainActivity extends AppCompatActivity {

    public static final String zoomKey = "zoom"; //Integer, describes zoom (from 1 to 20)
    public static final String typeKey = "mapType"; //Integer, describes position in Spinner
    public static final String refreshKey = "refreshRate"; //Integer, describes time of refresh in minutes
    public static final String displacementKey = "displacement"; //Integer, describes time of refresh in minutes
    public static final String isStartedKey = "isStarted";
    public static final String lastUrlKey = "url";
    public static final int zoomDefault = 15;
    public static final int refreshDefault = 10;
    public static final int typeDefault = 0;
    public static final float displacementDefault = 10.0f;
    public static final boolean isStartedDefault = false;
    public static final String mapBoxKey = Secrets.mapBoxKey;
    public static final String bingKey = Secrets.bingKey;
    private SharedPreferences prefs;
    private Spinner map_types;
    private EditText refresh_rate;
    private EditText zoom;
    private EditText displacement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Create an ad
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(Secrets.testDeviceID).build();
        mAdView.loadAd(adRequest);

        //Populate options in Activity
        map_types = (Spinner) findViewById(R.id.map_type);
        refresh_rate = (EditText) findViewById(R.id.refresh_rate);
        zoom = (EditText) findViewById(R.id.zoom);
        displacement = (EditText) findViewById(R.id.displacement);

        //Populating with data from SharedPreferences (if they exist)
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        map_types.setSelection(prefs.getInt(typeKey, typeDefault));
        refresh_rate.setText(String.valueOf(prefs.getInt(refreshKey, refreshDefault)));
        zoom.setText(String.valueOf(prefs.getInt(zoomKey, zoomDefault)));
        displacement.setText(String.valueOf(prefs.getFloat(displacementKey, displacementDefault)));

        //Check if everything is in order
        Utilities.checkPlayServices(this);
        Utilities.checkPermissions(this, 0);
    }

    @SuppressWarnings("UnusedParameters")
    public void onClick(View view) {
        if (Utilities.checkPlayServices(this) == ConnectionResult.SUCCESS) {

            //Save to preferences
            if (prefs.getInt(zoomKey, zoomDefault) != Integer.valueOf(zoom.getText().toString())) {
                prefs.edit().putInt(zoomKey, Integer.valueOf(zoom.getText().toString())).apply();
            }
            if (prefs.getInt(typeKey, typeDefault) != map_types.getSelectedItemPosition()) {
                prefs.edit().putInt(typeKey, map_types.getSelectedItemPosition()).apply();
            }
            if (prefs.getInt(refreshKey, refreshDefault) != Integer.valueOf(refresh_rate.getText().toString())) {
                prefs.edit().putInt(refreshKey, Integer.valueOf(refresh_rate.getText().toString())).apply();
            }
            if (prefs.getFloat(displacementKey, displacementDefault) != Float.valueOf(displacement.getText().toString())) {
                prefs.edit().putFloat(displacementKey, Integer.valueOf(displacement.getText().toString())).apply();
            }

            Intent intent = new Intent(
                    WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, MapWallpaperService.class));
            startActivity(intent);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_acknowledgements:
                startActivity(new Intent(this, AcknowledgementActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

}
