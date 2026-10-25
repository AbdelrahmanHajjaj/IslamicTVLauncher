package tv.afnon.home;

import java.util.ArrayList;
import java.util.List;

/** Home favourites occupy at most two balanced rows. */
final class FavouriteChoice {
    static final int MAX = 9;
    static int columns(int count) { return Math.max(1, (Math.min(MAX, count) + 1) / 2); }
    static int move(int slot, int count, int dx, int dy) {
        if (count < 1 || count > MAX) return -1;
        slot = Math.max(0, Math.min(count - 1, slot));
        int columns = columns(count), start = slot / columns * columns, end = Math.min(start + columns - 1, count - 1);
        if (dx != 0) return Math.max(start, Math.min(end, slot + dx));
        if (dy > 0 && slot < columns && count > columns) return Math.min(count - 1, slot + columns);
        if (dy < 0 && slot >= columns) return slot - columns;
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
