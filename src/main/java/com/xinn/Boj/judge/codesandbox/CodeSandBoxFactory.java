package com.xinn.Boj.judge.codesandbox;

import com.xinn.Boj.judge.codesandbox.impl.ExampleCodeSandBox;
import com.xinn.Boj.judge.codesandbox.impl.RemoteCodeSandBox;
import com.xinn.Boj.judge.codesandbox.impl.ThirdPartySandBox;

/**
 * 代码沙箱工厂
 * 根据字符串参数创建指定的代码沙箱实例
 */

public class CodeSandBoxFactory {
    public static CodeSandBox newInstance(String type){
        switch (type){
            case "example":
                return new ExampleCodeSandBox();
            case "remote":
                return new RemoteCodeSandBox();
            case "thirdParty":
                return new ThirdPartySandBox();
            default:
                return new ExampleCodeSandBox();
        }
    }
}
