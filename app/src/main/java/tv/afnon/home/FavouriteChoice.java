package tv.afnon.home;

import java.util.ArrayList;
import java.util.List;

/** Home favourites use a classic row or at most two balanced rows. */
final class FavouriteChoice {
    static final int QIBLA = 0, WIFI = 1, SETTINGS = 2, F0 = 3, M0 = 12, ALL = 17, RADIO = 18, INPUTS = 19, NODES = 20;
    static final int MAX = 9;
    static int visibleCount(int count, boolean classic) { return Math.max(0, Math.min(classic ? 4 : MAX, count)); }
    static int columns(int count, boolean classic) { return Math.max(1, classic ? visibleCount(count, true) : (Math.min(MAX, count) + 1) / 2); }
    static int move(int slot, int count, int dx, int dy, boolean classic) {
        if (count < 1 || count > MAX) return -1;
        slot = Math.max(0, Math.min(count - 1, slot));
        int columns = columns(count, classic), start = slot / columns * columns, end = Math.min(start + columns - 1, count - 1);
        if (dx != 0) return Math.max(start, Math.min(end, slot + dx));
        if (dy > 0 && slot < columns && count > columns) return Math.min(count - 1, slot + columns);
        if (dy < 0 && slot >= columns) return slot - columns;
        return slot;
    }
    /** Logical directions; MainActivity swaps horizontal keys in Arabic before calling. */
    static int homeMove(int from, int count, int recent, boolean classic, int lastFav, int dx, int dy) {
        int columns = columns(count, classic);
        boolean left = dx < 0, right = dx > 0, up = dy < 0, down = dy > 0;
        int to = -1;
        switch (from) {
            case QIBLA: to = left ? RADIO : right ? WIFI : down ? F0 : -1; break;
            case INPUTS: to = right ? RADIO : down ? F0 : -1; break;
            case RADIO: to = left ? INPUTS : right ? QIBLA : down ? F0 : -1; break;
            case WIFI: to = left ? QIBLA : right ? SETTINGS : down ? F0 + columns / 2 : -1; break;
            case SETTINGS: to = left ? WIFI : down ? F0 + columns - 1 : -1; break;
            default:
                if (from >= F0 && from < F0 + count) {
                    int slot = from - F0, col = slot % columns;
                    if (left) to = col == 0 ? -1 : from - 1;
                    else if (right && col < columns - 1 && slot + 1 < count) to = from + 1;
                    else if (up) to = slot >= columns ? from - columns : col == 0 ? RADIO : col == columns - 1 ? SETTINGS : WIFI;
                    else if (down) {
                        if (slot < columns && count > columns) to = F0 + Math.min(count - 1, slot + columns);
                        else to = recent == 0 ? ALL : M0 + Math.min(recent - 1, col * 5 / columns);
                    }
                } else if (from >= M0) {
                    if (left) to = from == ALL ? (recent == 0 ? -1 : M0 + recent - 1) : from == M0 ? -1 : from - 1;
                    else if (right) to = from == ALL ? -1 : from < M0 + recent - 1 ? from + 1 : ALL;
                    else if (up) to = count == 0 ? QIBLA : Math.min(F0 + count - 1, lastFav);
                    else if (down) to = ALL;
                }
        }
        if (down && (from < F0 || from == RADIO || from == INPUTS) && count == 0) return recent > 0 ? M0 : ALL;
        return to < 0 ? from : to;
    }

    static List<String> replace(List<String> current, int slot, String app) {
        if (slot < 0 || slot >= current.size() || current.size() > MAX || app == null || app.isEmpty()
            || current.contains(app)) throw new IllegalArgumentException("Invalid favourite replacement");
        List<String> next = new ArrayList<>(current);
        next.set(slot, app);
        return next;
    }
}
