package il.ac.technion.touricity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    public static final String ACTION_NOT_FOUND = "action_not_found";
    public static final String ACTION_FOUND = "action_found";
    public static final String EXTRA_LOC_ID = "extra_loc_id";
    public static final String EXTRA_LOC_NAME = "extra_loc_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (ACTION_NOT_FOUND.equals(intent.getAction())) {
            Toast.makeText(this, "Location not found.", Toast.LENGTH_LONG).show();
        }
        else if (ACTION_FOUND.equals(intent.getAction())) {
            long osmID = intent.getLongExtra(EXTRA_LOC_ID, -1);
            String locationName = intent.getStringExtra(EXTRA_LOC_NAME);

            MainFragment mf = (MainFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_main);
            mf.onLocationChanged(osmID, locationName);
        }
    }
}
