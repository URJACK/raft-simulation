package com.sicnu.netsimu.ui.scene;

import com.sicnu.netsimu.annotation.AllowNull;
import com.sicnu.netsimu.exception.ParameterException;

/**
 * 线性排布场景
 */
public class LinearAddNodeScene extends Scene {
    /**
     * @param filePath 需要被创建的 commands 文件路径
     */
    public LinearAddNodeScene(String filePath) {
        super(filePath);
    }

    /**
     * 生成命令集合，为之后的output()打下基础
     *
     * @param config 生成时使用的相关配置
     */
    @Override
    public void generateCommands(@AllowNull Config config) {
        //情况之前的命令
        clearCommandGenerators();
        //生成节点的命令
        if (!(config instanceof LinearConfig)) {
            //配置文件不正确
            new ParameterException("configuration not matched").printStackTrace();
            return;
        }
        LinearConfig linearConfig = (LinearConfig) config;
        if (linearConfig.left >= linearConfig.right) {
            new ParameterException("linearConfig.left must be smaller than right").printStackTrace();
            return;
        }
        /*
        如果left = 0, right = 20, nodeNum = 4
        |   .   .   .   .   |
        left                right
        我们在分段的时候要分成比nodeNum多一段
         */
        float wholeArea = linearConfig.right - linearConfig.left;
        float span = wholeArea / (linearConfig.nodeNum + 1);
        int idOffset = linearConfig.beginIdOffset;
        for (int i = 1; i <= linearConfig.nodeNum; i++) {
            float x = linearConfig.left + span * i;
            float y = linearConfig.y;
            addCommandGenerator(new CommandGenerator.NodeAddCommandGenerator(linearConfig.time, i + idOffset,
                    x, y, linearConfig.nodeClass, linearConfig.args));
        }
    }

    /**
     * 线性分布的配置
     */
    public static class LinearConfig extends Config {
        long time;
        int beginIdOffset;
        int nodeNum;
        float left;
        float right;
        float y;
        Class nodeClass;
        String[] args;

        /**
         * @param time          节点添加的时间
         * @param beginIdOffset 生成节点的时候，需要增加的偏移量，默认一般是0，添加的节点编号一般是 [1, nodeNum]
         *                      <p>
         *                      如果偏移量是5，添加的节点编号 [1 + 5 , nodeNum + 5]
         * @param nodeNum       生成的节点个数
         * @param left          线性分布的左边界
         * @param right         线性分布的右边界
         * @param y             线性分布的y坐标
         * @param nodeClass     需要被添加的结点类型
         * @param args          被添加节点需要增加的额外参数
         */
        public LinearConfig(long time, int beginIdOffset, int nodeNum, float left, float right, float y, Class nodeClass, String[] args) {
            this.time = time;
            this.beginIdOffset = beginIdOffset;
            this.nodeNum = nodeNum;
            this.left = left;
            this.right = right;
            this.y = y;
            this.nodeClass = nodeClass;
            this.args = args;
        }
    }
}
