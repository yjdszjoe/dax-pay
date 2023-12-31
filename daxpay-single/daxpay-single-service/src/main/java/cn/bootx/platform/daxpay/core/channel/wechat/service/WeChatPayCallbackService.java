package cn.bootx.platform.daxpay.core.channel.wechat.service;

import cn.bootx.platform.common.redis.RedisClient;
import cn.bootx.platform.daxpay.code.PayChannelEnum;
import cn.bootx.platform.daxpay.code.PayStatusEnum;
import cn.bootx.platform.daxpay.code.WeChatPayCode;
import cn.bootx.platform.daxpay.common.local.PaymentContextLocal;
import cn.bootx.platform.daxpay.core.callback.dao.CallbackNotifyManager;
import cn.bootx.platform.daxpay.core.channel.wechat.entity.WeChatPayConfig;
import cn.bootx.platform.daxpay.core.payment.callback.service.PayCallbackService;
import cn.bootx.platform.daxpay.func.AbsPayCallbackStrategy;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.ijpay.core.enums.SignType;
import com.ijpay.core.kit.WxPayKit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static cn.bootx.platform.daxpay.code.WeChatPayCode.APPID;


/**
 * 微信支付回调
 *
 * @author xxm
 * @since 2021/6/21
 */
@Slf4j
@Service
public class WeChatPayCallbackService extends AbsPayCallbackStrategy {
    private final WeChatPayConfigService weChatPayConfigService;

    public WeChatPayCallbackService(RedisClient redisClient, CallbackNotifyManager callbackNotifyManager,
                                    PayCallbackService payCallbackService, WeChatPayConfigService weChatPayConfigService) {
        super(redisClient, callbackNotifyManager, payCallbackService);
        this.weChatPayConfigService = weChatPayConfigService;
    }

    @Override
    public PayChannelEnum getPayChannel() {
        return PayChannelEnum.WECHAT;
    }

    /**
     * 获取支付单id
     */
    @Override
    public Long getPaymentId() {
        Map<String, String> params = PaymentContextLocal.get().getCallbackParam();
        String paymentId = params.get(WeChatPayCode.OUT_TRADE_NO);
        return Long.valueOf(paymentId);
    }

    /**
     * 获取支付状态
     */
    @Override
    public String getTradeStatus() {
        Map<String, String> params = PaymentContextLocal.get().getCallbackParam();
        if (WxPayKit.codeIsOk(params.get(WeChatPayCode.RESULT_CODE))) {
            return PayStatusEnum.SUCCESS.getCode();
        }
        else {
            return PayStatusEnum.FAIL.getCode();
        }
    }

    /**
     * 验证回调消息
     */
    @Override
    public boolean verifyNotify() {
        Map<String, String> params = PaymentContextLocal.get().getCallbackParam();
        String callReq = JSONUtil.toJsonStr(params);
        log.info("微信发起回调 报文: {}", callReq);
        String appId = params.get(APPID);

        if (StrUtil.isBlank(appId)) {
            log.warn("微信回调报文 appId 为空 {}", callReq);
            return false;
        }

        WeChatPayConfig weChatPayConfig = weChatPayConfigService.getConfig();
        if (Objects.isNull(weChatPayConfig)) {
            log.warn("微信支付配置不存在: {}", callReq);
            return false;
        }
        return WxPayKit.verifyNotify(params, weChatPayConfig.getApiKeyV2(), SignType.HMACSHA256, null);
    }

    @Override
    public String getReturnMsg() {
        Map<String, String> xml = new HashMap<>(4);
        xml.put(WeChatPayCode.RETURN_CODE, "SUCCESS");
        xml.put(WeChatPayCode.RETURN_MSG, "OK");
        return WxPayKit.toXml(xml);
    }

}
