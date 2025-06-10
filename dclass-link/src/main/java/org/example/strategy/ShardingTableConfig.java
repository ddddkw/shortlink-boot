package org.example.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShardingTableConfig {

    private static  final List<String> tablePrefixList = new ArrayList<>();

    static {
        tablePrefixList.add("0");
        tablePrefixList.add("1");
    }

    private static Random random = new Random();

    public static String getRandomPrefix(){
        int index = random.nextInt(tablePrefixList.size());
        return tablePrefixList.get(index);
    }
}
