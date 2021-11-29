package com.sicnu.netsimu.core.utils;

public class StringUtils {
    /**
     * 将一段用户导入的输入 清理其多余的空格
     * 方便后续解析命令
     *
     * @param commandText 单个命令的字符串
     * @return 清理完空格后的字符串
     */
    public static String clearRedundant(String commandText) {
        StringBuilder sb = new StringBuilder();
        int commentCount = 0;
        for (int i = 0; i < commandText.length(); i++) {
            char c = commandText.charAt(i);
            if (c == '/') {
                commentCount++;
                if (commentCount == 2) {
                    return sb.toString();
                }
                continue;
            }
            if (c == ' ' || c == '\t') {
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
