package ltd.newbee.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@TableName("tb_newbee_mall_user")
@Data
public class MallUser {
    @TableId(type = IdType.AUTO)
    private Long userId;

    private String nickName;

    private String loginName;

    private String passwordMd5;

    private String introduceSign;

    private String address;

    @TableLogic
    private Byte isDeleted;

    private Byte lockedFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
