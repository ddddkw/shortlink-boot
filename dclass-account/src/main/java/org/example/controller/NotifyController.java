package org.example.controller;

import org.example.service.NotifyService;
import org.example.utils.JsonData;;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notify")
public class NotifyController {

    @Autowired
    private NotifyService notifyService;

    @PostMapping("/sendSms")
    JsonData sendSms(){
        notifyService.testSend();
        return JsonData.buildSuccess();
    }
}
