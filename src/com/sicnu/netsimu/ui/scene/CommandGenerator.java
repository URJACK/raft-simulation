package com.sicnu.netsimu.ui.scene;

import java.lang.reflect.Field;

/**
 * 命令生成器
 */
public abstract class CommandGenerator {
    // 命令转换规则
    public String convert() throws IllegalAccessException {
        Class clazz = this.getClass();
        Field[] fields = clazz.getDeclaredFields();
        StringBuilder sb = new StringBuilder();
        for (Field field : fields) {
            AutoConvert annotation = field.getAnnotation(AutoConvert.class);
            if (annotation == null) {
                //如果没有自动转换注解则忽视它
                continue;
            }
            String s;
            Class<?> type = field.getType();
            if (int.class.equals(type) || long.class.equals(type) || float.class.equals(type) || double.class.equals(type) || char.class.equals(type) || String.class.equals(type)) {
                s = field.get(this).toString();
                sb.append(s);
                sb.append(" ,");
            } else if (Class.class.equals(type)) {
                //nodeClass.toString() 我们只要第二个字符串
                //class raft.mote.RaftMote
                //raft.mote.RaftMote
                s = field.get(this).toString().split(" ")[1];
                sb.append(s);
                sb.append(" ,");
            } else if (String[].class.equals(type)) {
                String[] params = (String[]) field.get(this);
                if (params != null) {
                    for (int i = 0; i < params.length - 1; i++) {
                        sb.append(params[i]);
                        sb.append(" ,");
                    }
                    sb.append(params[params.length - 1]);
                    sb.append(" ,");
                }
            } else {
                throw new IllegalStateException("Unexpected value: " + type);
            }
        }
        //如果末尾元素是一个逗号，我们将移除该逗号
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * “增加节点命令” 生成器
     * <pre>
     *  addCommandGenerator(new CommandGenerator.NodeAddCommandGenerator(linearConfig.time, i + idOffset,
     *  x, y, linearConfig.nodeClass, linearConfig.args));
     * </pre>
     * 生成命令如下：
     * <pre>
     * 1000, NODE_ADD, 1, 50, 100, com.sicnu.netsimu.core.mote.NormalMote
     * 1000, NODE_ADD, 3, 100, 100, com.sicnu.raft.mote.RaftMote , 3
     * </pre>
     */
    public static class NodeAddCommandGenerator extends CommandGenerator {
        @AutoConvert
        private long time;
        @AutoConvert
        private String commandType;
        @AutoConvert
        private int moteId;
        @AutoConvert
        private float x;
        @AutoConvert
        private float y;
        @AutoConvert
        private Class nodeClass;
        @AutoConvert
        private String[] params;

        public NodeAddCommandGenerator(long time, int moteId, float x, float y, Class nodeClass, String[] params) {
            this.time = time;
            commandType = "NODE_ADD";
            this.moteId = moteId;
            this.x = x;
            this.y = y;
            this.nodeClass = nodeClass;
            this.params = params;
        }

        @Override
        public String convert() throws IllegalAccessException {
            return super.convert() + " // 节点添加命令";
        }
    }
}
