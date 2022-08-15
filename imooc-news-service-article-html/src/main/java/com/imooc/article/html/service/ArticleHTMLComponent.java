package com.imooc.article.html.service;

import com.mongodb.client.gridfs.GridFSBucket;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * @author 小亮
 **/

@Component
public class ArticleHTMLComponent {

    @Autowired
    private GridFSBucket gridFSBucket;


    @Value("${freemarker.html.article}")
    private String articlePath;

    public Integer download(String articleId, String articleMongoId) throws Exception {
        // 拼接文件全路径，并创建输出流
        String path = articlePath + File.separator + articleId + ".html";
        File file = new File(path);
        OutputStream outputStream = new FileOutputStream(file);

        // 从GridFS中下载文件到指定输出流
        gridFSBucket.downloadToStream(new ObjectId(articleMongoId), outputStream);
        outputStream.close();

        // 返回一个成功的状态码
        return HttpStatus.OK.value();
    }

    public Integer delete(String articleId) throws Exception {
        // 拼接文件全路径，创建对象并删除
        String path = articlePath + File.separator + articleId + ".html";
        File file = new File(path);
        file.delete();

        // 返回一个成功的状态码
        return HttpStatus.OK.value();
    }
}
