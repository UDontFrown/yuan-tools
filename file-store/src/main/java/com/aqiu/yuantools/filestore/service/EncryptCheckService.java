package com.aqiu.yuantools.filestore.service;

import java.util.List;

/**
 * @author: yuanyang
 * @date: 2022-12-14 17:19
 * @desc:
 */
public abstract class EncryptCheckService {

    /**
     * 客户端加密开关
     */
    private Boolean clientSideEncrypt;

    /**
     * 特殊企业，不做处理
     */
    private List<String> skipCorpIdList;

    /**
     * 客户端加密开关是否开启
     *
     * 1、可以直接返回clientSideEncrypt
     * 2、可以结合corpId进行判断
     * @param corpId
     * @return
     */
    public abstract Boolean isOpeningClientSideEncrypt(String corpId);

    /**
     * 获取客户端加密配置
     * @return
     */
    public Boolean getClientSideEncrypt() {
        return clientSideEncrypt;
    }

    /**
     * 更新客户端加密配置
     * @param clientSideEncrypt
     */
    public void setClientSideEncrypt(Boolean clientSideEncrypt) {
        this.clientSideEncrypt = clientSideEncrypt;
    }

    /**
     * 获取企业列表
     * @return
     */
    public List<String> getSkipCorpIdList() {
        return skipCorpIdList;
    }

    /**
     * 更新企业列表
     * @param skipCorpIdList
     */
    public void setSkipCorpIdList(List<String> skipCorpIdList) {
        this.skipCorpIdList = skipCorpIdList;
    }
}
