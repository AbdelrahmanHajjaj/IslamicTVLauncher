package tv.afnon.home;

import java.util.Arrays;

public final class HomeLayoutCheck {
    private static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) {
        int cases = 0;
        for (int mode = 0; mode < 3; mode++) for (int side = 0; side < 3; side++)
        for (int bar = 0; bar < 2; bar++) for (int table = 0; table < 5; table++)
        for (int count = 0; count <= 9; count++) for (boolean classic : new boolean[] {false, true})
        for (boolean rtl : new boolean[] {false, true}) for (boolean labels : new boolean[] {false, true}) {
            HomeLayout h = new HomeLayout(mode, side, bar, table, count, classic, rtl, labels);
            check(h.x >= 96 && h.x + h.width <= 1824, "safe horizontal bounds");
            check(h.recentY + 94 <= 1024, "recent apps fit");
            check(h.count == 0 || h.tileHeight >= 120, "readable app tiles");
            check(h.tileWidth > 0 && h.tileWidth <= 640, "bounded tile width");
            check(h.count == 0 || h.favouritesY + h.rows * h.tileHeight + (h.rows - 1) * 24 < h.recentLabelY, "app rows do not overlap");
            check(table == 4 || h.tableY + 112 < h.labelY, "table does not overlap apps");
            if (mode == 0) {
                check(h.panelX + 600 < h.x || h.x + h.width < h.panelX, "mihrab does not overlap content");
                float physical = rtl ? 1920 - h.panelX - 600 : h.panelX;
                if (side == 1) check(physical == 96, "explicit left is physical left in either language");
                if (side == 2) check(physical == 1224, "explicit right is physical right in either language");
            }
            if (mode == 1 && bar == 0) check(h.panelY + 100 < h.tableY, "top bar separated");
            if (mode == 1 && bar == 1) check(h.recentY + 94 < h.panelY, "bottom bar separated");
            float reach = Tokens.FOCUS_RING_OFFSET + Tokens.FOCUS_RING_WIDTH;
            float fx = reach + (h.tileWidth + 2 * reach) * (Tokens.FOCUS_SCALE - 1) / 2;
            float fy = reach + (h.tileHeight + 2 * reach) * (Tokens.FOCUS_SCALE - 1) / 2;
            if (h.count > 0) {
                check(h.favouritesY - fy > h.labelY + Tokens.FONT_SIZE_LABEL * 1.4f + 8, "focused favourite clears its label");
                check(Tokens.LAYOUT_HOME_GAP - fy > 4, "focused tile clears the next row");
                if (mode == 0) check(Tokens.LAYOUT_HOME_PANEL_GAP - fx > 16, "focused tile clears mihrab");
            }
            if (mode != 0 && count == 6 && !classic) check(h.columns == 4, "wide Home uses four columns");
            cases++;
        }
        check(Arrays.equals(HomeLayout.prayers(0, -1, 2), new int[] {0,1,2,3,4,5}), "all retains sunrise");
        check(Arrays.equals(HomeLayout.prayers(1, -1, 2), new int[] {-1}), "no false current prayer at sunrise");
        check(Arrays.equals(HomeLayout.prayers(2, 5, 0), new int[] {0}), "next Fajr after Isha");
        check(Arrays.equals(HomeLayout.prayers(3, -1, 0), new int[] {-1,0}), "both keeps two slots before Fajr");
        check(HomeLayout.prayers(4, 2, 3).length == 0, "hidden has no slots");
        for (int count = 0; count <= 9; count++) for (int recent = 0; recent <= 5; recent++) for (boolean classic : new boolean[] {false, true}) for (boolean wide : new boolean[] {false, true}) {
            int visible = FavouriteChoice.visibleCount(count, classic);
            java.util.Set<Integer> expected = new java.util.HashSet<>(Arrays.asList(0, 1, 2, 17, 18, 19));
            for (int i = 0; i < visible; i++) expected.add(3 + i);
            for (int i = 0; i < recent; i++) expected.add(12 + i);
            java.util.Set<Integer> reached = new java.util.HashSet<>();
            java.util.List<Integer> queue = new java.util.ArrayList<>(); queue.add(visible > 0 ? 3 : recent > 0 ? 12 : 17);
            for (int i = 0; i < queue.size(); i++) {
                int from = queue.get(i);
                if (!reached.add(from)) continue;
                for (int[] direction : new int[][] {{-1,0}, {1,0}, {0,-1}, {0,1}}) {
                    int to = FavouriteChoice.homeMove(from, visible, recent, FavouriteChoice.columns(visible, classic, wide), 3, direction[0], direction[1]);
                    check(expected.contains(to), "focus never targets a hidden node");
                    if (!reached.contains(to)) queue.add(to);
                }
            }
            check(reached.equals(expected), "every header and app control stays reachable");
        }
        check(FavouriteChoice.move(3, 6, 0, 1, 4) == 5, "wide grid lower row clamps to last tile");
        check(FavouriteChoice.move(4, 6, 0, -1, 4) == 0, "wide grid returns to its column");
        check(FavouriteChoice.homeMove(0, 6, 5, 4, 3, 1, 0) == 19, "Qibla is outside Inputs");
        System.out.println(cases + " Home layout combinations checked");
    }
}
