package com.xinn.Boj.model.dto.question;

import lombok.Data;

/**
 * 判题配置
 *
 * @author allen
 * @date 2024/03/23
 */
@Data
public class JudgeConfig {
    /**
     * 时间限制：ms
     */
    private Long timeLimit;
    /**
     * 堆栈限制：kb
     */
    private Long stackLimit;
    /**
     * 内存限制：kb
     */
    private Long memoryLimit;
}
