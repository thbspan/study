package com.test.arthas.instrument;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.test.arthas.instrument.collector.CostTimeCollector;
import com.test.arthas.instrument.collector.WatchCollector;
import com.test.arthas.instrument.transformer.CostTimeTransformer;
import com.test.arthas.instrument.transformer.WatchReTransformer;

import io.javalin.Javalin;
import io.javalin.plugin.json.JavalinJson;

/**
 * VirtualMachine 依赖jdk目录下的 tools.jar
 * 实现参考 https://zhuanlan.zhihu.com/p/317699113
 */
public class JavaAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaAgent.class);
    public static Instrumentation inst;

    public static void premain(String agentArgs, Instrumentation inst) {
        LOGGER.info("Pre-Main called , args :{}", agentArgs);
        initAgent(agentArgs, inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        LOGGER.info("Agent-Main called , args :{}", agentArgs);
        initAgent(agentArgs, inst);
    }

    private static void initAgent(String agentArgs, Instrumentation inst) {
        JavaAgent.inst = inst;

        inst.addTransformer(new CostTimeTransformer());

        Javalin javalin = Javalin.create(
                config -> {
                    config.defaultContentType = "application/json";
                    config.autogenerateEtags = true;
                    config.addStaticFiles("/public");
                    config.asyncRequestTimeout = 10_000L;
                    config.enforceSsl = false;
                })
                // 默认7000端口
                .start();

        javalin.get("/", ctx -> ctx.result("Hello Java Agent!!!"));

        javalin.get("/jps", ctx -> {
            List<VirtualMachineDescriptor> list = VirtualMachine.list();
            List<Map<Object, Object>> jvmList = list.stream().map(virtualMachineDescriptor -> {
                Map<Object, Object> jvmMap = new LinkedHashMap<>(4);
                jvmMap.put("pid", virtualMachineDescriptor.id());
                jvmMap.put("displayName", virtualMachineDescriptor.displayName());
                jvmMap.put("provider", virtualMachineDescriptor.provider());
                return jvmMap;
            }).collect(Collectors.toList());
            ctx.result(JavalinJson.toJson(jvmList));
        });

        javalin.ws("/ws/watch/consumer", wsHandler -> {
            wsHandler.onConnect(ctx -> LOGGER.info("websocket connected. sessionId={}", ctx.getSessionId()));
            ConcurrentMap<String, WatchReTransformer> watchReTransformerMap = new ConcurrentHashMap<>();
            wsHandler.onMessage(ctx -> watchReTransformerMap.computeIfAbsent(ctx.getSessionId(), ctxSessionId -> {
                WatchReTransformer watchReTransformer = new WatchReTransformer();
                watchReTransformer.setWatch(true);
                watchReTransformer.setSessionId(ctxSessionId);
                String exampleClassName = ctx.message();
                LOGGER.info("watching class {}", exampleClassName);
                watchReTransformer.setWatchClassName(exampleClassName);
                inst.addTransformer(watchReTransformer, true);
                try {
                    inst.retransformClasses(Class.forName(exampleClassName));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Can not class " + exampleClassName, e);
                }
                WatchCollector.addListener(exampleClassName, ((key, advice) -> {
                    if (!key.equals(ctxSessionId)) {
                        return;
                    }
                    Map<String, Object> result = new HashMap<>(8);
                    result.put("clazz", advice.getClazz());
                    result.put("target", Objects.toString(advice.getTarget()));
                    result.put("params", Arrays.toString(advice.getParams()));
                    result.put("returnObj", Objects.toString(advice.getReturnObj()));
                    result.put("isThrow", advice.getThrow());
                    ctx.send(JavalinJson.toJson(result));
                }));
                return watchReTransformer;
            }));

            wsHandler.onClose(ctx -> {
                String ctxSessionId = ctx.getSessionId();
                LOGGER.info("websocket {} closed", ctxSessionId);
                WatchReTransformer watchReTransformer = watchReTransformerMap.get(ctxSessionId);
                if (watchReTransformer != null) {
                    watchReTransformer.setWatch(false);
                    inst.removeTransformer(watchReTransformer);
                    WatchCollector.removeListener(watchReTransformer.getWatchClassName());
                }
            });
        });

        javalin.get("/summaries/latency", ctx -> ctx.result(JavalinJson.toJson(CostTimeCollector.getCostTime())));

        javalin.get("/stop", ctx -> javalin.stop());
    }
}
