package com.xinn.Boj.judge;

import com.xinn.Boj.judge.codesandbox.model.ExecuteCodeRequest;
import com.xinn.Boj.judge.codesandbox.model.ExecuteCodeResponse;
import com.xinn.Boj.model.vo.QuestionSubmitVO;

/**
 * 判题服务
 */
public interface JudgeService {
    /**
     * 判题
     * @param questionSubmitId
     * @return
     */
    QuestionSubmitVO doJudge(long questionSubmitId);
}
