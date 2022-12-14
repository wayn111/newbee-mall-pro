package ltd.newbee.mall.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tb_table_1")
public class TbTable1 {

    @TableId(type = IdType.AUTO)
    private Integer id;


    private String name;

    private Date createTime;
}
