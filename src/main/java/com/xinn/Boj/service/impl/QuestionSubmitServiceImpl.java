package com.xinn.Boj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinn.Boj.common.ErrorCode;
import com.xinn.Boj.exception.BusinessException;
import com.xinn.Boj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.xinn.Boj.model.entity.Question;
import com.xinn.Boj.model.entity.QuestionSubmit;
import com.xinn.Boj.model.entity.User;
import com.xinn.Boj.model.enums.QuestionSubmitLanguageEnum;
import com.xinn.Boj.model.enums.QuestionSubmitStatusEnum;
import com.xinn.Boj.service.QuestionService;
import com.xinn.Boj.service.QuestionSubmitService;
import com.xinn.Boj.service.impl.mapper.QuestionSubmitMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author allen
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2024-03-23 12:59:49
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {
    @Resource
    private QuestionService questionService;

    /**
     * 提交题目答案的功能实现。
     *
     * @param questionSubmitAddRequest 提交的题目ID。
     * @param loginUser                登录的用户信息。
     * @return 返回题目提交的结果。
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 根据题目ID获取题目实体
        //todo 判断语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编程语言不在支持范围内");
        }

        String code = questionSubmitAddRequest.getCode();
        int questionId = questionSubmitAddRequest.getQuestionId();

        Question question = questionService.getById(questionId);
        // 判断题目实体是否存在，若不存在则抛出业务异常
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 获取当前登录用户的ID
        long userId = loginUser.getId();
        //todo 限流 保证用户只能同时提交一条记录

        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setLanguage(language);
        questionSubmit.setCode(code);

        //todo 设置初始状态
        questionSubmit.setJudgeInfo("{}");
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败!");
        }
        return questionSubmit.getId();


    }


    /**
     * 封装了事务的方法
     *
     * @param userId
     * @param postId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doQuestionSubmitInner(long userId, int questionId) {
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        QueryWrapper<QuestionSubmit> thumbQueryWrapper = new QueryWrapper<>(questionSubmit);
        QuestionSubmit oldQuestionSubmit = this.getOne(thumbQueryWrapper);
        boolean result;
        // 已题目提交
        if (oldQuestionSubmit != null) {
            result = this.remove(thumbQueryWrapper);
            if (result) {
                // 题目提交数 - 1
                result = questionService.update()
                        .eq("id", questionId)
                        .gt("thumbNum", 0)
                        .setSql("thumbNum = thumbNum - 1")
                        .update();
                return result ? -1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            // 未题目提交
            result = this.save(questionSubmit);
            if (result) {
                // 题目提交数 + 1
                result = questionService.update()
                        .eq("id", questionId)
                        .setSql("thumbNum = thumbNum + 1")
                        .update();
                return result ? 1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }
}




