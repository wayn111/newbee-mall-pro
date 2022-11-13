package ltd.newbee.mall.core.entity.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 商品分类对象VO
 */
@Data
public class GoodsCategoryVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -4332099215192984767L;
    private Long categoryId;

    private Long parentId;

    private Byte categoryLevel;

    private String categoryName;

    private List<GoodsCategoryVO> subCategoryVOS;
}
