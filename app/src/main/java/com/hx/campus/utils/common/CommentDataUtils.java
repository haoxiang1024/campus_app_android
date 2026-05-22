package com.hx.campus.utils.common;

import com.hx.campus.adapter.entity.Comment;
import java.util.ArrayList;
import java.util.List;


public class CommentDataUtils {

    
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
