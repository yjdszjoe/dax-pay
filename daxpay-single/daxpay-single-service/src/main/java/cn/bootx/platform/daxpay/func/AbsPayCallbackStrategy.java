package cn.bootx.platform.daxpay.func;

import cn.bootx.platform.common.redis.RedisClient;
import cn.bootx.platform.daxpay.code.PayChannelEnum;
import cn.bootx.platform.daxpay.common.local.PaymentContextLocal;
import cn.bootx.platform.daxpay.core.callback.dao.CallbackNotifyManager;
import cn.bootx.platform.daxpay.core.callback.entity.CallbackNotify;
import cn.bootx.platform.daxpay.core.payment.callback.result.PayCallbackResult;
import cn.bootx.platform.daxpay.core.payment.callback.service.PayCallbackService;
import cn.hutool.json.JSONUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付回调处理抽象接口
 *
 * @author xxm
 * @since 2021/6/21
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbsPayCallbackStrategy implements PayStrategy {

    protected static final ThreadLocal<Map<String, String>> PARAMS = new TransmittableThreadLocal<>();

    private final RedisClient redisClient;

    private final CallbackNotifyManager callbackNotifyManager;

    private final PayCallbackService payCallbackService;

    /**
     * 支付回调
     */
    public String payCallback(Map<String, String> params) {
        PaymentContextLocal.get().getCallbackParam().putAll(params);
        log.info("支付回调处理: {}", params);
        // 验证消息
        if (!this.verifyNotify()) {
            return null;
        }
        // 去重处理
        if (!this.duplicateChecker()) {
            return this.getReturnMsg();
        }
        // 调用统一回调处理
        PayCallbackResult result = payCallbackService.callback(this.getPaymentId(), this.getTradeStatus(), params);
        // 记录回调记录
        this.saveNotifyRecord(result);
        return this.getReturnMsg();
    }

    /**
     * 支付类型
     * @see PayChannelEnum
     */
    public abstract PayChannelEnum getPayChannel();

    /**
     * 去重处理
     */
    public boolean duplicateChecker() {
        // 判断10秒内是否已经回调处理
        String key = "payment:callback:duplicate:" + this.getPaymentId();
        return redisClient.setIfAbsent(key, "", 10 * 1000);
    }

    /**
     * 验证信息格式
     */
    public abstract boolean verifyNotify();

    /**
     * 获取paymentId
     */
    public abstract Long getPaymentId();

    /**
     * 获取支付状态
     * @see cn.bootx.platform.daxpay.code.PayStatusEnum
     */
    public abstract String getTradeStatus();

    /**
     * 返回响应结果
     */
    public abstract String getReturnMsg();

    /**
     * 保存回调记录
     */
    public void saveNotifyRecord(PayCallbackResult result) {
        Map<String, String> callbackParam = PaymentContextLocal.get().getCallbackParam();
        CallbackNotify payNotifyRecord = new CallbackNotify()
                .setNotifyInfo(JSONUtil.toJsonStr(callbackParam))
                .setNotifyTime(LocalDateTime.now())
                .setPaymentId(this.getPaymentId())
                .setPayChannel(this.getPayChannel().getCode())
                .setPayStatus(result.getStatus())
                .setMsg(result.getMsg());
        callbackNotifyManager.save(payNotifyRecord);
    }

}
