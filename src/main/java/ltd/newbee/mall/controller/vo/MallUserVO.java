package ltd.newbee.mall.controller.vo;

import lombok.Data;

import java.util.Date;

@Data
public class MallUserVO {
    private Long userId;

    private String nickName;

    private String loginName;

    private String password;

    private String introduceSign;

    private String address;

    private int shopCartItemCount;

    private Byte isDeleted;

    private Byte lockedFlag;

    private Date createTime;
}
