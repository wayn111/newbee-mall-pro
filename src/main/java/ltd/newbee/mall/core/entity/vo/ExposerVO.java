package ltd.newbee.mall.core.entity.vo;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 秒杀服务接口地址暴露类
 */
@Data
public class ExposerVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 686399022714457693L;
    // 是否开启秒杀
    private boolean exposed;

    // 一种加密措施
    private String md5;

    // id
    private long seckillId;

    // 系统当前时间（毫秒）
    private long now;

    // 开启时间
    private long start;

    // 结束时间
    private long end;

    public ExposerVO(boolean exposed, String md5, long seckillId) {
        this.exposed = exposed;
        this.md5 = md5;
        this.seckillId = seckillId;
    }

    public ExposerVO(boolean exposed, long seckillId, long now, long start, long end) {
        this.exposed = exposed;
        this.seckillId = seckillId;
        this.now = now;
        this.start = start;
        this.end = end;
    }

    public ExposerVO(boolean exposed, long seckillId) {
        this.exposed = exposed;
        this.seckillId = seckillId;
    }

}
