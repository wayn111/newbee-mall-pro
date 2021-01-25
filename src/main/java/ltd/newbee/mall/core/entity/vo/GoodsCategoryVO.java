package ltd.newbee.mall.core.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * 商品分类对象VO
 */
@Data
public class GoodsCategoryVO {
    private Long categoryId;

    private Long parentId;

    private Byte categoryLevel;

    private String categoryName;

    private List<GoodsCategoryVO> subCategoryVOS;
}
