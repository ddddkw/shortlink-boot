package org.example.params;

import lombok.Data;

@Data
public class SendCodeRequestParam {

    private String captcha;

    private String to;
}

