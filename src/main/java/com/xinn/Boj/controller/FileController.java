package com.xinn.Boj.controller;

import cn.hutool.core.io.FileUtil;
import com.xinn.Boj.common.BaseResponse;
import com.xinn.Boj.common.ErrorCode;
import com.xinn.Boj.common.ResultUtils;
import com.xinn.Boj.constant.FileConstant;
import com.xinn.Boj.exception.BusinessException;
import com.xinn.Boj.manager.CosManager;
import com.xinn.Boj.model.dto.file.UploadFileRequest;
import com.xinn.Boj.model.entity.User;
import com.xinn.Boj.model.enums.FileUploadBizEnum;
import com.xinn.Boj.service.UserService;
import java.io.File;
import java.util.Arrays;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    /**
     * 处理文件上传请求。
     *
     * @param multipartFile 上传的文件对象。
     * @param uploadFileRequest 包含上传文件请求信息的对象，如业务类型等。
     * @param request 用户的请求对象，用于获取登录用户信息。
     * @return 返回文件的可访问URL。
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
            UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        // 根据请求获取业务类型并验证
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 对文件进行合法性验证
        validFile(multipartFile, fileUploadBizEnum);

        // 获取登录用户信息
        User loginUser = userService.getLoginUser(request);

        // 生成文件存储的唯一标识和文件名
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);

        File file = null;
        try {
            // 创建临时文件并上传至云存储
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);

            // 返回文件的可访问URL
            return ResultUtils.success(FileConstant.COS_HOST + filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除上传后创建的临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }


    /**
     * 校验上传的文件是否符合指定的业务类型和大小限制。
     *
     * @param multipartFile 上传的文件对象，包含文件内容和其他相关信息
     * @param fileUploadBizEnum 文件上传的业务类型，用于区分不同场景下的文件校验规则
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 获取文件大小和文件后缀
        long fileSize = multipartFile.getSize();
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L; // 定义1M的字节单位转换

        // 根据不同的业务类型，执行不同的文件校验规则
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            // 对用户头像的文件大小进行校验
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            // 对用户头像的文件类型进行校验
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }

}
