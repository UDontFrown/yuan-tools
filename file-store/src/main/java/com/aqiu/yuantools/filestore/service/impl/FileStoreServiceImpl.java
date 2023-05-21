package com.aqiu.yuantools.filestore.service.impl;

import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSEncryptionClient;
import com.aliyun.oss.model.*;
import com.aqiu.yuantools.filestore.service.EncryptCheckService;
import com.aqiu.yuantools.filestore.service.FileStoreService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: yuanyang
 * @date: 2022-12-12 16:15
 * @desc:
 */
@Service
public class FileStoreServiceImpl implements FileStoreService {

    private final Logger logger = LoggerFactory.getLogger(FileStoreServiceImpl.class);
    /**
     * 加密模式标志位
     */
    private static final String ENCRYPT_MODE = "encrypt-mode";
    /**
     * 客户端加密标识
     */
    private static final String CLIENT_SIDE_ENCRYPT = "client-side-encryption";
    /**
     * 非客户端加密标识
     */
    private static final String NOT_CLIENT_SIDE_ENCRYPT = "not-client-side-encryption";

    @Autowired
    private OSSEncryptionClient ossEncryptionClient;
    @Autowired
    private OSS ossNotEncryptionClient;
    @Autowired
    private EncryptCheckService encryptCheckService;

    @Override
    public PutObjectResult upload(String corpId, String bucketName, String objectName, InputStream inputStream) {
        if (StringUtils.isAnyBlank(bucketName, objectName) || Objects.isNull(inputStream)) {
            logger.error("上传时非法参数, bucketName={}, objectName={}", bucketName, objectName);
            return null;
        }
        try {
            // 上传
            logger.info("文件开始上传, bucketName={}, objectName={}", bucketName, objectName);
            StopWatch stopWatch = new StopWatch("文件上传");
            stopWatch.start();
            // 设置用户自定义元信息
            Boolean openingClientSideEncrypt = encryptCheckService.isOpeningClientSideEncrypt(corpId);
            ObjectMetadata userMetaData = new ObjectMetadata();
            Map<String, String> userMetaDataMap = new HashMap<>(1);
            userMetaDataMap.put(ENCRYPT_MODE, Objects.equals(Boolean.TRUE, openingClientSideEncrypt)?
                    CLIENT_SIDE_ENCRYPT : NOT_CLIENT_SIDE_ENCRYPT);
            userMetaData.setUserMetadata(userMetaDataMap);
            // 上传文件
            PutObjectResult putObjectResult = Objects.equals(Boolean.TRUE, openingClientSideEncrypt) ?
                    ossEncryptionClient.putObject(bucketName, objectName, inputStream, userMetaData) :
                    ossNotEncryptionClient.putObject(bucketName, objectName, inputStream, userMetaData);
            if (Objects.isNull(putObjectResult)) {
                logger.error("文件上传失败, bucketName={}, objectName={}", bucketName, objectName);
                return null;
            }
            stopWatch.stop();
            logger.info("文件上传成功, bucketName={}, objectName={}， putObjectResult={}, running-time={}ms", bucketName, objectName, JSON.toJSONString(putObjectResult), stopWatch.getTotalTimeMillis());
            return putObjectResult;
        } catch (Exception e) {
            logger.error("文件上传异常, bucketName={}, objectName={}", bucketName, objectName, e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("文件上传时文件流关闭失败, corpId={}, bucketName={}, objectName={}", corpId, bucketName, objectName, e);
            }
        }
        return null;
    }

    @Override
    public OSSObject download(String bucketName, String objectName) {
        if (StringUtils.isAnyBlank(bucketName, objectName)) {
            logger.error("下载时非法参数, bucketName={}, objectName={}", bucketName, objectName);
            return null;
        }
        try {
            if (!ossNotEncryptionClient.doesObjectExist(bucketName, objectName)) {
                logger.error("文件不存在, bucketName={}, objectName={}", bucketName, objectName);
                return null;
            }
            ObjectMetadata objectMetadata = ossNotEncryptionClient.getObjectMetadata(bucketName, objectName);
            if (Objects.isNull(objectMetadata)) {
                logger.error("获取文件元信息失败, bucketName={}, objectName={}", bucketName, objectName);
                return null;
            }
            // 加密后上传
            logger.info("文件开始下载, bucketName={}, objectName={}", bucketName, objectName);
            StopWatch stopWatch = new StopWatch("文件下载");
            stopWatch.start();
            // 如果有客户端加密标识则使用加密client下载,否则使用默认client
            OSSObject ossObject = Objects.equals(CLIENT_SIDE_ENCRYPT, objectMetadata.getUserMetadata().get(ENCRYPT_MODE)) ?
                    ossEncryptionClient.getObject(bucketName, objectName) :
                    ossNotEncryptionClient.getObject(bucketName, objectName);
            stopWatch.stop();
            if (Objects.isNull(ossObject)) {
                logger.error("文件下载失败, bucketName={}, objectName={}", bucketName, objectName);
                return null;
            }
            logger.info("文件下载成功, bucketName={}, objectName={}, running-time={}ms", bucketName, objectName, stopWatch.getTotalTimeMillis());
            return ossObject;
        } catch (Exception e) {
            logger.error("文件下载异常, bucketName={}, objectName={}", bucketName, objectName, e);
        }
        return null;
    }

    @Override
    public DeleteObjectsResult batchRemove(String bucketName, List<String> objectNames) {
        if (StringUtils.isBlank(bucketName) || CollectionUtils.isEmpty(objectNames)) {
            logger.error("删除时非法参数, bucketName={}, objectNames={}", bucketName, objectNames);
            return null;
        }
        try {
            // 加密后上传
            logger.info("文件开始删除, bucketName={}, objectNames={}", bucketName, objectNames);
            StopWatch stopWatch = new StopWatch("文件删除");
            stopWatch.start();
            DeleteObjectsResult result = ossEncryptionClient.deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(objectNames).withEncodingType("url"));
            if (Objects.isNull(result) || CollectionUtils.isEmpty(result.getDeletedObjects())) {
                logger.error("文件删除失败, bucketName={}, objectNames={}", bucketName, objectNames);
                return null;
            }
            stopWatch.stop();
            if (result.getDeletedObjects().size() == objectNames.size()) {
                logger.info("文件删除成功, bucketName={}, objectNames={}, running-time={}ms", bucketName, objectNames, stopWatch.getTotalTimeMillis());
            } else {
                logger.info("文件部分删除成功, bucketName={}, allObjectNames={}, deletedObjectNames={}, running-time={}", bucketName, objectNames, result.getDeletedObjects(), stopWatch.getTotalTimeMillis());
            }
            return result;
        } catch (Exception e) {
            logger.error("文件删除异常, bucketName={}, objectNames={}", bucketName, objectNames, e);
        }
        return null;
    }

    @Override
    public void remoteFileBatchEncrypt(String corpId, String bucketName, String prefix) {
        if (StringUtils.isBlank(corpId) || StringUtils.isBlank(bucketName)) {
            logger.error("批量加密时非法参数, corpId={}, bucketName={}", corpId, bucketName);
            return;
        }
        Boolean openingClientSideEncrypt = encryptCheckService.isOpeningClientSideEncrypt(corpId);
        if (!Objects.equals(Boolean.TRUE, openingClientSideEncrypt)) {
            logger.info("未开启文件加密, corpId={}", corpId);
            return;
        }
        // 设置每页列举200个文件。
        int maxKeys = 200;
        StopWatch stopWatch = new StopWatch("远程文件批量加密, corpId=" + corpId);
        try {
            String nextMarker = null;
            ObjectListing objectListing;
            stopWatch.start();
            do {
                // 设置筛选条件
                objectListing = ossNotEncryptionClient.listObjects(new ListObjectsRequest(bucketName)
                        .withMarker(nextMarker).withMaxKeys(maxKeys).withPrefix(prefix));
                List<OSSObjectSummary> summaryList = objectListing.getObjectSummaries();
                for (OSSObjectSummary summary : summaryList) {
                    // 拿到文件名
                    String objectName = summary.getKey();
                    // 获取文件元信息
                    ObjectMetadata objectMetadata = ossNotEncryptionClient.getObjectMetadata(bucketName, objectName);
                    if (Objects.isNull(objectMetadata)) {
                        logger.error("批量加密时获取文件元信息失败, bucketName={}, objectName={}", bucketName, objectName);
                        continue;
                    }
                    // 如果是客户端加密的则跳过
                    if (Objects.equals(CLIENT_SIDE_ENCRYPT, objectMetadata.getUserMetadata().get(ENCRYPT_MODE))) {
                        continue;
                    }
                    // 否则用普通客户端下载
                    OSSObject object = ossNotEncryptionClient.getObject(bucketName, objectName);
                    if (Objects.isNull(object)) {
                        logger.error("批量加密时下载文件失败, bucketName={}, objectName={}", bucketName, objectName);
                        continue;
                    }
                    // 设置用户自定义元信息
                    ObjectMetadata userMetaData = new ObjectMetadata();
                    Map<String, String> userMetaDataMap = new HashMap<>(1);
                    userMetaDataMap.put(ENCRYPT_MODE, CLIENT_SIDE_ENCRYPT);
                    userMetaData.setUserMetadata(userMetaDataMap);
                    // 再进行加密上传至OSS
                    try (InputStream inputStream = object.getObjectContent()) {
                        PutObjectResult putObjectResult = ossEncryptionClient.putObject(bucketName, objectName,
                                inputStream, userMetaData);
                        if (Objects.isNull(putObjectResult)) {
                            logger.error("批量加密时文件加密后上传失败, bucketName={}, objectName={}", bucketName, objectName);
                        }
                    } catch (Exception e) {
                        logger.error("批量加密时文件加密后上传异常, bucketName={}, objectName={}", bucketName, objectName);
                    }
                }
                nextMarker = objectListing.getNextMarker();
            } while (objectListing.isTruncated());
            stopWatch.stop();
        } catch (Exception e) {
            logger.error("批量加密时文件上传异常, corpId={}, bucketName={}", corpId, bucketName, e);
        }
        logger.info("远程文件批量加密成功, corpId={}, bucketName={}, running-time={}ms", corpId, bucketName, stopWatch.getLastTaskTimeMillis());
    }
}
