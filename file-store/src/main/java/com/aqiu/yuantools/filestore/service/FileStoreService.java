package com.aqiu.yuantools.filestore.service;

import com.aliyun.oss.model.DeleteObjectsResult;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectResult;

import java.io.InputStream;
import java.util.List;

/**
 * @author: yuanyang
 * @date: 2022-12-12 16:13
 * @desc:
 */
public interface FileStoreService {

    /**
     * 文件上传
     *
     * @param corpId
     * @param bucketName
     * @param objectName
     * @param inputStream
     * @return
     */
    PutObjectResult upload(String corpId, String bucketName, String objectName, InputStream inputStream);

    /**
     * 文件下载
     * @param bucketName
     * @param objectName
     * @return 文件对象
     */
    OSSObject download(String bucketName, String objectName);

    /**
     * 文件批量删除
     * @param bucketName
     * @param objectNames
     * @return
     */
    DeleteObjectsResult batchRemove(String bucketName, List<String> objectNames);

    /**
     * 远程文件批量加密，可用于定时任务中对远程文件进行批量加密
     * @param corpId
     * @param bucketName
     * @param prefix
     */
    void remoteFileBatchEncrypt(String corpId, String bucketName, String prefix);
}
