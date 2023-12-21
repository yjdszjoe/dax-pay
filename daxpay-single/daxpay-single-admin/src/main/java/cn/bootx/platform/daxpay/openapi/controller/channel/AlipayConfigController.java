package cn.bootx.platform.daxpay.openapi.controller.channel;

import cn.bootx.platform.common.core.rest.Res;
import cn.bootx.platform.common.core.rest.ResResult;
import cn.bootx.platform.common.core.rest.dto.LabelValue;
import cn.bootx.platform.daxpay.core.channel.alipay.service.AlipayConfigService;
import cn.bootx.platform.daxpay.dto.channel.alipay.AlipayConfigDto;
import cn.bootx.platform.daxpay.param.channel.alipay.AlipayConfigParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author xxm
 * @since 2021/2/26
 */
@Tag(name = "支付宝配置")
@RestController
@RequestMapping("/alipay")
@AllArgsConstructor
public class AlipayConfigController {

    private final AlipayConfigService alipayConfigService;

    @Operation(summary = "更新")
    @PostMapping("/update")
    public ResResult<Void> update(@RequestBody AlipayConfigParam param) {
        alipayConfigService.update(param);
        return Res.ok();
    }

    @Operation(summary = "根据Id查询")
    @GetMapping("/findById")
    public ResResult<AlipayConfigDto> findById() {
        return Res.ok(alipayConfigService.getConfig().toDto());
    }

    @Operation(summary = "支付宝支持支付方式")
    @GetMapping("/findPayWayList")
    public ResResult<List<LabelValue>> findPayWayList() {
        return Res.ok(alipayConfigService.findPayWayList());
    }

    @SneakyThrows
    @Operation(summary = "读取证书文件内容")
    @PostMapping("/readPem")
    public ResResult<String> readPem(MultipartFile file){
        return Res.ok(new String(file.getBytes(), StandardCharsets.UTF_8));
    }
}
