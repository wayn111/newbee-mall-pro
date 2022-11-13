package ltd.newbee.mall.core.entity.vo;

import lombok.Data;
import ltd.newbee.mall.core.entity.GoodsCategory;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 搜索页面分类数据VO
 */
@Data
public class SearchPageCategoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5851848214339537806L;
    private String firstLevelCategoryName;

    private List<GoodsCategory> secondLevelCategoryList;

    private String secondLevelCategoryName;

    private List<GoodsCategory> thirdLevelCategoryList;

    private String currentCategoryName;

}
