package com.xinn.Boj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.xinn.Boj.common.ErrorCode;
import com.xinn.Boj.constant.CommonConstant;
import com.xinn.Boj.exception.BusinessException;
import com.xinn.Boj.exception.ThrowUtils;
import com.xinn.Boj.model.dto.question.QuestionQueryRequest;
import com.xinn.Boj.model.entity.Question;
import com.xinn.Boj.model.entity.User;
import com.xinn.Boj.model.vo.QuestionVO;
import com.xinn.Boj.model.vo.UserVO;
import com.xinn.Boj.service.QuestionService;
import com.xinn.Boj.service.UserService;
import com.xinn.Boj.service.impl.mapper.QuestionMapper;
import com.xinn.Boj.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author allen
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2024-03-23 12:58:03
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService{
    private final static Gson GSON = new Gson();

    @Resource
    private UserService userService;


    /**
     * 校验题目合法性
     *
     * @param add 是否处于创建阶段
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        String answer = question.getAnswer();

        String judgeCase = question.getJudgeCase();
        String judgeConfig = question.getJudgeConfig();

        // 处于创建时，以下参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags,judgeConfig,judgeCase), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 10000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
        if (StringUtils.isNotBlank(answer) && answer.length() > 10000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题解过长");
        }
        if (StringUtils.isNotBlank(judgeCase) && judgeCase.length() > 10000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用例过长");
        }
        if (StringUtils.isNotBlank(judgeConfig) && judgeConfig.length() > 10000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "配置过长");
        }
    }

    /**
     * 获取查询包装类
     * 根据前端传入的参数创建对应的Mybatis plus 查询类QueryWrapper
     *
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        Integer id = questionQueryRequest.getId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        List<String> tags = questionQueryRequest.getTags();
        Long userId = questionQueryRequest.getUserId();


        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();

        // 拼接查询条件

        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollectionUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    /**
     * 关联查询
     * @param question
     * @param request
     * @return
     */
    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        QuestionVO questionVO = QuestionVO.objToVo(question);
        long questionId = question.getId();
        // 1. 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUserVO(userVO);
        // 2. todo 已登录，获取用户点赞、收藏列表
//        User loginUser = userService.getLoginUserPermitNull(request);
//        if (loginUser != null) {
//            // 获取点赞
//            QueryWrapper<QuestionThumb> questionThumbQueryWrapper = new QueryWrapper<>();
//            questionThumbQueryWrapper.in("questionId", questionId);
//            questionThumbQueryWrapper.eq("userId", loginUser.getId());
//            QuestionThumb questionThumb = questionThumbMapper.selectOne(questionThumbQueryWrapper);
//            questionVO.setHasThumb(questionThumb != null);
//            // 获取收藏
//            QueryWrapper<QuestionFavour> questionFavourQueryWrapper = new QueryWrapper<>();
//            questionFavourQueryWrapper.in("questionId", questionId);
//            questionFavourQueryWrapper.eq("userId", loginUser.getId());
//            QuestionFavour questionFavour = questionFavourMapper.selectOne(questionFavourQueryWrapper);
//            questionVO.setHasFavour(questionFavour != null);
//        }
        return questionVO;
    }

    /**
     * 获取问题页面，包含问题的详细信息。
     *
     * @param questionPage 分页查询问题的结果，包含当前页、页大小和总记录数。
     * @param request HTTP请求对象，用于解析请求信息（本方法未使用，可根据实际需要添加）。
     * @return 返回填充了用户信息的问题详情分页对象。
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        // 创建问题详情的分页对象，复制问题分页的当前页、页大小和总记录数
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        // 如果问题列表为空，则直接返回空的详情分页对象
        if (CollectionUtils.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 1. 查询与问题相关联的用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        // 根据用户ID集合，查询用户信息，并按ID分组
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充每个问题的详细信息，包括用户信息
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            QuestionVO questionVO = QuestionVO.objToVo(question);
            Long userId = question.getUserId();
            User user = null;
            // 如果用户ID存在于用户信息集合中，则获取其详细信息
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            // 为问题详情填充用户详情
            questionVO.setUserVO(userService.getUserVO(user));
            return questionVO;
        }).collect(Collectors.toList());
        // 设置填充了详细信息的问题列表到分页对象
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }

}




