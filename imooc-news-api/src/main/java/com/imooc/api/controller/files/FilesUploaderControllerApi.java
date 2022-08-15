package com.imooc.api.controller.files;

import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 小亮
 **/

@Api(value = "文件上传API", tags = "文件上传api")
@RequestMapping("fs")
public interface FilesUploaderControllerApi {

    /**
     * 上传用户头像
     * @param userId
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping("/uploadFace")
    public GraceJSONResult uploadFace(@RequestParam String userId, MultipartFile file) throws Exception;


    /**
     * 上传多文件
     * @param userId
     * @param files
     * @return
     * @throws Exception
     */
    @PostMapping("/uploadSomeFiles")
    public GraceJSONResult uploadSomeFiles(@RequestParam String userId, MultipartFile[] files) throws Exception;

    /**
     * 上传人脸信息
     * @param newAdminBO
     * @return
     * @throws Exception
     */
    @PostMapping("/uploadToGridFS")
    public GraceJSONResult uploadToGridFS(@RequestBody NewAdminBO newAdminBO) throws Exception;

    /**
     * 查看人脸信息
     * @param faceId
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping("/readInGridFS")
    public void readInGridFS(@RequestParam String faceId,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws Exception;

    /**
     * 根据faceId获得admin的base64人脸信息信息
     * @param faceId
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping("readFace64InGridFS")
    public GraceJSONResult readFace64InGridFS(
            @RequestParam String faceId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception;


}
