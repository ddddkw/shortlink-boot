package org.example.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.example.config.OssConfig;
import org.example.service.FileService;
import org.example.utils.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Autowired
    private OssConfig ossConfig;

    @Override
    public String uploadUserImage(MultipartFile file){
        String bucketName = ossConfig.getBucketname();
        String endpoint = ossConfig.getEndpoint();
        String accessKeySecret = ossConfig.getAccessKeySecret();
        String accessKeyId = ossConfig.getAccessKeyId();

        // OSS客户端构建
        OSS ossClinet = new OSSClientBuilder().build(endpoint,accessKeyId,accessKeySecret);
        String originalFilename = file.getOriginalFilename();

        LocalDateTime ldt =LocalDateTime.now();
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        // user/年份/月份/日期-文件进行归档

        String folder = pattern.format(ldt);
        // 使用uuid做文件名
        String fileName = CommonUtil.generateUUID();
        String extendsion = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 在oss的bucket创建文件夹
        String newFileName = "user/"+folder+"/"+fileName+extendsion;

        try {
           PutObjectResult result = ossClinet.putObject(bucketName,newFileName,file.getInputStream());
           if (result!=null) {
               String imgUrl = "https://"+bucketName+"."+endpoint+"/"+newFileName;
               return imgUrl;
           }
        } catch (Exception e){
            log.error(e.getMessage());
        } finally {
            ossClinet.shutdown();
        }

        return null;
    }
}
