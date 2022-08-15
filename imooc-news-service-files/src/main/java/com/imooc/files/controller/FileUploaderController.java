package com.imooc.files.controller;

import com.imooc.api.controller.files.FilesUploaderControllerApi;
import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.exception.GraceException;
import com.imooc.files.resource.FileResource;
import com.imooc.files.service.impl.UploaderServiceImpl;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.utils.FileUtils;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 小亮
 **/

@RestController
public class FileUploaderController implements FilesUploaderControllerApi {

    private static final Logger log = LoggerFactory.getLogger(FileUploaderController.class);

    // 文件临时下载位置
    private static final String TEMP_PATH = "/workspaces/temp_face";

    @Autowired
    private UploaderServiceImpl uploaderService;

    @Autowired
    private FileResource fileResource;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Override
    public GraceJSONResult uploadFace(String userId, MultipartFile file) throws Exception {

        // 判断用户id是否为空
        if (StringUtils.isBlank(userId)) {
            GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        String path = "";

        // 判断上传的文件是否为空
        if (file != null) {

            String fileName = file.getOriginalFilename();
            // 判断文件名不能为空
            if (StringUtils.isNotBlank(fileName)) {
                String fileNameArr[] = fileName.split("\\.");
                // 获得后缀
                String suffix = fileNameArr[fileNameArr.length - 1];
                // 判断后缀符合我们的预定义规范
                if (!suffix.equalsIgnoreCase("png") &&
                        !suffix.equalsIgnoreCase("jpg") &&
                        !suffix.equalsIgnoreCase("jpeg")
                ) {
                    return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_FORMATTER_FAILD);
                }

                // 执行上传
                path = uploaderService.upload(file, suffix);
//                path = uploaderService.uploadOSS(file, userId, suffix);

            } else {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_NULL_ERROR);
            }


        } else {
            GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_NULL_ERROR);
        }

        // 判断路径是否为空
        if (StringUtils.isBlank(path)) {
            GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        // 生成最终路径
        String finalPath = fileResource.getHost() + path;
        log.info(finalPath);

        return GraceJSONResult.ok(finalPath);
    }

    @Override
    public GraceJSONResult uploadSomeFiles(
            String userId,
            MultipartFile[] files) throws Exception {

        // 声明list，用于存放多个图片的地址路径，返回到前端
        List<String> imageUrlList = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                String path = "";
                if (file != null) {
                    // 获得文件上传的名称
                    String fileName = file.getOriginalFilename();

                    // 判断文件名不能为空
                    if (StringUtils.isNotBlank(fileName)) {
                        String fileNameArr[] = fileName.split("\\.");
                        // 获得后缀
                        String suffix = fileNameArr[fileNameArr.length - 1];
                        // 判断后缀符合我们的预定义规范
                        if (!suffix.equalsIgnoreCase("png") &&
                                !suffix.equalsIgnoreCase("jpg") &&
                                !suffix.equalsIgnoreCase("jpeg")
                        ) {
                            continue;
                        }

                        // 执行上传
                        path = uploaderService.upload(file, suffix);

                    } else {
                        continue;
                    }
                } else {
                    continue;
                }

                String finalPath = "";
                if (StringUtils.isNotBlank(path)) {
                    finalPath = fileResource.getHost() + path;
                    imageUrlList.add(finalPath);
                } else {
                    continue;
                }
            }
        }

        return GraceJSONResult.ok(imageUrlList);
    }



    @Override
    public GraceJSONResult uploadToGridFS(NewAdminBO newAdminBO) throws Exception{
        // 获取对象中的人脸信息，并创建数据流
        String img64 = newAdminBO.getImg64();
        byte[] bytes = new BASE64Decoder().decodeBuffer(img64);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        // 上传数据到mongdb，并获取文件id
        ObjectId fileId = gridFSBucket
                .uploadFromStream(newAdminBO.getUsername() + ".png", byteArrayInputStream);

        // 将文件id转换成字符串，并返回给前端
        String fileIdStr = fileId.toString();

        return GraceJSONResult.ok(fileIdStr);
    }

    @Override
    public void readInGridFS(String faceId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //判断人脸id是否为空
        if (StringUtils.isBlank(faceId) || faceId.equalsIgnoreCase("null")) {
            GraceException.display(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);
        }

        // 获取文件内容
        File file = readFileById(faceId);

        // 下载文件内容到浏览器上
        FileUtils.downloadFileByStream(response, file);
    }

    @Override
    public GraceJSONResult readFace64InGridFS(String faceId,
                                              HttpServletRequest request,
                                              HttpServletResponse response) throws Exception {
        // 判断人脸信息是否存在
        if (StringUtils.isBlank(faceId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_FACE_NULL_ERROR);
        }

        // 创建文件对象，并转换为base64信息
        File file = new File(faceId);
        String base64 = FileUtils.fileToBase64(file);

        return GraceJSONResult.ok(base64);
    }

    private File readFileById(String faceId) throws FileNotFoundException {
        // 根据id号获取相关内容
        GridFSFindIterable findIterable = gridFSBucket.find(Filters.eq("_id", new ObjectId(faceId)));
        GridFSFile file = findIterable.first();

        // 判断内容是否存在
        if (file == null) {
            GraceException.display(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);
        }

        // 定义文件名称和下载位置，并创建文件
        File downDir = new File(TEMP_PATH);
        if (!downDir.exists()) {
            downDir.mkdirs();
        }
        String fileName = TEMP_PATH + "/" + file.getFilename();
        File downFile = new File(fileName);

        // 创建输出流
        FileOutputStream fileOutputStream = new FileOutputStream(downFile);

        // 将文件下载到本地上
        gridFSBucket.downloadToStream(new ObjectId(faceId), fileOutputStream);

        return downFile;
    }

}
