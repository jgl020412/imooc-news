package com.imooc.files.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author 小亮
 **/
public interface UploaderService {

    /**
     * 上传文件
     * @param file
     * @param fileExtName
     * @return 文件上传路径
     * @throws Exception
     */
    public String upload(MultipartFile file, String fileExtName) throws Exception;

    // TODO: 2022/7/20 使用OSS进行文件上传

}
