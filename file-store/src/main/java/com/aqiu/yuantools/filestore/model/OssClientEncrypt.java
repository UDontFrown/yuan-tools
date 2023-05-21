package com.aqiu.yuantools.filestore.model;

import java.util.List;

/**
 * @author: yuanyang
 * @date: 2022-12-14 16:53
 * @desc:
 */
public class OssClientEncrypt {
    /**
     * 是否开启客户端加密
     */
    private Boolean openClientEncrypt;
    /**
     * 特殊企业，不做处理
     */
    private List<String> skipCorpIdList;

    public Boolean getOpenClientEncrypt() {
        return openClientEncrypt;
    }

    public void setOpenClientEncrypt(Boolean openClientEncrypt) {
        this.openClientEncrypt = openClientEncrypt;
    }

    public List<String> getSkipCorpIdList() {
        return skipCorpIdList;
    }

    public void setSkipCorpIdList(List<String> skipCorpIdList) {
        this.skipCorpIdList = skipCorpIdList;
    }
}
