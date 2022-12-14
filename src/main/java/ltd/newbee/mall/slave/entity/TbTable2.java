package ltd.newbee.mall.slave.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tb_table_2")
public class TbTable2 {

    @TableId(type = IdType.AUTO)
    private Integer id;


    private String name;

    private Date createTime;
}
