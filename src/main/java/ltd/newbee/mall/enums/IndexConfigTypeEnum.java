package ltd.newbee.mall.enums;

/**
 * 首页配置枚举类
 */
public enum IndexConfigTypeEnum {
    /**
     * 默认
     */
    DEFAULT(0, "DEFAULT"),
    /**
     * 首页热门商品
     */
    INDEX_GOODS_HOT(3, "INDEX_GOODS_HOTS"),

    /**
     * 首页新品
     */
    INDEX_GOODS_NEW(4, "INDEX_GOODS_NEW"),

    /**
     * 首页推荐商品
     */
    INDEX_GOODS_RECOMMOND(5, "INDEX_GOODS_RECOMMOND");

    private int type;

    private String name;

    IndexConfigTypeEnum(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public static IndexConfigTypeEnum getIndexConfigTypeEnumByType(int type) {
        for (IndexConfigTypeEnum indexConfigTypeEnum : IndexConfigTypeEnum.values()) {
            if (indexConfigTypeEnum.getType() == type) {
                return indexConfigTypeEnum;
            }
        }
        return DEFAULT;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
