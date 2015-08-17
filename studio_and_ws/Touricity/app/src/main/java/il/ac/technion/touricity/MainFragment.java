package il.ac.technion.touricity;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import il.ac.technion.touricity.data.ToursContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

    private long mLocationId = -1;
    private String mLocationName = null;

    TextView mTextView;

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mTextView = (TextView)rootView.findViewById(R.id.textview_location_main);

        return rootView;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SearchManager searchManager = (SearchManager)getActivity()
                    .getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView)menu
                    .findItem(R.id.search_location).getActionView();
            if (searchView != null) {
                searchView.setSearchableInfo(searchManager.
                        getSearchableInfo(getActivity().getComponentName()));
                searchView.setIconifiedByDefault(true);
                searchView.setQueryHint(getResources().getString(R.string.search_hint));

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        // Delete previous results
                        getActivity().getContentResolver().delete(
                                ToursContract.OSMEntry.CONTENT_URI,
                                null,
                                null
                        );
                        // do something with s, the entered string
//                        Log.d("test", getActivity().);
                        Context context = getActivity();
                        Intent intent = new Intent(context, SearchActivity.class);
                        intent.setAction(Intent.ACTION_SEARCH);
                        intent.putExtra(SearchManager.QUERY, s);
                        getActivity().startActivity(intent);

                        // return true if the query has been handled by the listener
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {

                        // return true if the action was handled by the listener
                        return true;
                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.search_location) {
            getActivity().onSearchRequested();
            return true;
        }
        if (id == R.id.action_settings) {
            Context context = getActivity();
            Intent settingsIntent = new Intent(context, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onLocationChanged(long id, String name) {
        // save important values to preferences
        Context context = getActivity();
        mLocationId = id;
        mLocationName = name;
        mTextView.setText(mLocationName);
    }
}
