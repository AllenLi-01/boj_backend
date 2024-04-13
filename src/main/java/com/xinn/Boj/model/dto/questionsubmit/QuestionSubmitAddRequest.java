package com.xinn.Boj.model.dto.questionsubmit;

import lombok.Data;

import java.io.Serializable;

/**
 * 题目提交请求
 *
 * @author Xinn Li
 *  
 */
@Data
public class QuestionSubmitAddRequest implements Serializable {

    /**
     * 语言
     */
    private String language;

    /**
     * 代码
     */
    private String code;



    /**
     * 题号
     */
    private Integer questionId;

    private static final long serialVersionUID = 1L;

}