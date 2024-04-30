package com.xinn.Boj.judge;

import cn.hutool.json.JSONUtil;
import com.xinn.Boj.common.ErrorCode;
import com.xinn.Boj.exception.BusinessException;
import com.xinn.Boj.judge.codesandbox.CodeSandBox;
import com.xinn.Boj.judge.codesandbox.CodeSandBoxFactory;
import com.xinn.Boj.judge.codesandbox.model.ExecuteCodeRequest;
import com.xinn.Boj.judge.codesandbox.model.ExecuteCodeResponse;
import com.xinn.Boj.model.dto.question.JudgeCase;
import com.xinn.Boj.model.entity.Question;
import com.xinn.Boj.model.entity.QuestionSubmit;
import com.xinn.Boj.model.enums.QuestionSubmitLanguageEnum;
import com.xinn.Boj.model.enums.QuestionSubmitStatusEnum;
import com.xinn.Boj.model.vo.QuestionSubmitVO;
import com.xinn.Boj.service.QuestionService;
import com.xinn.Boj.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {
    @Resource
    QuestionService questionService;

    @Resource
    QuestionSubmitService questionSubmitService;
    @Value("${codesandbox.type:example}")
    private String type;
    /**
     * 判题
     *
     * @param questionSubmitId
     * @return
     */
    @Override
    public QuestionSubmitVO doJudge(long questionSubmitId) {
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
        //如果题目不在待判题状态，则不执行判题,防止同一提交记录重复执行
        if(!Objects.equals(status, QuestionSubmitStatusEnum.WAITING.getValue())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"判题正在执行中");
        }

        //更改判题状态
        QuestionSubmit questionUpdate = new QuestionSubmit();
        questionUpdate.setId(questionSubmitId);
        questionUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionUpdate);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"题目状态更新错误！");
        }


        String code = questionSubmit.getCode();
        String language = questionSubmit.getLanguage();


        CodeSandBox codeSandBox = CodeSandBoxFactory.newInstance(type);
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCase = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCase.stream().map(JudgeCase::getInput).collect(Collectors.toList());

        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandBox.execute(executeCodeRequest);
        //todo 判题结果判定


        Long id = questionSubmit.getId();
        String judgeInfo = questionSubmit.getJudgeInfo();

        Long userId = questionSubmit.getUserId();
        Date submitTime = questionSubmit.getSubmitTime();
        Integer isDelete = questionSubmit.getIsDelete();

        return null;
    }
}
