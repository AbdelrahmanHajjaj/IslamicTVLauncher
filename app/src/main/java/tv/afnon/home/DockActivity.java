package tv.afnon.home;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/** No-display bridge for firmware buttons that can only launch an activity. */
public final class DockActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        finish();
        new Handler(Looper.getMainLooper()).post(() -> {
            if (!RemoteKeys.toggleDock()) Toast.makeText(getApplicationContext(), Screen.loadText(this).enableButtons(), Toast.LENGTH_LONG).show();
        });
    }
}
