package pl.czerniak.kamil.herelivewallpaper;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.services.android.telemetry.MapboxTelemetry;

/**
 * pl.czerniak.kamil.herelivewallpaper from project HereLiveWallpaper.
 * Created by kamil on 18/06/2017.
 */

public class AcknowledgementActivity extends AppCompatActivity {

    String psText;
    boolean isTelemetryOn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_acknowledgements);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_acknowledgements));

        psText = GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(getApplicationContext());
        Mapbox instance = Mapbox.getInstance(this, MainActivity.mapBoxKey);
        isTelemetryOn = MapboxTelemetry.getInstance().isTelemetryEnabled();

        //Set up text in all textviews
        setHtml((TextView) findViewById(R.id.picasso_text), getString(R.string.picasso_license));

        setHtml((TextView) findViewById(R.id.mapbox_text), getString(R.string.mapbox_license));
        CheckBox mapboxButton = (CheckBox) findViewById(R.id.telemetry_toggle);
        mapboxButton.setChecked(isTelemetryOn);
        mapboxButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MapboxTelemetry.getInstance().setTelemetryEnabled(isChecked);
            }
        });

        //((TextView)findViewById(R.id.playservices_text)).setText(psText);

        TextView asl = (TextView) findViewById(R.id.asl_text);
        setHtml(asl, getString(R.string.asl_license));


    }

    void setHtml(TextView textView, String string) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(string, Html.FROM_HTML_MODE_COMPACT));
        } else {
            textView.setText(Html.fromHtml(string));
        }
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

}


