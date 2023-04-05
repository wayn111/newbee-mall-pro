package ltd.newbee.mall.recommend.core;

import com.google.common.collect.Lists;
import ltd.newbee.mall.recommend.dto.RelateDTO;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

public class CoreMath {
    /**
     * @param userId
     * @param list
     * @return
     */
    public List<Integer> recommend1(Integer userId, List<RelateDTO> list) {
        // 返回的相关系数集合
        Map<Double, Integer> distances = computeNearestNeighbor(userId, list);
        // 从小到大排序
        Iterator<Integer> iterator = distances.values().iterator();
        // 获取最后一个，相关系数最大，也就是最相似的用户id
        Integer nearest = iterator.next();
        while (iterator.hasNext()) {
            nearest = iterator.next();
        }
        // 对每个用户的购买商品记录进行分组
        Map<Integer, List<RelateDTO>> userMap = list.stream().collect(Collectors.groupingBy(RelateDTO::getUserId));
        // 最近邻用户买过的商品id列表
        List<Integer> neighborItemList = userMap.get(nearest).stream().map(RelateDTO::getProductId).toList();
        // 指定用户买过的商品id列表
        List<Integer> userItemList = userMap.get(userId).stream().map(RelateDTO::getProductId).toList();

        // 找到最近邻买过，但是该用户没买过的商品id，放入推荐列表
        List<Integer> recommendList = new ArrayList<>();
        for (Integer item : neighborItemList) {
            if (!userItemList.contains(item)) {
                recommendList.add(item);
            }
        }
        Collections.sort(recommendList);
        return recommendList;
    }

    /**
     * 根据前三个最相似的用户进行推荐
     *
     * @param userId 用户id
     * @param list   推荐的商品idList集合
     * @return
     */
    public List<Integer> recommend(Integer userId, List<RelateDTO> list) {
        // 相关强度： 相关系数 0.8-1.0 极强相关 0.6-0.8 强相关 0.4-0.6 中等程度相关 0.2-0.4 弱相关 0.0-0.2 极弱相关或无相关
        Map<Double, Integer> distances = computeNearestNeighbor(userId, list);
        List<Integer> similarUserIdList;
        List<Integer> values = new ArrayList<>(distances.values());
        int size = values.size();
        if (size >= 3) {
            // 找出前三个相似的用户
            values.sort(Collections.reverseOrder());
            similarUserIdList = values.stream().limit(3).collect(Collectors.toList());
        } else {
            // 用户未购买过商品时，基于用户点击量推荐
            Map<Integer, Long> collect = list.stream().collect(Collectors.groupingBy(RelateDTO::getProductId, Collectors.counting()));
            LinkedHashSet<Map.Entry<Integer, Long>> linkedHashSet = collect.entrySet().stream().sorted((o1, o2) -> Math.toIntExact(o2.getValue() - o1.getValue()))
                    .limit(10)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            return linkedHashSet.stream().map(Map.Entry::getKey).collect(Collectors.toList());
        }
        // 对每个用户的购买商品记录进行分组
        Map<Integer, List<RelateDTO>> userMap = list.stream().collect(Collectors.groupingBy(RelateDTO::getUserId));
        // 前三名相似用户购买过的商品
        List<Integer> similarProductIdList = new ArrayList<>();
        for (Integer similarUserId : similarUserIdList) {
            // 获取相似用户购买商品的记录
            List<Integer> collect = userMap.get(similarUserId).stream().map(RelateDTO::getProductId).toList();
            // 过滤掉重复的商品
            List<Integer> collect1 = collect.stream().filter(e -> !similarProductIdList.contains(e)).toList();
            similarProductIdList.addAll(collect1);
        }
        // 当前登录用户购买过的商品
        List<Integer> userProductIdList = userMap.getOrDefault(userId, Collections.emptyList()).stream().map(RelateDTO::getProductId).toList();
        // 相似用户买过，但是当前用户没买过的商品作为推荐
        List<Integer> recommendList = new ArrayList<>();
        for (Integer similarProduct : similarProductIdList) {
            if (!userProductIdList.contains(similarProduct)) {
                recommendList.add(similarProduct);
            }
        }
        Collections.sort(recommendList);
        return recommendList;
    }

    /**
     * 在给定userId的情况下，计算其他用户和它的相关系数并排序
     *
     * @param userId
     * @param list
     * @return
     */
    private Map<Double, Integer> computeNearestNeighbor(Integer userId, List<RelateDTO> list) {
        Map<Integer, List<RelateDTO>> userMap = list.stream().collect(Collectors.groupingBy(RelateDTO::getUserId));
        Map<Double, Integer> distances = new TreeMap<>();
        userMap.forEach((k, v) -> {
            if (k.intValue() != userId.intValue()) {
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
            if (x.getProductId().intValue() == y.getProductId().intValue()) {
                // TODO 将购买次数大于5的加入统计计算
                xs.add(x.getIndex());
                ys.add(y.getIndex());
            }
        }));
        return getRelate(xs, ys);
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
    public static Double getRelate(List<Integer> xs, List<Integer> ys) {
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
