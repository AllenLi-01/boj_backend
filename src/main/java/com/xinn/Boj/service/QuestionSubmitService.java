package com.xinn.Boj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinn.Boj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.xinn.Boj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.xinn.Boj.model.entity.QuestionSubmit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xinn.Boj.model.entity.User;
import com.xinn.Boj.model.vo.QuestionSubmitVO;

/**
* @author allen
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2024-03-23 12:59:49
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {
    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

    /**
     * 题目提交（内部服务）
     *
     * @param userId
     * @param questionId
     * @return
     */
    int doQuestionSubmitInner(long userId, int questionId);



    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);


    /**
     * 获取题目封装
     *
     * @param questionSubmit
     * @param user
     * @return
     */
    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User user);
    /**
     * 分页获取题目封装
     *
     * @param questionSubmitPage
     * @param user
     * @return
     */
    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User user);


}
