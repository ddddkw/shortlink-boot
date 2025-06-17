package org.example.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShardingTableConfig {

    private static  final List<String> TablePrefixList = new ArrayList<>();

    static {
        TablePrefixList.add("0");
        TablePrefixList.add("1");
    }

    public static String getRandomPrefix(String code){
        int hashCode = code.hashCode();
        int index = Math.abs(hashCode) % TablePrefixList.size();
        return TablePrefixList.get(index);
    }
}
