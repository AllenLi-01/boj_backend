package com.xinn.Boj.judge.codesandbox.model;

import com.xinn.Boj.model.dto.questionsubmit.JudgeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCodeResponse {
    private List<String> outputList;
    private JudgeInfo judgeInfo;
    private Integer status;
}
