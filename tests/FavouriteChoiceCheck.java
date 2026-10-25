package tv.afnon.home;
import java.util.*;
public final class FavouriteChoiceCheck {
    static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    public static void main(String[] args) {
        for (int count = 1; count <= 9; count++) for (int slot = 0; slot < count; slot++)
            for (int[] delta : new int[][] {{-1,0},{1,0},{0,-1},{0,1}}) {
                int moved = FavouriteChoice.move(slot,count,delta[0],delta[1],false);
                check(moved >= 0 && moved < count);
            }
        check(FavouriteChoice.columns(6,false)==3);
        check(FavouriteChoice.columns(9,false)==5);
        check(FavouriteChoice.move(2,6,0,1,false)==5);
        check(FavouriteChoice.move(8,9,0,-1,false)==3);
        check(FavouriteChoice.columns(0,true)==1);
        check(FavouriteChoice.visibleCount(0,true)==0);
        check(FavouriteChoice.visibleCount(9,true)==4);
        check(FavouriteChoice.visibleCount(9,false)==9);
        check(FavouriteChoice.columns(4,true)==4);
        for (int count=1; count<=4; count++) for (int slot=0; slot<count; slot++) {
            check(FavouriteChoice.move(slot,count,0,1,true)==slot);
            check(FavouriteChoice.move(slot,count,0,-1,true)==slot);
            check(FavouriteChoice.move(slot,count,1,0,true)==Math.min(count-1,slot+1));
            check(FavouriteChoice.move(slot,count,-1,0,true)==Math.max(0,slot-1));
        }
        List<String> before = Arrays.asList("a","b","c","d","e","f");
        check(FavouriteChoice.replace(before,4,"new").equals(Arrays.asList("a","b","c","d","new","f")));
        check(before.get(4).equals("e"));
        try { FavouriteChoice.replace(before,2,"a"); throw new AssertionError(); } catch (IllegalArgumentException expected) {}
        try { FavouriteChoice.replace(before,6,"new"); throw new AssertionError(); } catch (IllegalArgumentException expected) {}
    }
}
