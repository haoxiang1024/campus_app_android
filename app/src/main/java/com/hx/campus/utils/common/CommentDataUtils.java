package com.hx.campus.utils.common;

import com.hx.campus.adapter.entity.Comment;
import java.util.ArrayList;
import java.util.List;

/**
 * 评论数据处理工具类
 */
public class CommentDataUtils {

    /**
     * 将多级嵌套评论转化为二级扁平结构
     *
     * @param originalList 原始后端数据列表
     * @return 转换后的扁平化数据列表
     */
    public static List<Comment> flattenComments(List<Comment> originalList) {
        List<Comment> result = new ArrayList<>();
        if (originalList == null) {
            return result;
        }

        for (Comment mainComment : originalList) {
            List<Comment> flatReplies = new ArrayList<>();
            collectAllReplies(mainComment.getReplies(), flatReplies);
            mainComment.setReplies(flatReplies);
            result.add(mainComment);
        }
        return result;
    }

    /**
     * 递归收集所有子评论
     *
     * @param source 原始子评论列表
     * @param target 收集结果的扁平列表
     */
    private static void collectAllReplies(List<Comment> source, List<Comment> target) {
        if (source == null || source.isEmpty()) {
            return;
        }

        for (Comment reply : source) {
            target.add(reply);
            collectAllReplies(reply.getReplies(), target);
            reply.setReplies(new ArrayList<>());
        }
    }
}
