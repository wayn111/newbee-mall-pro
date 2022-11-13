package ltd.newbee.mall.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 秒杀商品entity
 */
@Data
@TableName("tb_newbee_mall_seckill")
public class Seckill implements Serializable {

    @Serial
    private static final long serialVersionUID = -3525445534150833719L;

    @TableId(type = IdType.AUTO)
    private Long seckillId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 秒杀价格
     */
    private Integer seckillPrice;

    /**
     * 秒杀商品数量
     */
    private Integer seckillNum;

    /**
     * 秒杀商品状态（0下架，1上架）
     */
    private Byte status;

    /**
     * 秒杀开始时间
     */
    private Date seckillBegin;

    /**
     * 秒杀结束使时间
     */
    private Date seckillEnd;

    /**
     * 秒杀商品排序
     */
    private Integer seckillRank;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 跟新时间
     */
    private Date updateTime;

    /**
     * 删除标识字段(0-未删除 1-已删除)
     */
    private Byte isDeleted;
}
