package com.xinn.Boj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinn.Boj.annotation.AuthCheck;
import com.xinn.Boj.common.BaseResponse;
import com.xinn.Boj.common.ErrorCode;
import com.xinn.Boj.common.ResultUtils;
import com.xinn.Boj.constant.UserConstant;
import com.xinn.Boj.exception.BusinessException;
import com.xinn.Boj.model.dto.question.QuestionQueryRequest;
import com.xinn.Boj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.xinn.Boj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.xinn.Boj.model.entity.Question;
import com.xinn.Boj.model.entity.QuestionSubmit;
import com.xinn.Boj.model.entity.User;
import com.xinn.Boj.model.vo.QuestionSubmitVO;
import com.xinn.Boj.service.QuestionSubmitService;
import com.xinn.Boj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目提交接口
 *
 * @author Xinn Li
 *  
 */
@RestController
@RequestMapping("/question_submit")
@Slf4j
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserService userService;

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return 提交记录的ID
     */
    @PostMapping("/")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
            HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能提交
        final User loginUser = userService.getLoginUser(request);
        long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest,loginUser);
        return ResultUtils.success(questionSubmitId);
    }

    /**
     * 分页获取题目提交记录列表
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        //获取分页信息
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage,loginUser));
    }

}
