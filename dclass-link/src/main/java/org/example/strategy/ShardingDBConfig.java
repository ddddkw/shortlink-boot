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

    public static String getRandomPrefix(String code){
        int hashCode = code.hashCode();
        int index = Math.abs(hashCode) % DBPrefixList.size();
        return DBPrefixList.get(index);
    }
}
