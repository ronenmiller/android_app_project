package il.ac.technion.touricity.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class TouricitySyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static TouricitySyncAdapter sTouricitySyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("TouricitySyncService", "onCreate - TouricitySyncService");
        synchronized (sSyncAdapterLock) {
            if (sTouricitySyncAdapter == null) {
                sTouricitySyncAdapter = new TouricitySyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sTouricitySyncAdapter.getSyncAdapterBinder();
    }
}