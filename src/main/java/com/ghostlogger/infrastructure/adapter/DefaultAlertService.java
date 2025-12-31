package com.ghostlogger.infrastructure.adapter;

import com.ghostlogger.domain.model.ErrorLog;
import com.ghostlogger.domain.port.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Default Alert Service Implementation
 * <p>
 * Simulates sending alerts to external systems (PagerDuty, Slack, etc.)
 */
@Service
public class DefaultAlertService implements AlertService {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultAlertService.class);
    
    @Override
    public void sendAlert(ErrorLog errorLog) throws Exception {
        // Simulate network latency
        Thread.sleep(100);
        
        log.warn("🚨 ALERT SENT: {} [severity={}, traceId={}]",
            errorLog.message(),
            errorLog.severity(),
            errorLog.traceContext().traceId());
        
        // Simulate potential alert failure (5% failure rate for testing)
        if (Math.random() < 0.05) {
            throw new Exception("Alert service temporarily unavailable");
        }
    }
}
