package com.xinn.Boj.model.dto.questionsubmit;

import com.xinn.Boj.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 题目查询请求
 *
 * @author Xinn Li
 *  
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionSubmitQueryRequest extends PageRequest implements Serializable {

    /**
     * 语言
     */
    private String language;
    private Integer status;
    private Long userId;
    /**
     * 题号
     */
    private Integer questionId;

    private static final long serialVersionUID = 1L;

}