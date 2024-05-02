package com.xinn.Boj.judge.strategy;

import com.xinn.Boj.model.dto.questionsubmit.JudgeInfo;

public interface JudgeStrategy {
    JudgeInfo doJudge(JudgeContext judgeContext);
}
