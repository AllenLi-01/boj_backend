package com.xinn.Boj.judge.strategy;

import com.xinn.Boj.judge.codesandbox.model.JudgeInfo;

public interface JudgeStrategy {
    JudgeInfo doJudge(JudgeContext judgeContext);
}
