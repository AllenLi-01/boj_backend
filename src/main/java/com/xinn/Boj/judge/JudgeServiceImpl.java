package com.xinn.Boj.judge;

import cn.hutool.json.JSONUtil;
import com.xinn.Boj.common.ErrorCode;
import com.xinn.Boj.exception.BusinessException;
import com.xinn.Boj.judge.codesandbox.CodeSandBox;
import com.xinn.Boj.judge.codesandbox.CodeSandBoxFactory;
import com.xinn.Boj.judge.codesandbox.model.ExecuteCodeRequest;
import com.xinn.Boj.judge.codesandbox.model.ExecuteCodeResponse;
import com.xinn.Boj.judge.strategy.DefaultJudgeStrategy;
import com.xinn.Boj.judge.strategy.JudgeContext;
import com.xinn.Boj.judge.strategy.JudgeManager;
import com.xinn.Boj.judge.strategy.JudgeStrategy;
import com.xinn.Boj.model.dto.question.JudgeCase;
import com.xinn.Boj.model.dto.questionsubmit.JudgeInfo;
import com.xinn.Boj.model.entity.Question;
import com.xinn.Boj.model.entity.QuestionSubmit;
import com.xinn.Boj.model.enums.JudgeInfoMessageEnum;
import com.xinn.Boj.model.enums.QuestionSubmitStatusEnum;
import com.xinn.Boj.service.QuestionService;
import com.xinn.Boj.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {
    @Resource
    QuestionService questionService;

    @Resource
    QuestionSubmitService questionSubmitService;

    @Resource
    JudgeManager judgeManager;

    //沙箱类型，从application.yaml中读取
    @Value("${codesandbox.type:example}")
    private String type;
    /**
     * 判题服务
     *
     * @param questionSubmitId
     * @return
     */
    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if(questionSubmit==null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"提交信息不存在");
        }

        Integer questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);

        if(question==null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"提交信息不存在");
        }
        Integer status = questionSubmit.getStatus();
        //如果题目提交记录不在待判题状态，则不执行判题,防止同一提交记录重复执行
        if(!Objects.equals(status, QuestionSubmitStatusEnum.WAITING.getValue())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"判题正在执行中");
        }

        //如果题目处于待判题状态，则提交代码沙箱判题。

        //先改变题目提交记录状态
        QuestionSubmit questionUpdate = new QuestionSubmit();
        questionUpdate.setId(questionSubmitId);
        questionUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionUpdate);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"题目状态更新错误！");
        }
        String code = questionSubmit.getCode();
        String language = questionSubmit.getLanguage();


        //从代码沙箱工厂中取到对应类型的沙箱实例
        CodeSandBox codeSandBox = CodeSandBoxFactory.newInstance(type);
        //注意，inputList是题目设置的测试用例，JudgeCase是一对Input和Output。
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());

        //链式构造判题请求
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();

        //向代码沙箱提交判题请求，并拿到执行结果
        ExecuteCodeResponse executeCodeResponse = codeSandBox.execute(executeCodeRequest);
        //根据沙箱执行结果，对判题结果进行判定
        JudgeInfoMessageEnum judgeInfoMessage;
        List<String> outputList = executeCodeResponse.getOutputList();
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setOutputList(outputList);
        judgeContext.setInputList(inputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);


        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);

        //修改数据库中的判题状态
        questionUpdate = new QuestionSubmit();
        questionUpdate.setId(questionSubmitId);
        questionUpdate.setStatus(QuestionSubmitStatusEnum.COMPLETED.getValue());
        questionUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionSubmitService.updateById(questionUpdate);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"题目状态更新错误！");
        }
        return questionSubmitService.getById(questionSubmitId);
    }
}
