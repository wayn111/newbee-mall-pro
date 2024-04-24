package ltd.newbee.mall.recommend.core; // 包声明

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.recommend.dto.RelateDTO;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

@Slf4j
public class CoreMath {

    /**
     * 计算相关系数并排序
     *
     * @param key  代表当前用户或物品的唯一标识
     * @param map  存储用户或物品与对应关系数据的映射
     * @param type 确定使用哪种方式计算相关系数：0代表基于用户的余弦相似度，1代表基于物品的余弦相似度，2代表使用皮尔森系数
     * @return 返回一个根据相关系数排序的映射，映射的键为相关系数，值为用户或物品的唯一标识
     */
    public static Map<Double, Long> computeNeighbor(Long key, Map<Long, List<RelateDTO>> map, int type) {
        // 使用TreeMap存储相关系数和对应键值对，确保结果有序
        Map<Double, Long> distMap = new TreeMap<>();
        // 获取当前用户或物品的关系数据
        List<RelateDTO> items = map.get(key);
        map.forEach((k, v) -> {
            // 排除当前用户或物品
            if (!k.equals(key)) {
                // 计算相关系数
                double coefficient = relateDist(v, items, type);
                // 将系数和标识加入映射中
                distMap.put(coefficient, k);
            }
        });
        // 返回结果
        return distMap;
    }

    /**
     * 计算两个序列间的相关系数
     *
     * @param xList 第一个用户或物品的关系数据列表
     * @param yList 第二个用户或物品的关系数据列表
     * @param type  计算方式标识
     * @return 返回两个序列间的相关系数
     */
    private static double relateDist(List<RelateDTO> xList, List<RelateDTO> yList, Integer type) {
        // 存储第一个列表的相关数据
        List<Integer> xs = Lists.newArrayList();
        // 存储第二个列表的相关数据
        List<Integer> ys = Lists.newArrayList();
        xList.forEach(x -> yList.forEach(y -> {
            if ((type == 0 || type == 2) && x.getProductId().longValue() == y.getProductId().longValue()) {
                xs.add(x.getIndex());
                ys.add(y.getIndex());
            } else if (type == 1 && x.getUserId().longValue() == y.getUserId().longValue()) {
                xs.add(x.getIndex());
                ys.add(y.getIndex());
            }
        }));
        if (ys.isEmpty() || xs.isEmpty()) {
            // 如果一个序列为空，则相关系数为0
            return 0d;
        }
        if (type == 2) {
            // 使用皮尔森系数计算
            return pearsonRelate(xs, ys);
        } else {
            // 使用余弦相似度计算
            return cosineSimilarity(xs, ys);
        }
    }

    /**
     * 皮尔森（pearson）相关系数计算
     *
     * @param xs 第一个列表的数据
     * @param ys 第二个列表的数据
     * @return 返回两个列表的皮尔森相关系数
     */
    public static double pearsonRelate(List<Integer> xs, List<Integer> ys) {
        // 各种相关数据计算
        int n = xs.size();
        double Ex = xs.stream().mapToDouble(x -> x).sum();
        double Ey = ys.stream().mapToDouble(y -> y).sum();
        double Ex2 = xs.stream().mapToDouble(x -> Math.pow(x, 2)).sum();
        double Ey2 = ys.stream().mapToDouble(y -> Math.pow(y, 2)).sum();
        double Exy = IntStream.range(0, n).mapToDouble(i -> xs.get(i) * ys.get(i)).sum();
        // 计算相关系数
        double numerator = Exy - Ex * Ey / n;
        double denominator = Math.sqrt((-Math.pow(Ex, 2) / n) * (Ey2 - Math.pow(Ey, 2) / n));
        if (denominator == 0 || Double.isNaN(numerator) || Double.isNaN(denominator)) {
            // 如果分母为0，则相关系数为0
            return 0.0;
        }
        return numerator / denominator;
    }

    /**
     * 计算向量之间的余弦相似度
     *
     * @param xs 第一个向量
     * @param ys 第二个向量
     * @return 返回两向量的余弦相似度
     */
    private static double cosineSimilarity(List<Integer> xs, List<Integer> ys) {
        // 向量点积以及各自向量的范数计算
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
        // 计算并返回余弦相似度
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

}
