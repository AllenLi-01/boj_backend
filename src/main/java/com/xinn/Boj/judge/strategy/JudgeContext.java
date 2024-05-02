package com.xinn.Boj.judge.strategy;

import com.xinn.Boj.model.dto.question.JudgeCase;
import com.xinn.Boj.model.dto.questionsubmit.JudgeInfo;
import com.xinn.Boj.model.entity.Question;
import com.xinn.Boj.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

@Data
public class JudgeContext {
    private List<String> outputList;
    private List<String> inputList;
    private List<JudgeCase> judgeCaseList;
    private JudgeInfo judgeInfo;
    private Question question;
    private QuestionSubmit questionSubmit;
}
