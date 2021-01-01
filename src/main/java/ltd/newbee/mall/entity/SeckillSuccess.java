package ltd.newbee.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("tb_newbee_mall_seckill_success")
public class SeckillSuccess implements Serializable {

    private static final long serialVersionUID = 7496465154189153364L;

    @TableId(type = IdType.AUTO)
    private Long secId;

    private Long seckillId;

    private Long userId;

    private Byte status;

    private Date createTime;
}
