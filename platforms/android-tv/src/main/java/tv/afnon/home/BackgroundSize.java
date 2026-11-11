package tv.afnon.home;

/** Match the app window up to Full HD; never allocate a 4K wallpaper on a low-memory TV. */
final class BackgroundSize {
    final int width, height;

    BackgroundSize(int width, int height) {
        width = Math.max(1, width); height = Math.max(1, height);
        float scale = Math.min(1f, Math.min(Tokens.BACKGROUND_MAX_WIDTH / width, Tokens.BACKGROUND_MAX_HEIGHT / height));
        this.width = Math.max(1, Math.round(width * scale));
        this.height = Math.max(1, Math.round(height * scale));
    }
}
