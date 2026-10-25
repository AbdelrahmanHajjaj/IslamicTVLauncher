package tv.afnon.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Bundle;
import java.util.*;

/** Inputs remain reachable when stock Home is disabled. TV/input services stay installed. */
public final class InputsActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        Text t = Screen.loadText(this);
        List<String> names = new ArrayList<>();
        List<Intent> targets = new ArrayList<>();
        TvInputManager manager = (TvInputManager)getSystemService(TV_INPUT_SERVICE);
        try {
            if (manager != null) for (TvInputInfo info : manager.getTvInputList()) {
                if (info.isHidden(this)) continue;
                Intent target = info.isPassthroughInput() ? new Intent(Intent.ACTION_VIEW, TvContract.buildChannelUriForPassthroughInput(info.getId()))
                    : new Intent(Intent.ACTION_VIEW).setDataAndType(TvContract.Channels.CONTENT_URI, "vnd.android.cursor.dir/channel");
                CharSequence custom = info.loadCustomLabel(this);
                names.add((custom == null ? info.loadLabel(this) : custom).toString());
                targets.add(target);
            }
        } catch (RuntimeException ignored) { }
        names.add(t.inputsSystem());
        new AlertDialog.Builder(this).setTitle(t.inputsTitle()).setItems(names.toArray(new String[0]), (d, which) -> {
            Intent target = which < targets.size() ? targets.get(which) : FirmwareKeys.sourceMenu(this);
            if (target == null) target = new Intent("com.android.tv.action.VIEW_INPUTS");
            try { startActivity(target); } catch (RuntimeException e) { SystemScreens.open(this, android.provider.Settings.ACTION_SETTINGS); }
            finish();
        }).setOnCancelListener(d -> finish()).show();
    }
}
