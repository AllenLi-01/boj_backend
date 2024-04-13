package com.xinn.Boj.model.dto.question;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 *
 * @author Xinn Li
 *  
 */
@Data
public class QuestionAddRequest implements Serializable {


    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 题解
     */
    private String answer;



    /**
     * 判题用例（json数组）
     */
    private List<JudgeCase> judgeCase;

    /**
     * 判题配置（json对象）
     */
    private List<JudgeConfig> judgeConfig;


    /**
     * 创建者 id
     */
    private Long userId;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}