package tv.afnon.home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.widget.Toast;
import java.util.*;

/** Public TV inputs, with CleanYourTV's MIME routing and hardware-tuner preference. */
final class InputSources {
    static final class Source {
        final String label;
        final Intent intent;
        Source(String label, Intent intent) { this.label = label; this.intent = intent; }
    }

    static List<Source> list(Context c) {
        List<Source> out = new ArrayList<>();
        TvInputManager manager = c.getSystemService(TvInputManager.class);
        if (manager == null) return out;
        Text t = Screen.loadText(c);
        try {
            List<TvInputInfo> inputs = new ArrayList<>(manager.getTvInputList());
            inputs.sort(Comparator.comparing(TvInputInfo::getId));
            TvInputInfo tuner = null;
            Map<String, TvInputInfo> byId = new HashMap<>();
            for (TvInputInfo info : inputs) if (!info.isHidden(c)) byId.put(info.getId(), info);
            for (TvInputInfo info : inputs) {
                if (info.isHidden(c)) continue;
                if (info.getType() == TvInputInfo.TYPE_TUNER) {
                    if ((info.getServiceInfo().applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                            && InputOrder.betterTuner(info.getId(), tuner == null ? null : tuner.getId())) tuner = info;
                    continue;
                }
                if (!info.isPassthroughInput()) continue;
                boolean hasChild = false;
                for (TvInputInfo child : byId.values()) if (info.getId().equals(child.getParentId())) hasChild = true;
                if (hasChild) continue;
                String label = label(c, info);
                TvInputInfo parent = byId.get(info.getParentId());
                if (parent != null) label += t.sep() + label(c, parent);
                out.add(new Source(label, new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(TvContract.buildChannelUriForPassthroughInput(info.getId()), TvContract.Channels.CONTENT_ITEM_TYPE)));
            }
            if (tuner != null) {
                String pkg = tuner.getServiceInfo().packageName;
                // Restrict tuner routing to its TV player; unrelated video-preview inputs are not Live TV.
                Intent channels = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(TvContract.buildChannelsUriForInput(tuner.getId()), TvContract.Channels.CONTENT_TYPE);
                android.content.pm.ResolveInfo player = c.getPackageManager().resolveActivity(channels, 0);
                if (player != null && player.activityInfo != null) {
                    Intent live = new Intent(channels).setPackage(player.activityInfo.packageName);
                    Intent hardware = new Intent(Intent.ACTION_VIEW)
                        .setDataAndType(TvContract.buildChannelUriForPassthroughInput(tuner.getId()), TvContract.Channels.CONTENT_ITEM_TYPE)
                        .setPackage(player.activityInfo.packageName);
                    if (hardware.resolveActivity(c.getPackageManager()) != null) live = hardware;
                    out.add(new Source(t.liveTv(), live));
                } else {
                    Intent live = Apps.launchIntent(c, pkg);
                    if (live != null) out.add(new Source(t.liveTv(), live));
                }
            }
        } catch (RuntimeException ignored) { /* The TV's source menu remains the fallback. */ }
        return out;
    }

    private static String label(Context c, TvInputInfo info) {
        CharSequence custom = info.loadCustomLabel(c), base = info.loadLabel(c);
        String name = base == null ? info.getId() : base.toString();
        if (custom != null && custom.length() > 0 && !custom.toString().equals(name)) name = custom + " · " + name;
        return name;
    }

    static void open(Context c, Intent target) {
        if (launch(c, target)) return;
        if (launch(c, FirmwareKeys.sourceMenu(c))) return;
        if (launch(c, new Intent("com.android.tv.action.VIEW_INPUTS"))) return;
        Toast.makeText(c, Screen.loadText(c).systemUnavailable(), Toast.LENGTH_LONG).show();
    }

    static boolean launch(Context c, Intent target) {
        if (target == null) return false;
        try { c.startActivity(new Intent(target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); return true; }
        catch (RuntimeException unavailable) { return false; }
    }
}
