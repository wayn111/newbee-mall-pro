package ltd.newbee.mall.recommend.core; // 包路径声明

import ltd.newbee.mall.recommend.dto.RelateDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserCF {
    /**
     * 实现用户协同过滤推荐算法
     *
     * @param userId 用户ID
     * @param num    推荐商品的数量
     * @param list   预处理的关联数据列表
     * @param type   计算相似度的类型：0-基于用户的余弦相似度，1-基于物品的余弦相似度，2-基于用户的皮尔森系数
     * @return 商品ID的列表
     */
    public static List<Long> recommend(Long userId, Integer num,
                                       List<RelateDTO> list, Integer type) {
        // 用户购买商品数据的分组映射
        Map<Long, List<RelateDTO>> userMap = list.stream()
                .collect(Collectors.groupingBy(RelateDTO::getUserId));
        // 计算当前用户和其他用户之间的相似度
        Map<Double, Long> userDisMap = CoreMath.computeNeighbor(userId, userMap, type);
        // 与当前用户相似的用户列表
        List<Long> similarUserIdList = new ArrayList<>();
        // 相似度值列表
        List<Double> values = new ArrayList<>(userDisMap.keySet());
        values.sort(Collections.reverseOrder()); // 将相似度值列表降序排序
        // 取出相似度最高的三个值
        List<Double> scoresList = values.stream().limit(3).toList();
        // 从相似用户中提取用户ID
        for (Double aDouble : scoresList) {
            similarUserIdList.add(userDisMap.get(aDouble));
        }
        // 与当前用户相似的产品列表
        List<Long> similarProductIdList = new ArrayList<>();
        for (Long similarUserId : similarUserIdList) {
            // 获取每个相似用户购买过的产品ID集
            List<Long> collect = userMap.get(similarUserId).stream()
                    .map(RelateDTO::getProductId).toList();
            // 过滤掉已经存在于推荐列表中的产品ID，避免重复
            List<Long> uniqueProducts = collect.stream()
                    .filter(e -> !similarProductIdList.contains(e)).toList();
            similarProductIdList.addAll(uniqueProducts);
        }
        // 当前用户已购买的商品ID
        List<Long> userProductIdList = userMap.getOrDefault(userId,
                Collections.emptyList()).stream().map(RelateDTO::getProductId).toList();
        // 推荐那些相似用户购买，但是自己尚未购买的商品
        List<Long> recommendList = similarProductIdList.stream()
                .filter(prodId -> !userProductIdList.contains(prodId))
                .sorted().toList(); // 将推荐列表排序并去重
        // 返回指定数量的推荐商品ID列表
        return recommendList.stream().distinct().limit(num).toList();
    }
}
