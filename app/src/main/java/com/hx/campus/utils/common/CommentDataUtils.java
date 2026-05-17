package com.hx.campus.utils.common;

import com.hx.campus.adapter.entity.Comment;
import java.util.ArrayList;
import java.util.List;


public class CommentDataUtils {

    
    public static List<Comment> flattenComments(List<Comment> originalList) {
        //实现了评论树的扁平化处理 遍历所有一级评论，对每个一级评论的子回复进行深度优先的递归收集。
        //扁平化后，一级评论的replies字段不再是嵌套的树形结构，而是一个按展示顺序排列的平铺列表
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
