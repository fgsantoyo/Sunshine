package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by newuser on 7/21/14.
 */
public class ForecastFragment extends Fragment {

    private static final String TAG = ForecastFragment.class.getSimpleName();

    private ArrayAdapter<String> listviewAdapter;

    private SharedPreferences sharedPreferences;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        final String zipcode = sharedPreferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default_value));
        final String unitSystem = sharedPreferences.getString(getString(R.string.pref_units_key),
                getString(R.string.pref_units_default));
        Log.v(TAG, "Fetching weather conditions for location: " + zipcode);
        Log.v(TAG, "Fetching weather conditions in: " + unitSystem);

        new FetchWeatherTask().execute(zipcode, "json", unitSystem, "7");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my, container, false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        listviewAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList());
        ListView listviewForecast = (ListView) rootView.findViewById(R.id.listview_forecast);
        listviewForecast.setAdapter(listviewAdapter);

        listviewForecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailIntent = new Intent(getActivity(), ForecastDetailActivity.class);
                detailIntent.putExtra(Intent.EXTRA_TEXT, listviewAdapter.getItem(position));
                startActivity(detailIntent);
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            String postalCode = "32746";
            String mode = "json";
            String units = "metric";
            int days = 1;
            if (null != params && params.length > 0) {
                postalCode = params[0];
                mode = params[1];
                units = params[2];
                days = Integer.parseInt(params[3]);
            }
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily/";

                String endpoint = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter("q", postalCode)
                        .appendQueryParameter("mode", mode)
                        .appendQueryParameter("units", units)
                        .appendQueryParameter("cnt", params[3])
                        .build()
                        .toString();

                URL url = new URL(endpoint);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }

                forecastJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            String[] result = new String[]{};
            try {
                result = Utils.getWeatherDataFromJson(forecastJsonStr, days);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
            }

            return result;
        }

        @Override
        protected void onPostExecute(String[] s) {
            super.onPostExecute(s);
            if (null != s) {
                listviewAdapter.clear();
                for (String item : s) {
                    listviewAdapter.add(item);
                }
                //With ArrayAdaptor there is no need to call the below method as the adaptor calls
                //internally
//                listviewAdapter.notifyDataSetChanged();
            }
        }
    }
}
