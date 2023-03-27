package ltd.newbee.mall.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName
public class CdkInfo {
    private Integer cdkId;
    private String cdkNo;

    private Date createTime;
    private String createUser;
}
