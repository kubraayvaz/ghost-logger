package com.ghostlogger.domain.port;

import com.ghostlogger.domain.model.ErrorLog;

/**
 * Alert Service Port - Hexagonal Architecture
 * <p>
 * Sends alerts for critical errors (e.g., PagerDuty, Slack, Email)
 */
public interface AlertService {
    
    /**
     * Send alert for critical error log
     * 
     * @param errorLog The error log to alert on
     * @throws Exception if alert fails
     */
    void sendAlert(ErrorLog errorLog) throws Exception;
}
