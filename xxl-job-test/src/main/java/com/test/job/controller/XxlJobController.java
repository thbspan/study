package com.test.job.controller;

import javax.annotation.Resource;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.test.job.config.XxlJobConfig;
import com.test.job.exception.AccessTokenWrongException;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.impl.ExecutorBizImpl;
import com.xxl.job.core.biz.model.IdleBeatParam;
import com.xxl.job.core.biz.model.KillParam;
import com.xxl.job.core.biz.model.LogParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.util.GsonTool;
import com.xxl.job.core.util.ThrowableUtil;
import com.xxl.job.core.util.XxlJobRemotingUtil;

@RestController
@RequestMapping(value = "/xxl", produces = MediaType.TEXT_HTML_VALUE)
public class XxlJobController {
    @Resource
    private XxlJobConfig xxlJobConfig;
    private final ExecutorBiz executorBiz = new ExecutorBizImpl();

    @ModelAttribute
    public void checkToken(@RequestHeader(name = XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN, required = false) String accessTokenReq) {
        String accessToken = xxlJobConfig.getAccessToken();
        if (accessToken != null && accessToken.trim().length() > 0 && !accessToken.equals(accessTokenReq)) {
            throw new AccessTokenWrongException();
        }
    }

    @PostMapping("/beat")
    public String beat() {
        return GsonTool.toJson(executorBiz.beat());
    }

    @PostMapping("/idleBeat")
    public String idleBeat(@RequestBody String requestData) {
        return GsonTool.toJson(executorBiz.idleBeat(GsonTool.fromJson(requestData, IdleBeatParam.class)));
    }

    @PostMapping("/run")
    public String run(@RequestBody String requestData) {
        return GsonTool.toJson(executorBiz.run(GsonTool.fromJson(requestData, TriggerParam.class)));
    }

    @PostMapping("/kill")
    public String kill(@RequestBody String requestData) {
        return GsonTool.toJson(executorBiz.kill(GsonTool.fromJson(requestData, KillParam.class)));
    }

    @PostMapping("/log")
    public String log(@RequestBody String requestData) {
        return GsonTool.toJson(executorBiz.log(GsonTool.fromJson(requestData, LogParam.class)));
    }

    @ExceptionHandler(AccessTokenWrongException.class)
    public String handleAccessTokenWrongException() {
        return GsonTool.toJson(new ReturnT<>(ReturnT.FAIL_CODE, "The access token is wrong."));
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e) {
        return GsonTool.toJson(new ReturnT<String>(ReturnT.FAIL_CODE, "request error:" + ThrowableUtil.toString(e)));
    }
}
