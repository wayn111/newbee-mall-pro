package ltd.newbee.mall.entity.vo;

import lombok.Data;

/**
 * 查询参数封装对象
 */
@Data
public class SearchObjVO {

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
