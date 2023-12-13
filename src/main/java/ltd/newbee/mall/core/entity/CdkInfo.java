package ltd.newbee.mall.core.entity;

import cn.hutool.db.meta.TableType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName
public class CdkInfo {
    @TableId(type = IdType.AUTO)
    private Integer cdkId;
    private String cdkNo;

    private Date createTime;
    private String createUser;
}
