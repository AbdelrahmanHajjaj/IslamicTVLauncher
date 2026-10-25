package tv.afnon.home;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/** Remote and Settings entry to the same quick input chooser used on Home. */
public final class InputsActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if (RemoteKeys.showInputs()) { finish(); return; }
        Ui.init(this);
        View menu = QuickMenus.inputs(this, this::finish);
        setContentView(menu);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.gravity = Gravity.TOP | (Screen.loadText(this).ar ? Gravity.LEFT : Gravity.RIGHT);
        lp.x = Ui.ipx(96); lp.y = Ui.ipx(152);
        lp.width = Ui.ipx(640); lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(lp);
        QuickMenus.focusFirst(menu);
    }
}
