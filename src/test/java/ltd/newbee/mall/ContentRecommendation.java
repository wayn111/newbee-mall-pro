package ltd.newbee.mall;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContentRecommendation {
    // 定义商品和用户的向量空间
    Map<String, double[]> items;
    Map<String, double[]> users;

    // 初始化向量空间
    public ContentRecommendation(Map<String, double[]> items, Map<String, double[]> users) {
        this.items = items;
        this.users = users;
    }

    // 根据用户id获取推荐商品
    public List<String> getRecommendations(String userId, int num) {
        // 获取该用户的向量
        double[] userVector = users.get(userId);

        // 计算该用户和所有商品的余弦相似度
        List<Map.Entry<String, Double>> similarities = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : items.entrySet()) {
            String itemId = entry.getKey();
            double[] itemVector = entry.getValue();
            double similarity = cosineSimilarity(userVector, itemVector);
            similarities.add(new AbstractMap.SimpleEntry<>(itemId, similarity));
        }

        // 根据相似度排序，取前num个商品
        similarities.sort((o1, o2) -> Double.compare(o2.getValue(), o1.getValue()));
        List<String> recommendations = new ArrayList<>();
        for (int i = 0; i < num && i < similarities.size(); i++) {
            recommendations.add(similarities.get(i).getKey());
        }
        return recommendations;
    }

    // 计算向量之间的余弦相似度
    private double cosineSimilarity(double[] vector1, double[] vector2) {
        double dotProduct = 0;
        double norm1 = 0;
        double norm2 = 0;
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += Math.pow(vector1[i], 2);
            norm2 += Math.pow(vector2[i], 2);
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
