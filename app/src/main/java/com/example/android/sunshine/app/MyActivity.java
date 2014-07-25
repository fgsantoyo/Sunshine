package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MyActivity extends ActionBarActivity {

    private final String TAG = MyActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
        Log.v(TAG," onCreate called");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
            return true;
        } else if (id == R.id.action_map){
            openPreferredLocationInMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openPreferredLocationInMap() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String preferredLocation = sharedPreferences.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default_value));

        Uri uri = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", preferredLocation)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't call " + preferredLocation + ", no receiving apps installed!");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG," onStart called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG," onResume called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG," onPause called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG," onStop called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG," onDestroy called");
    }
}
