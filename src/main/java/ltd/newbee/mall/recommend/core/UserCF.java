package ltd.newbee.mall.recommend.core;

import ltd.newbee.mall.recommend.dto.RelateDTO;

import java.util.*;
import java.util.stream.Collectors;

public class UserCF {
    /**
     * 物用户协同推荐
     *
     * @param userId 用户ID
     * @param num    返回数量
     * @param list   预处理数据
     * @param type   类型0基于用户推荐使用余弦相似度 1基于物品推荐使用余弦相似度 2基于用户推荐使用皮尔森系数计算
     * @return 商品id集合
     */
    public static List<Long> recommend(Long userId, Integer num,
                                       List<RelateDTO> list, Integer type) {
        // 对每个用户的购买商品记录进行分组
        Map<Long, List<RelateDTO>> userMap = list.stream()
                .collect(Collectors.groupingBy(RelateDTO::getUserId));
        // 获取其他用户与当前用户的关系值
        Map<Double, Long> userDisMap = CoreMath.computeNeighbor(userId, userMap, type);
        List<Long> similarUserIdList = new ArrayList<>();
        List<Double> values = new ArrayList<>(userDisMap.keySet());
        values.sort(Collections.reverseOrder());
        List<Double> scoresList = values.stream().limit(3).toList();
        // 获取关系最近的用户
        for (Double aDouble : scoresList) {
            similarUserIdList.add(userDisMap.get(aDouble));
        }
        List<Long> similarProductIdList = new ArrayList<>();
        for (Long similarUserId : similarUserIdList) {
            // 获取相似用户购买商品的记录
            List<Long> collect = userMap.get(similarUserId).stream()
                    .map(RelateDTO::getProductId).toList();
            // 过滤掉重复的商品
            List<Long> collect1 = collect.stream()
                    .filter(e -> !similarProductIdList.contains(e)).toList();
            similarProductIdList.addAll(collect1);
        }
        // 当前登录用户购买过的商品
        List<Long> userProductIdList = userMap.getOrDefault(userId,
                Collections.emptyList()).stream().map(RelateDTO::getProductId).toList();
        // 相似用户买过，但是当前用户没买过的商品作为推荐
        List<Long> recommendList = new ArrayList<>();
        for (Long similarProduct : similarProductIdList) {
            if (!userProductIdList.contains(similarProduct)) {
                recommendList.add(similarProduct);
            }
        }
        Collections.sort(recommendList);
        return recommendList.stream().distinct().limit(num).toList();
    }
}
