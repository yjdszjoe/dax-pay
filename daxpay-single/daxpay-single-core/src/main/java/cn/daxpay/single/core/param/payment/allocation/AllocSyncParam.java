package cn.daxpay.single.core.param.payment.allocation;

import cn.daxpay.single.core.param.PaymentCommonParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;

/**
 * 分账同步请求参数
 * @author xxm
 * @since 2024/4/12
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "分账同步请求参数")
public class AllocSyncParam extends PaymentCommonParam {

    @Schema(description = "分账号")
    @Size(max = 32, message = "分账号不可超过32位")
    private String allocNo;

    @Schema(description = "商户分账号")
    @Size(max = 100, message = "商户分账号不可超过100位")
    private String bizAllocNo;
}
