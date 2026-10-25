package tv.afnon.home;

import java.util.ArrayList;
import java.util.List;

/** Home's four large favourites and up to five smaller favourites below. */
final class FavouriteChoice {
    static final int MAX = 9;
    static int move(int slot, int count, int dx, int dy) {
        if (count < 1 || count > MAX) return -1;
        slot = Math.max(0, Math.min(count - 1, slot));
        int start = slot < 4 ? 0 : 4, end = slot < 4 ? Math.min(3, count - 1) : count - 1;
        if (dx != 0) return Math.max(start, Math.min(end, slot + dx));
        if (dy > 0 && slot < 4 && count > 4) return Math.min(count - 1, slot + 4);
        if (dy < 0 && slot >= 4) return Math.min(3, slot - 4);
        return slot;
    }
    static List<String> replace(List<String> current, int slot, String app) {
        if (slot < 0 || slot >= current.size() || current.size() > MAX || app == null || app.isEmpty()
            || current.contains(app)) throw new IllegalArgumentException("Invalid favourite replacement");
        List<String> next = new ArrayList<>(current);
        next.set(slot, app);
        return next;
    }
}
