package com.imooc.files.service.impl;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.imooc.files.service.UploaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * @author 小亮
 **/

@Service
public class UploaderServiceImpl implements UploaderService {

    @Autowired
    private FastFileStorageClient fileStorageClient;

    @Override
    public String upload(MultipartFile file, String fileExtName) throws Exception {
        // 上传文件
        InputStream inputStream = file.getInputStream();
        StorePath storePath = fileStorageClient.uploadFile(inputStream,
                file.getSize(), fileExtName, null);
        inputStream.close();

        // 获取上传文件路径
        String fullPath = storePath.getFullPath();

        // 返回文件上传路径
        return fullPath;
    }
}
