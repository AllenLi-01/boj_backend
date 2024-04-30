package com.xinn.Boj.judge.codesandbox.impl;

import com.xinn.Boj.judge.codesandbox.CodeSandBox;
import com.xinn.Boj.judge.codesandbox.model.ExecuteCodeRequest;
import com.xinn.Boj.judge.codesandbox.model.ExecuteCodeResponse;
import com.xinn.Boj.model.dto.questionsubmit.JudgeInfo;
import com.xinn.Boj.model.enums.JudgeInfoMessageEnum;
import com.xinn.Boj.model.enums.QuestionSubmitStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 示例代码沙箱
 */
public class ExampleCodeSandBox implements CodeSandBox {
    private static final Logger log = LoggerFactory.getLogger(ExampleCodeSandBox.class);

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    @Override
    public ExecuteCodeResponse execute(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(inputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.AC.getText());
        judgeInfo.setTime(100L);
        judgeInfo.setMemory(100L);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.COMPLETED.getValue());
        
        return executeCodeResponse;
    }
}
