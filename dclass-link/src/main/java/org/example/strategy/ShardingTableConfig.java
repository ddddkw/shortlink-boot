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

    private static Random random = new Random();

    public static String getRandomPrefix(){
        int index = random.nextInt(TablePrefixList.size());
        return TablePrefixList.get(index);
    }
}
