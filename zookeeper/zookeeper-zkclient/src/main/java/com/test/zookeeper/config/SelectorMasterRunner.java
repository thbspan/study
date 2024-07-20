package com.test.zookeeper.config;

import javax.annotation.PostConstruct;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SelectorMasterRunner implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectorMasterRunner.class);
    @Value("${zk.url}")
    private String zkUrl;

    private static final String ELECTION_PATH = "/election";
    private ZkClient zkClient;

    @PostConstruct
    public void init() {
        zkClient = new ZkClient(zkUrl, 10_000, 10_000);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //1,项目启动的时候  在zk创建临时节点
        createEphemeral();
        //2，谁能够创建成功谁就是主服务器
        //3,使用服务监听节点是否被删除，如果被删。 重新开始创建节点
        zkClient.subscribeDataChanges(ELECTION_PATH, new IZkDataListener() {
            //返回节点如果被删除后 返回通知
            public void handleDataDeleted(String arg0) throws Exception {
                //重新创建（选举）
                LOGGER.info("开始重新选举策略");
                createEphemeral();
            }

            public void handleDataChange(String dataPath, Object data) throws Exception {
                // do nothing
            }
        });
    }

    private void createEphemeral() {
        try {
            zkClient.createEphemeral(ELECTION_PATH);
            //标志位true  单个jvm共享
            ElectionMaster.IS_MASTER = true;
            LOGGER.info("选举成功，当前实例被选举为主节点");
        } catch (Exception e) {
            LOGGER.info("选举节点已经存在");
            ElectionMaster.IS_MASTER = false;
        }
    }
}
