package com.xinn.Boj.judge.codesandbox.impl;

import com.xinn.Boj.judge.codesandbox.CodeSandBox;
import com.xinn.Boj.judge.codesandbox.model.ExecuteCodeRequest;
import com.xinn.Boj.judge.codesandbox.model.ExecuteCodeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 远程代码沙箱
 */
public class RemoteCodeSandBox implements CodeSandBox {
    private static final Logger log = LoggerFactory.getLogger(RemoteCodeSandBox.class);

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    @Override
    public ExecuteCodeResponse execute(ExecuteCodeRequest executeCodeRequest) {
        log.info("远程代码沙箱");
        return null;
    }
}
