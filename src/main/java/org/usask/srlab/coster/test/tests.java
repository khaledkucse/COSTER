package org.usask.srlab.coster.test;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class tests {
    private static List<String> replaceListItem(List<String> desiredList, String searchKey, List<String> comparingList) {
        for(int index= desiredList.indexOf(searchKey);index<desiredList.size();index++){
            if(!desiredList.get(index).equals(comparingList.get(index)))
                desiredList.set(index,comparingList.get(index));
        }
        return desiredList;
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
        System.out.println(System.currentTimeMillis());
    }

//    private static BitSet removeAt (BitSet b,       int at, int len, int newSize) {
//        BitSet clone = (BitSet) b.clone();
//        int max = b.length();
//        while (at < max) {
//            clone.set(at, b.get(at + len));
//            at++;
//        }
//        clone.set();
//    }

}
