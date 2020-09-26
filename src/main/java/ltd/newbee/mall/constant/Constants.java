package ltd.newbee.mall.constant;

public class Constants {

    /**
     * 上传文件的默认url前缀，根据部署设置自行修改
     */
    public static final String FILE_UPLOAD_DIC = "D:\\upload\\";

    public static final Byte CATEGORY_LEVEL_ONE = 1;
    public static final Byte CATEGORY_LEVEL_TWO = 2;
    public static final Byte CATEGORY_LEVEL_THREE = 3;


    public final static int INDEX_GOODS_HOT_NUMBER = 4;//首页热卖商品数量
    public final static int INDEX_GOODS_NEW_NUMBER = 5;//首页新品数量
    public final static int INDEX_GOODS_RECOMMOND_NUMBER = 10;//首页推荐商品数量


    public final static String MALL_USER_SESSION_KEY = "mallUser";//session中user的key

    public final static String MALL_VERIFY_CODE_KEY = "mallVerifyCode";//验证码key


    public final static int GOODS_SEARCH_PAGE_LIMIT = 10;//搜索分页的默认条数(每页10条)

    public final static int ORDER_SEARCH_PAGE_LIMIT = 3;//我的订单列表分页的默认条数(每页3条)

    /**
     * 字符编码
     */
    public static final String UTF_ENCODING = "UTF-8";
    /**
     * 字符串表示true
     */
    public static final String TRUE = "true";
    /**
     * 操作状态，成功
     */
    public static final Integer OPERATE_SUCCESS = 0;
    /**
     * 操作状态，失败
     */
    public static final Integer OPERATE_FAIL = 1;

    /**
     * 当前页
     */
    public static String PAGE_NUMBER = "pageNumber";

    /**
     * 分页大小
     */
    public static String PAGE_SIZE = "pageSize";

    /**
     * 排序字段名
     */
    public static String SORT_NAME = "sortName";

    /**
     * 排序方式 asc或者desc
     */
    public static String SORT_ORDER = "sortOrder";
    public static String ORDER_DESC = "desc";
    public static String SORT_ASC = "asc";
}
