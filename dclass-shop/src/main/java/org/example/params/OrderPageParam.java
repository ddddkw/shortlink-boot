package org.example.params;

import lombok.Data;

@Data
public class OrderPageParam {

    private int page;

    private int size;

    private String state;

}
