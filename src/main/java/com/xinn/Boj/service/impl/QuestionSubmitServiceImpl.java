package com.xinn.Boj.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinn.Boj.common.ErrorCode;
import com.xinn.Boj.constant.CommonConstant;
import com.xinn.Boj.exception.BusinessException;
import com.xinn.Boj.model.dto.question.QuestionQueryRequest;
import com.xinn.Boj.model.dto.questionsubmit.JudgeInfo;
import com.xinn.Boj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.xinn.Boj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.xinn.Boj.model.entity.Question;
import com.xinn.Boj.model.entity.QuestionSubmit;
import com.xinn.Boj.model.entity.User;
import com.xinn.Boj.model.enums.QuestionSubmitLanguageEnum;
import com.xinn.Boj.model.enums.QuestionSubmitStatusEnum;
import com.xinn.Boj.model.vo.QuestionSubmitVO;
import com.xinn.Boj.model.vo.QuestionVO;
import com.xinn.Boj.model.vo.UserVO;
import com.xinn.Boj.service.QuestionService;
import com.xinn.Boj.service.QuestionSubmitService;
import com.xinn.Boj.service.UserService;
import com.xinn.Boj.service.impl.mapper.QuestionSubmitMapper;
import com.xinn.Boj.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Resource
    private UserService userService;

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
        JudgeInfo judgeInfo = new JudgeInfo();
        questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
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

    /**
     * 获取查询包装类
     * 根据前端传入的参数创建对应的Mybatis plus 查询类QueryWrapper
     *
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long userId = questionSubmitQueryRequest.getUserId();
        Integer questionId = questionSubmitQueryRequest.getQuestionId();

        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();


        // 拼接查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId),"questionId", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status),"status", status);

        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        System.out.println(queryWrapper);
        return queryWrapper;
    }


    /**
     * 脱敏原始题目提交记录得到VO类
     * @return
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User user) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        long userId = user.getId();
        //只有管理员和用户本人才能看到代码
        if(userId!=questionSubmit.getUserId() && !userService.isAdmin(user)){
            questionSubmitVO.setCode(null);
        }
        System.out.println("Log:问题id"+questionSubmitVO.getQuestionId());

        // 关联查询用户信息
        Long submitUserId = questionSubmit.getUserId();
        User submitUser = null;
        if (submitUserId > 0) {
            submitUser = userService.getById(submitUserId);
        }
        UserVO userVO = userService.getUserVO(submitUser);
        questionSubmitVO.setUserVO(userVO);

        return questionSubmitVO;
    }

    /**
     * 分页获取题目提交记录的视图
     *

     * @return 返回填充了用户信息的问题详情分页对象。
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User user) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        // 创建问题详情的分页对象，复制问题分页的当前页、页大小和总记录数
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        // 如果问题列表为空，则直接返回空的详情分页对象
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }

        //对每个对象都进行分别脱敏
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
                .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, user))
                .collect(Collectors.toList());

        questionSubmitVOPage.setRecords(questionSubmitVOList);

        return questionSubmitVOPage;
    }
}




