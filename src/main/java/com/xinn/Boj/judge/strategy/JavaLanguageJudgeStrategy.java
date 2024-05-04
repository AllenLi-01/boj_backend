package com.xinn.Boj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.xinn.Boj.model.dto.question.JudgeCase;
import com.xinn.Boj.model.dto.question.JudgeConfig;
import com.xinn.Boj.judge.codesandbox.model.JudgeInfo;
import com.xinn.Boj.model.entity.Question;
import com.xinn.Boj.model.enums.JudgeInfoMessageEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * java判题策略
 */
@Slf4j
public class JavaLanguageJudgeStrategy implements JudgeStrategy {
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        List<String> outputList = judgeContext.getOutputList();
        List<String> inputList = judgeContext.getInputList();
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();
        Question question = judgeContext.getQuestion();
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();

        Long time = judgeInfo.getTime();
        Long memory = judgeInfo.getMemory();
        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setTime(time);
        judgeInfoResponse.setMemory(memory);

        JudgeInfoMessageEnum judgeInfoMessage = JudgeInfoMessageEnum.AC;

        if(outputList.size() != inputList.size()){
            judgeInfoMessage = JudgeInfoMessageEnum.WA;
            judgeInfoResponse.setMessage(judgeInfoMessage.getValue());
            return judgeInfoResponse;
        }
        //逐一比对输出列表和输出样例的值
        for(int i = 0;i<outputList.size();i++){
            JudgeCase judgeCase = judgeCaseList.get(i);
            //注意一下输入、输出用例的类型都是String
            if(!outputList.get(i).equals(judgeCase.getOutput())){
                judgeInfoMessage = JudgeInfoMessageEnum.WA;
                judgeInfoResponse.setMessage(judgeInfoMessage.getValue());
                return judgeInfoResponse;
            }
        }
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);

        //判断题目限制
        if(time > judgeConfig.getTimeLimit()){
            judgeInfoMessage = JudgeInfoMessageEnum.TLE;
            judgeInfoResponse.setMessage(judgeInfoMessage.getValue());
            return judgeInfoResponse;
        }
        if(memory > judgeConfig.getMemoryLimit()){
            judgeInfoMessage = JudgeInfoMessageEnum.MLE;
            judgeInfoResponse.setMessage(judgeInfoMessage.getValue());
            return judgeInfoResponse;
        }
        return judgeInfoResponse;
    }
}
