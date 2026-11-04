package com.ankang.contract.service.impl;

import com.ankang.contract.dto.UploadSession;
import com.ankang.contract.service.UploadSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 手机扫码上传会话服务实现
 * 会话保存在内存中（单实例部署），10分钟未使用自动过期
 */
@Service
public class UploadSessionServiceImpl implements UploadSessionService {

    private static final Logger logger = LoggerFactory.getLogger(UploadSessionServiceImpl.class);

    private static final long SESSION_TTL_MILLIS = 10 * 60 * 1000L;

    private final Map<String, UploadSession> sessions = new ConcurrentHashMap<>();

    @Override
    public UploadSession createSession(String contractNo, String uploadBy) {
        UploadSession session = new UploadSession();
        session.setToken(UUID.randomUUID().toString().replace("-", ""));
        session.setContractNo(contractNo);
        session.setUploadBy(uploadBy);
        session.setExpireTime(System.currentTimeMillis() + SESSION_TTL_MILLIS);
        sessions.put(session.getToken(), session);
        logger.info("创建手机上传会话: contractNo={}, uploadBy={}, token={}", contractNo, uploadBy, session.getToken());
        return session;
    }

    @Override
    public UploadSession getSession(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        UploadSession session = sessions.get(token);
        if (session == null) {
            return null;
        }
        // 惰性过期：每次访问校验有效期，并顺延10分钟窗口
        if (System.currentTimeMillis() > session.getExpireTime()) {
            sessions.remove(token);
            return null;
        }
        session.setExpireTime(System.currentTimeMillis() + SESSION_TTL_MILLIS);
        return session;
    }

    @Override
    public void removeSession(String token) {
        sessions.remove(token);
    }

    /**
     * 定时清理过期会话，避免内存泄漏
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void cleanExpiredSessions() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, UploadSession>> iterator = sessions.entrySet().iterator();
        int removed = 0;
        while (iterator.hasNext()) {
            if (iterator.next().getValue().getExpireTime() < now) {
                iterator.remove();
                removed++;
            }
        }
        if (removed > 0) {
            logger.debug("清理过期上传会话: {}个", removed);
        }
    }
}
