package tv.afnon.home;

/** Home-only geometry in logical (start/end) coordinates. Prayer screens keep their own layout. */
final class HomeLayout {
    static final int MIHRAB = 0, COMPACT = 1, HIDDEN = 2;
    static final int ALL = 0, CURRENT = 1, NEXT = 2, BOTH = 3, NONE = 4;
    final int mode, table, count, columns, rows;
    final float x, width, panelX, panelY, tableY, labelY, favouritesY, tileWidth, tileHeight, recentLabelY, recentY;

    HomeLayout(int mode, int side, int bar, int table, int count, boolean classic, boolean rtl) {
        this.mode = clamp(mode, HIDDEN); this.table = clamp(table, NONE);
        this.count = FavouriteChoice.visibleCount(count, classic);
        columns = FavouriteChoice.columns(this.count, classic);
        rows = this.count == 0 ? 0 : (this.count + columns - 1) / columns;
        float margin = Tokens.SPACE_SAFE_INLINE, gap = Tokens.LAYOUT_HOME_GAP;
        float full = Tokens.LAYOUT_HOME_WIDTH - 2 * margin;
        boolean panelAtStart = side == 0 || (side == 1) != rtl;
        width = this.mode == MIHRAB ? full - Tokens.LAYOUT_HOME_MIHRAB_WIDTH - gap : full;
        x = this.mode == MIHRAB && panelAtStart ? margin + Tokens.LAYOUT_HOME_MIHRAB_WIDTH + gap : margin;
        panelX = panelAtStart ? margin : margin + width + gap;
        panelY = this.mode == MIHRAB ? Tokens.LAYOUT_HOME_BOTTOM - Tokens.LAYOUT_HOME_MIHRAB_HEIGHT
            : bar == 1 ? Tokens.LAYOUT_HOME_BOTTOM - Tokens.LAYOUT_HOME_BAR_HEIGHT : Tokens.LAYOUT_HOME_TOP;
        float top = Tokens.LAYOUT_HOME_TOP;
        float bottom = Tokens.LAYOUT_HOME_BOTTOM;
        if (this.mode == COMPACT) {
            if (bar == 1) bottom = panelY - gap;
            else top += Tokens.LAYOUT_HOME_BAR_HEIGHT + gap;
        }
        tableY = top;
        labelY = top + (this.table == NONE ? 0 : Tokens.LAYOUT_HOME_TABLE_HEIGHT + gap);
        favouritesY = labelY + (rows == 0 ? 0 : Tokens.LAYOUT_HOME_LABEL_HEIGHT);
        tileWidth = Math.min(Tokens.LAYOUT_HOME_TILE_MAX_WIDTH, (width - (columns - 1) * gap) / columns);
        float available = bottom - favouritesY - (rows == 0 ? 0 : gap) - Tokens.LAYOUT_HOME_LABEL_HEIGHT - Tokens.LAYOUT_HOME_RECENT_HEIGHT;
        tileHeight = rows == 0 ? 0 : Math.min(Tokens.LAYOUT_HOME_TILE_MAX_HEIGHT, (available - (rows - 1) * gap) / rows);
        recentLabelY = favouritesY + (rows == 0 ? 0 : rows * tileHeight + (rows - 1) * gap + gap);
        recentY = recentLabelY + Tokens.LAYOUT_HOME_LABEL_HEIGHT;
    }

    static int clamp(int value, int max) { return Math.max(0, Math.min(max, value)); }
    static int[] prayers(int table, int current, int next) {
        switch (clamp(table, NONE)) {
            case CURRENT: return new int[] {current};
            case NEXT: return new int[] {next};
            case BOTH: return new int[] {current, next};
            case NONE: return new int[0];
            default: return new int[] {0, 1, 2, 3, 4, 5};
        }
    }
}
