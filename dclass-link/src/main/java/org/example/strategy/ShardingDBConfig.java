package org.example.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShardingDBConfig {

    private static  final List<String> DBPrefixList = new ArrayList<>();

    static {
        DBPrefixList.add("0");
        DBPrefixList.add("1");
    }

    private static Random random = new Random();

    public static String getRandomPrefix(){
        int index = random.nextInt(DBPrefixList.size());
        return DBPrefixList.get(index);
    }
}
