package com.ankang.contract.service;

import com.ankang.contract.dto.UploadSession;

/**
 * 手机扫码上传会话服务
 */
public interface UploadSessionService {

    /**
     * 创建上传会话
     * @param contractNo 合同编号
     * @param uploadBy 上传人
     * @return 上传会话信息（含短时效token）
     */
    UploadSession createSession(String contractNo, String uploadBy);

    /**
     * 校验并获取会话
     * @param token 会话token
     * @return 会话信息，token无效或已过期时返回null
     */
    UploadSession getSession(String token);

    /**
     * 删除会话
     * @param token 会话token
     */
    void removeSession(String token);
}
