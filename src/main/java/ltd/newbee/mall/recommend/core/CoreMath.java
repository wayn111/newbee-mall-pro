package ltd.newbee.mall.recommend.core;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.recommend.dto.RelateDTO;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class CoreMath {

    /**
     * 根据前三个最相似的用户进行推荐
     *
     * @param userId 用户id
     * @param num    返回数量
     * @param list   推荐的商品idList集合
     * @return
     */
    public List<Long> recommend(Long userId, Integer num, List<RelateDTO> list) {
        // 相关强度： 相关系数 0.8-1.0 极强相关 0.6-0.8 强相关 0.4-0.6 中等程度相关 0.2-0.4 弱相关 0.0-0.2 极弱相关或无相关
        Map<Double, Long> distances = computeNearestNeighbor(userId, list);
        List<Long> similarUserIdList;
        List<Long> values = new ArrayList<>(distances.values());
        int size = values.size();
        if (size >= 3) {
            // 找出前一个相似的用户
            values.sort(Collections.reverseOrder());
            similarUserIdList = values.stream().limit(3).collect(Collectors.toList());
            log.info("相似用户ids:{}", similarUserIdList);
        } else {
            // 用户未购买过商品时，基于用户点击量推荐
            Map<Long, Long> collect = list.stream().collect(Collectors.groupingBy(RelateDTO::getProductId, Collectors.counting()));
            LinkedHashSet<Map.Entry<Long, Long>> linkedHashSet = collect.entrySet().stream().sorted((o1, o2) -> Math.toIntExact(o2.getValue() - o1.getValue()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            return linkedHashSet.stream().map(Map.Entry::getKey).distinct().limit(num).collect(Collectors.toList());
        }
        // 对每个用户的购买商品记录进行分组
        Map<Long, List<RelateDTO>> userMap = list.stream().collect(Collectors.groupingBy(RelateDTO::getUserId));
        // 前三名相似用户购买过的商品
        List<Long> similarProductIdList = new ArrayList<>();
        for (Long similarUserId : similarUserIdList) {
            // 获取相似用户购买商品的记录
            List<Long> collect = userMap.get(similarUserId).stream().map(RelateDTO::getProductId).toList();
            // 过滤掉重复的商品
            List<Long> collect1 = collect.stream().filter(e -> !similarProductIdList.contains(e)).toList();
            similarProductIdList.addAll(collect1);
        }
        // 当前登录用户购买过的商品
        List<Long> userProductIdList = userMap.getOrDefault(userId, Collections.emptyList()).stream().map(RelateDTO::getProductId).toList();
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

    /**
     * 在给定userId的情况下，计算其他用户和它的相关系数并排序
     *
     * @param userId
     * @param list
     * @return
     */
    private Map<Double, Long> computeNearestNeighbor(Long userId, List<RelateDTO> list) {
        Map<Long, List<RelateDTO>> userMap = list.stream().collect(Collectors.groupingBy(RelateDTO::getUserId));
        Map<Double, Long> distances = new TreeMap<>();
        userMap.forEach((k, v) -> {
            if (k.longValue() != userId.longValue()) {
                double distance = pearson_dis(v, userMap.getOrDefault(userId, Collections.emptyList()));
                distances.put(distance, k);
            }
        });
        return distances;
    }

    /**
     * 计算两个序列间的相关系数
     *
     * @param xList
     * @param yList
     * @return
     */
    private double pearson_dis(List<RelateDTO> xList, List<RelateDTO> yList) {
        List<Integer> xs = Lists.newArrayList();
        List<Integer> ys = Lists.newArrayList();
        xList.forEach(x -> yList.forEach(y -> {
            // 购买的商品相同，交集
            if (x.getProductId().longValue() == y.getProductId().longValue()) {
                xs.add(x.getIndex());
                ys.add(y.getIndex());
            }
        }));
        return pearsonRelate(xs, ys);
    }

    /**
     * 方法描述: 皮尔森（pearson）相关系数计算
     * 余弦相似度：越接近于 1 ，说明两个用户的浏览行为越相似
     *
     * @param xs
     * @param ys
     * @throws
     * @Return {@link Double}
     * @author tarzan
     * @date 2020年07月31日 17:03:20
     */
    public static Double pearsonRelate(List<Integer> xs, List<Integer> ys) {
        int n = xs.size();
        double Ex = xs.stream().mapToDouble(x -> x).sum();
        double Ey = ys.stream().mapToDouble(y -> y).sum();
        double Ex2 = xs.stream().mapToDouble(x -> Math.pow(x, 2)).sum();
        double Ey2 = ys.stream().mapToDouble(y -> Math.pow(y, 2)).sum();
        double Exy = IntStream.range(0, n).mapToDouble(i -> xs.get(i) * ys.get(i)).sum();
        double numerator = Exy - Ex * Ey / n;
        double denominator = Math.sqrt((Ex2 - Math.pow(Ex, 2) / n) * (Ey2 - Math.pow(Ey, 2) / n));
        if (denominator == 0 || Double.isNaN(numerator) || Double.isNaN(denominator)) {
            return 0.0;
        }
        return numerator / denominator;
    }

}
