package com.test.zookeeper.controller;

import com.test.zookeeper.config.ElectionMaster;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {
    @Value("${server.port}")
    private String serverPort;

    @Value("${server.port}")
    private String port;

    @RequestMapping("getServerInfo")
    public String getServerInfo() {
        return "serverPort:" + serverPort + " " + port + " " + (ElectionMaster.IS_MASTER ? "选举为主" : "选举为从");
    }
}
