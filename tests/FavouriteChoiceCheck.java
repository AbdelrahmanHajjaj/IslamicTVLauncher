package tv.afnon.home;
import java.util.*;
public final class FavouriteChoiceCheck {
    static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    public static void main(String[] args) {
        for (int count = 1; count <= 9; count++) for (int slot = 0; slot < count; slot++)
            for (int[] delta : new int[][] {{-1,0},{1,0},{0,-1},{0,1}}) {
                int moved = FavouriteChoice.move(slot,count,delta[0],delta[1]);
                check(moved >= 0 && moved < count);
            }
        check(FavouriteChoice.columns(6)==3);
        check(FavouriteChoice.columns(9)==5);
        check(FavouriteChoice.move(2,6,0,1)==5);
        check(FavouriteChoice.move(8,9,0,-1)==3);
        List<String> before = Arrays.asList("a","b","c","d","e","f");
        check(FavouriteChoice.replace(before,4,"new").equals(Arrays.asList("a","b","c","d","new","f")));
        check(before.get(4).equals("e"));
        try { FavouriteChoice.replace(before,2,"a"); throw new AssertionError(); } catch (IllegalArgumentException expected) {}
        try { FavouriteChoice.replace(before,6,"new"); throw new AssertionError(); } catch (IllegalArgumentException expected) {}
    }
}
