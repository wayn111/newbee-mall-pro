package ltd.newbee.mall.controller.vo;

import lombok.Data;

import java.util.List;

@Data
public class GoodsCategoryVO {
    private Long categoryId;

    private Long parentId;

    private Byte categoryLevel;

    private String categoryName;

    private List<GoodsCategoryVO> subCategoryVOS;
}
