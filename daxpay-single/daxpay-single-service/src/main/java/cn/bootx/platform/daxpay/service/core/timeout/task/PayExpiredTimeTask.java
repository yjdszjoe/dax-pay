package cn.bootx.platform.daxpay.service.core.timeout.task;

import cn.bootx.platform.daxpay.param.pay.PaySyncParam;
import cn.bootx.platform.daxpay.service.core.payment.sync.service.PaySyncService;
import cn.bootx.platform.daxpay.service.core.timeout.dao.PayExpiredTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

/**
 *
 * @author xxm
 * @since 2024/1/2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayExpiredTimeTask {
    private final PayExpiredTimeRepository repository;
    private final PaySyncService paySyncService;


//    @Scheduled(cron = "*/5 * * * * ?")
    public void task(){
        log.info("执行超时取消任务....");
        Set<String> expiredKeys = repository.getExpiredKeys(LocalDateTime.now());
        for (String expiredKey : expiredKeys) {
            log.info("key:{}", expiredKey);
            try {
                // 执行同步操作, 网关同步时会对支付的进行状态的处理
                Long paymentId = Long.parseLong(expiredKey);
                PaySyncParam paySyncParam = new PaySyncParam();
                paySyncParam.setPaymentId(paymentId);
                paySyncService.sync(paySyncParam);
            } catch (Exception e) {
                log.error("超时取消任务 异常", e);
            }

        }



    }
}
