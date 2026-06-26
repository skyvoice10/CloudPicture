package com.example.yupicturebackend.utils;

import java.awt.Color;

/**
 * 颜色相似度工具类
 */
public class ColorSimilarUtils {

    /**
     * 计算两个颜色的欧氏距离（RGB 空间）
     */
    public static double colorDistance(Color c1, Color c2) {
        int r1 = c1.getRed(), g1 = c1.getGreen(), b1 = c1.getBlue();
        int r2 = c2.getRed(), g2 = c2.getGreen(), b2 = c2.getBlue();

        return Math.sqrt(
                Math.pow(r1 - r2, 2) +
                        Math.pow(g1 - g2, 2) +
                        Math.pow(b1 - b2, 2)
        );
    }

    /**
     * 判断颜色是否相似（阈值越小越严格）
     * 推荐阈值：20~50
     */
    public static boolean isSimilar(Color c1, Color c2, double threshold) {
        return colorDistance(c1, c2) <= threshold;
    }




    /**
     * 示例测试
     */
    public static void main(String[] args) {
        Color c1 = new Color(255, 0, 0);
        Color c2 = new Color(250, 10, 10);

        System.out.println("欧氏距离: " + colorDistance(c1, c2));
    }
}
