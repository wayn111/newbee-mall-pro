package ltd.newbee.mall.recommend.core;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.recommend.dto.RelateDTO;

import java.util.*;
import java.util.stream.IntStream;

@Slf4j
public class CoreMath {

    /**
     * 计算相关系数并排序
     *
     * @param key
     * @param map
     * @param type 类型0基于用户推荐使用余弦相似度 1基于物品推荐使用余弦相似度 2基于用户推荐使用皮尔森系数计算
     * @return Map<Double, Long>
     */
    public static Map<Double, Long> computeNeighbor(Long key, Map<Long, List<RelateDTO>> map, int type) {
        Map<Double, Long> distMap = new TreeMap<>();
        List<RelateDTO> items = map.get(key);
        map.forEach((k, v) -> {
            // 排除此用户
            if (!k.equals(key)) {
                // 关系系数
                double coefficient = relateDist(v, items, type);
                // 关系距离
                // double distance=Math.abs(coefficient);
                distMap.put(coefficient, k);
            }
        });
        return distMap;
    }

    /**
     * 计算两个序列间的相关系数
     *
     * @param xList
     * @param yList
     * @param type  类型0基于用户推荐使用余弦相似度 1基于物品推荐使用余弦相似度 2基于用户推荐使用皮尔森系数计算
     * @return
     */
    private static double relateDist(List<RelateDTO> xList, List<RelateDTO> yList, Integer type) {
        List<Integer> xs = Lists.newArrayList();
        List<Integer> ys = Lists.newArrayList();
        xList.forEach(x -> yList.forEach(y -> {
            if (type == 0 || type == 2) {
                // 基于用户推荐时如果两个用户购买的商品相同，则计算相似度
                if (x.getProductId().longValue() == y.getProductId().longValue()) {
                    xs.add(x.getIndex());
                    ys.add(y.getIndex());
                }
            } else if (type == 1) {
                // 基于物品推荐时如果两个用户id相同，则计算相似度
                if (x.getUserId().longValue() == y.getUserId().longValue()) {
                    xs.add(x.getIndex());
                    ys.add(y.getIndex());
                }
            }

        }));
        if (ys.size() == 0 || xs.size() == 0) {
            return 0d;
        }
        if (type == 2) {
            return pearsonRelate(xs, ys);
        } else {
            return cosineSimilarity(xs, ys);
        }
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
    public static double pearsonRelate(List<Integer> xs, List<Integer> ys) {
        int n = xs.size();
        double Ex = xs.stream().mapToDouble(x -> x).sum();
        double Ey = ys.stream().mapToDouble(y -> y).sum();
        double Ex2 = xs.stream().mapToDouble(x -> Math.pow(x, 2)).sum();
        double Ey2 = ys.stream().mapToDouble(y -> Math.pow(y, 2)).sum();
        double Exy = IntStream.range(0, n).mapToDouble(i -> xs.get(i) * ys.get(i)).sum();
        double numerator = Exy - Ex * Ey / n;
        double denominator = Math.sqrt((-Math.pow(Ex, 2) / n) * (Ey2 - Math.pow(Ey, 2) / n));
        if (denominator == 0 || Double.isNaN(numerator) || Double.isNaN(denominator)) {
            return 0.0;
        }
        return numerator / denominator;
    }


    /**
     * 计算向量之间的余弦相似度
     *
     * @param xs
     * @param xs
     * @return double
     */
    private static double cosineSimilarity(List<Integer> xs, List<Integer> ys) {
        double dotProduct = 0;
        double norm1 = 0;
        double norm2 = 0;
        for (int i = 0; i < xs.size(); i++) {
            Integer x = xs.get(i);
            Integer y = ys.get(i);
            dotProduct += x * y;
            norm1 += Math.pow(x, 2);
            norm2 += Math.pow(y, 2);
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

}
