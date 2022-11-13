package ltd.newbee.mall.core.entity.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询参数封装对象
 */
@Data
public class SearchObjVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 8060875279157963202L;
    /**
     * 查询条件
     */
    private String keyword;

    /**
     * 分类条件
     */
    private Long goodsCategoryId;

    /**
     * 排序字段条件
     */
    private String sidx;

    /**
     * 排序 asc desc
     */
    private String order;

}
