package com.hx.campus.fragment.other;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.hx.campus.R;
import com.hx.campus.adapter.comment.CommentAdapter;
import com.hx.campus.adapter.entity.Comment;
import com.hx.campus.adapter.entity.SearchInfo;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentSearchInfoBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xrouter.annotation.AutoWired;
import com.xuexiang.xrouter.launcher.XRouter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page(name = "详情")
public class SearchInfoFragment extends BaseFragment<FragmentSearchInfoBinding> {

    public static final String KEY_INFO = "info";

    @AutoWired(name = KEY_INFO)
    SearchInfo searchInfo;//实体类不能序列化，否则无法注入

    private CommentAdapter commentAdapter;
    // 评论回复相关参数：0 代表直接评论帖子，非0代表回复对应ID的评论/用户
    private int currentParentId = 0;
    private int currentReplyUserId = 0;
    private PopupWindow emojiPopup; // Emoji面板弹窗

    /**
     * 初始化参数
     */
    @Override
    protected void initArgs() {
        super.initArgs();
        XRouter.getInstance().inject(this);
    }

    /**
     * 构建ViewBinding
     *
     * @param inflater  inflater
     * @param container 容器
     * @return ViewBinding
     */
    @NonNull
    @Override
    protected FragmentSearchInfoBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentSearchInfoBinding.inflate(inflater, container, attachToRoot);
    }

    /**
     * 初始化控件
     */
    @Override
    protected void initViews() {
        setData();
        initCommentList();  // 初始化评论列表
        initCommentEvent(); // 初始化评论发送事件
        initEmojiPanel();   // 初始化Emoji面板

        if (binding.btnSharePoster != null) {
            binding.btnSharePoster.setOnClickListener(v -> {
                Utils.showResponse("正在生成分享海报...");
                sharePoster();
            });
        }
    }

    /**
     * 初始化Emoji面板
     */
    private void initEmojiPanel() {
        // 常用Emoji列表
        String[] emojis = {
                "😀","😂","🤣","😅","😊","😍","😘","😜",
                "😝","🤩","😔","😢","😭","😡","🤯","👍",
                "👎","🙏","🤝","👏","🔥","💯","❤️","💔"
        };

        // 构建Emoji网格布局
        GridLayout gridLayout = new GridLayout(getContext());
        gridLayout.setColumnCount(8); // 每行8个表情
        gridLayout.setBackgroundColor(Color.parseColor("#F5F6F9"));
        gridLayout.setPadding(16, 16, 16, 16);

        for (String emoji : emojis) {
            TextView tv = new TextView(getContext());
            tv.setText(emoji);
            tv.setTextSize(26);
            tv.setPadding(12, 12, 12, 12);
            tv.setOnClickListener(v -> {
                // 插入Emoji到输入框光标位置
                int cursor = binding.etCommentInput.getSelectionStart();
                binding.etCommentInput.getText().insert(cursor, emoji);
            });
            gridLayout.addView(tv);
        }

        // 包装成PopupWindow
        emojiPopup = new PopupWindow(gridLayout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        emojiPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        emojiPopup.setOutsideTouchable(true);

        // Emoji按钮点击事件
        binding.btnEmoji.setOnClickListener(v -> {
            // 隐藏软键盘
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(binding.etCommentInput.getWindowToken(), 0);

            // 弹出Emoji面板
            emojiPopup.showAsDropDown(binding.btnEmoji, 0, - (binding.btnEmoji.getHeight() + 500));
        });

        // 输入框点击关闭Emoji面板
        binding.etCommentInput.setOnClickListener(v -> {
            if (emojiPopup.isShowing()) {
                emojiPopup.dismiss();
            }
        });
    }

    /**
     * 初始化评论列表
     */
    private void initCommentList() {
        commentAdapter = new CommentAdapter();
        binding.rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvComments.setAdapter(commentAdapter);

        // 评论回复点击事件
        commentAdapter.setOnCommentClickListener((parentId, targetUserId, targetNickname) -> {
            currentParentId = parentId;
            currentReplyUserId = targetUserId;
            binding.etCommentInput.setHint("回复 " + targetNickname + "...");
            binding.etCommentInput.requestFocus();
            // 弹出软键盘
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(binding.etCommentInput, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // 加载评论数据
        loadComments();
    }

    /**
     * 加载评论数据
     */
    private void loadComments() {
        if (searchInfo == null || searchInfo.getId() == 0) return;

        RetrofitClient.getInstance().getApi().getComments(searchInfo.getId()).enqueue(new Callback<Result<List<Comment>>>() {
            @Override
            public void onResponse(Call<Result<List<Comment>>> call, Response<Result<List<Comment>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<List<Comment>> serverResponse = response.body();
                    if (serverResponse.getStatus() == 0) {
                        commentAdapter.setNewData(serverResponse.getData());
                    }
                }
            }

            @Override
            public void onFailure(Call<Result<List<Comment>>> call, Throwable t) {
                Utils.showResponse("网络异常，加载评论失败");
            }
        });
    }

    /**
     * 初始化评论发送事件
     */
    private void initCommentEvent() {
        binding.btnSendComment.setOnClickListener(v -> {
            String content = binding.etCommentInput.getText().toString().trim();
            if (TextUtils.isEmpty(content)) {
                Utils.showResponse("评论内容不能为空");
                return;
            }

            // 获取当前登录用户
            User user = Utils.getBeanFromSp(getContext(), "User", "user");
            if (user == null) {
                Utils.showResponse("请先登录");
                return;
            }

            // 提交评论
            submitComment(searchInfo.getId(), user.getId(), content, currentParentId, currentReplyUserId);
        });
    }

    /**
     * 提交评论到后端
     */
    private void submitComment(int searchInfoId, int userId, String content, int parentId, int replyUserId) {
        // 禁用按钮防重复点击
        binding.btnSendComment.setEnabled(false);

        RetrofitClient.getInstance().getApi()
                .addComment(searchInfoId, userId, content, parentId, replyUserId)
                .enqueue(new Callback<Result<String>>() {
                    @Override
                    public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                        binding.btnSendComment.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            Result<String> result = response.body();
                            if (result.getStatus() == 0) {
                                // 清空输入框、重置回复状态
                                binding.etCommentInput.setText("");
                                binding.etCommentInput.clearFocus();
                                binding.etCommentInput.setHint("写下你的评论...");
                                currentParentId = 0;
                                currentReplyUserId = 0;

                                // 隐藏软键盘
                                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (imm != null) {
                                    imm.hideSoftInputFromWindow(binding.etCommentInput.getWindowToken(), 0);
                                }

                                // 刷新评论列表
                                loadComments();
                                Utils.showResponse("评论发布成功");
                            } else {
                                Utils.showResponse(result.getMsg());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<String>> call, Throwable t) {
                        binding.btnSendComment.setEnabled(true);
                        Utils.showResponse("网络异常，发布评论失败");
                    }
                });
    }

    /**
     * 填充基础数据
     */
    private void setData() {
        if (searchInfo == null) return;

        //设置标题
        binding.tvLostTitle.setText(searchInfo.getTitle());
        //设置内容
        binding.tvLostContent.setText(searchInfo.getContent());
        //加载图片
        if (TextUtils.isEmpty(searchInfo.getImg())) {
            binding.imgLost.setVisibility(View.GONE);
        } else {
            binding.imgLost.setVisibility(View.VISIBLE);
            Glide.with(this).load(searchInfo.getImg()).into(binding.imgLost);
        }
        //设置失主名称
        binding.tvAuthor.setText(searchInfo.getNickname());
        //设置联系方式
        binding.tvPhonenum.setText(searchInfo.getPhone());
        //设置地点
        binding.location.setText(searchInfo.getPlace());
        //设置状态
        binding.state.setText(searchInfo.getState());
        //设置发布日期
        String date = Utils.dateFormat(searchInfo.getPub_date());
        binding.tvDate.setText(date);
        //私信
        binding.chatBtn.setOnClickListener(v -> {
            String targetId = String.valueOf(searchInfo.getUser_id());
            if (TextUtils.isEmpty(targetId)) {
                return;
            }
            io.rong.imkit.utils.RouteUtils.routeToConversationActivity(
                    getContext(),
                    io.rong.imlib.model.Conversation.ConversationType.PRIVATE,
                    targetId
            );
        });
    }

    private void sharePoster() {
        if (searchInfo == null) {
            Utils.showResponse("数据未加载完成");
            return;
        }

        binding.posterTitle.setText(searchInfo.getTitle());
        binding.posterContent.setText(searchInfo.getContent());

        if (binding.imgLost.getDrawable() != null) {
            binding.posterItemImage.setImageDrawable(binding.imgLost.getDrawable());
            binding.posterItemImage.setVisibility(View.VISIBLE);
        } else {
            binding.posterItemImage.setVisibility(View.GONE);
        }

        String baseUrl = Utils.getUrlFromAssets(getContext());
        String shareUrl = baseUrl + "share.html?id=" + searchInfo.getId() + "&type=" + (searchInfo.getType().equals("失物") ? "lost" : "found");

        Bitmap qrBitmap = generateQRCode(shareUrl, 400, 400);
        if (qrBitmap != null) {
            binding.posterQrcode.setImageBitmap(qrBitmap);
        }

        binding.layoutPoster.post(() -> {
            Bitmap posterBitmap = createBitmapFromView(binding.layoutPoster);
            if (posterBitmap != null) {
                saveAndShareImage(posterBitmap);
            } else {
                Utils.showResponse("海报生成失败");
            }
        });
    }

    private Bitmap generateQRCode(String text, int width, int height) {
        try {
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix bitMatrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixels[y * width + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap createBitmapFromView(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(view.getLayoutParams().width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void saveAndShareImage(Bitmap bitmap) {
        if (bitmap == null) {
            Utils.showResponse("图片生成异常，请重试");
            return;
        }

        try {
            String fileName = "Campus_Poster_" + System.currentTimeMillis() + ".png";
            OutputStream galleryStream = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Campus");

                Uri galleryUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (galleryUri != null) {
                    galleryStream = requireContext().getContentResolver().openOutputStream(galleryUri);
                }
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                File galleryFile = new File(imagesDir, fileName);
                galleryStream = new FileOutputStream(galleryFile);

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(galleryFile));
                requireContext().sendBroadcast(mediaScanIntent);
            }

            if (galleryStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, galleryStream);
                galleryStream.flush();
                galleryStream.close();
                Utils.showResponse("已保存到相册，正在拉起分享...");
            }

            File cachePath = new File(requireContext().getCacheDir(), "images");
            if (!cachePath.exists()) {
                cachePath.mkdirs();
            }
            File shareFile = new File(cachePath, "share_poster.png");
            FileOutputStream shareStream = new FileOutputStream(shareFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, shareStream);
            shareStream.flush();
            shareStream.close();

            Uri shareUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", shareFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooserIntent = Intent.createChooser(shareIntent, "分享寻物启事海报");
            chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(chooserIntent);

        } catch (Exception e) {
            e.printStackTrace();
            Utils.showResponse("操作异常，请检查权限配置");
        }
    }
}