package ltd.newbee.mall.util;

/**
 * @author: waynaqua
 * @date: 2023/12/16 23:48
 */
public class ChineseUtil {

    public static boolean hasChinese(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 0x4e00 && c <= 0x9fbb) {
                return true;
            }
        }
        return false;
    }
}
