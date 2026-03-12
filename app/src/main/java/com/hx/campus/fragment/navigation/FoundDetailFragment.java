package com.hx.campus.fragment.navigation;

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
import com.hx.campus.adapter.entity.LostFound;
import com.hx.campus.adapter.entity.User;
import com.hx.campus.core.BaseFragment;
import com.hx.campus.databinding.FragmentFoundDetailBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.xuexiang.xpage.annotation.Page;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page
public class FoundDetailFragment extends BaseFragment<FragmentFoundDetailBinding> {
    public static final String KEY_FOUND = "found";

    private CommentAdapter commentAdapter;
    LostFound found;

    private int currentParentId = 0;      // 0 代表直接评论失物招领帖子
    private int currentReplyUserId = 0;   // 0 代表没有回复特定的人

    /**
     * 构建ViewBinding
     */
    @NonNull
    @Override
    protected FragmentFoundDetailBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot)  {
        return FragmentFoundDetailBinding.inflate(inflater, container, attachToRoot);
    }

    /**
     * 初始化参数
     */
    @Override
    protected void initArgs() {
        super.initArgs();
        if (getArguments() != null) {
            found = (LostFound) getArguments().getSerializable(KEY_FOUND);
        }
    }

    /**
     * 获取页面标题
     */
    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.detail);
    }

    /**
     * 初始化控件
     */
    @Override
    protected void initViews() {
        setViews();         //设置控件
        initCommentList();  // 初始化评论列表
        initCommentEvent(); // 初始化发送评论事件
        initEmojiPanel();

        if (binding.btnSharePoster != null) {
            binding.btnSharePoster.setOnClickListener(v -> {
                // 提示用户正在生成
                Utils.showResponse("正在生成分享海报...");
                // 调用分享方法
                sharePoster();
            });
        }
    }

    private void initEmojiPanel() {
        // 常用的自带 Emoji 列表
        String[] emojis = {
                "😀","😂","🤣","😅","😊","😍","😘","😜",
                "😝","🤩","😔","😢","😭","😡","🤯","👍",
                "👎","🙏","🤝","👏","🔥","💯","❤️","💔"
        };

        // 动态创建一个简单的网格布局放表情
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
                // 点击表情，直接插入到输入框当前光标位置
                int cursor = binding.etCommentInput.getSelectionStart();
                binding.etCommentInput.getText().insert(cursor, emoji);
            });
            gridLayout.addView(tv);
        }

        // 用 PopupWindow 包装这个面板
        PopupWindow emojiPopup = new PopupWindow(gridLayout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true); // true 允许点击外部消失
        emojiPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        emojiPopup.setOutsideTouchable(true);

        // 点击表情按钮弹出
        binding.btnEmoji.setOnClickListener(v -> {
            // 隐藏软键盘
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(binding.etCommentInput.getWindowToken(), 0);

            // 在输入框上方或下方弹出
            emojiPopup.showAsDropDown(binding.btnEmoji, 0, - (binding.btnEmoji.getHeight() + 400));
        });

        // 当用户点击输入框时，如果表情面板开着就把它关掉
        binding.etCommentInput.setOnClickListener(v -> {
            if (emojiPopup.isShowing()) {
                emojiPopup.dismiss();
            }
        });
    }

    /**
     * 初始化评论列表和 RecyclerView
     */
    private void initCommentList() {
        commentAdapter = new CommentAdapter();
        binding.rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvComments.setAdapter(commentAdapter);

        commentAdapter.setOnCommentClickListener((parentId, targetUserId, targetNickname) -> {
            currentParentId = parentId;
            currentReplyUserId = targetUserId;
            binding.etCommentInput.setHint("回复 " + targetNickname + "...");
            binding.etCommentInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(binding.etCommentInput, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // 加载评论数据
        loadComments();
    }

    /**
     * 发起网络请求获取评论
     */
    private void loadComments() {
        if (found == null) return;

        RetrofitClient.getInstance().getApi().getComments(found.getId()).enqueue(new Callback<Result<List<Comment>>>() {
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
                Utils.showResponse("网络异常");
            }
        });
    }

    /**
     * 初始化发送评论按钮的点击事件
     */
    private void initCommentEvent() {
        binding.btnSendComment.setOnClickListener(v -> {
            String content = binding.etCommentInput.getText().toString().trim();
            if (TextUtils.isEmpty(content)) {
                Utils.showResponse("评论内容不能为空");
                return;
            }

            User user = Utils.getBeanFromSp(getContext(), "User", "user");
            if (user == null) {
                Utils.showResponse("请先登录");
                return;
            }
            int currentUserId = user.getId();
            submitComment(found.getId(), currentUserId, content, currentParentId, currentReplyUserId);
        });
    }

    /**
     * 提交评论到后端
     */
    private void submitComment(int lostfoundId, int userId, String content, int parentId, int replyUserId) {
        // 禁用按钮防连点
        binding.btnSendComment.setEnabled(false);
        RetrofitClient.getInstance().getApi().addComment(lostfoundId, userId, content, parentId, replyUserId).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                binding.btnSendComment.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getStatus() == 0) {
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

                        // 重新加载评论列表刷新 UI
                        loadComments();
                    } else {
                        Utils.showResponse(response.body().getMsg());
                    }
                }
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                binding.btnSendComment.setEnabled(true);
                Utils.showResponse("网络异常");
            }
        });
    }

    private void setViews() {
        //设置标题
        binding.tvLostTitle.setText(found.getTitle());
        //设置内容
        binding.tvLostContent.setText(found.getContent());
        //加载图片
        if (TextUtils.isEmpty(found.getImg())) {
            binding.imgLost.setVisibility(View.GONE);
        } else {
            binding.imgLost.setVisibility(View.VISIBLE);
            Glide.with(this).load(found.getImg()).into(binding.imgLost);
        }
        //设置失主名称
        binding.tvAuthor.setText(found.getNickname());
        //设置联系方式
        binding.tvPhonenum.setText(found.getPhone());
        //设置地点
        binding.location.setText(found.getPlace());
        //设置状态
        binding.state.setText(found.getState());
        //设置发布日期
        String date = Utils.dateFormat(found.getPubDate());
        binding.tvDate.setText(date);
        //私信
        binding.chatBtn.setOnClickListener(v -> {
            String targetId = String.valueOf(found.getUserId());
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
        if (found == null) {
            Utils.showResponse("数据未加载完成");
            return;
        }

        // 填充海报数据
        binding.posterTitle.setText(found.getTitle());
        binding.posterContent.setText(found.getContent());
        String baseUrl = Utils.getUrlFromAssets(getContext());
        String shareUrl = baseUrl + "share.html?id=" + found.getId() + "&type=found";

        // 生成二维码并贴到海报上
        Bitmap qrBitmap = generateQRCode(shareUrl, 400, 400);
        if (qrBitmap != null) {
            binding.posterQrcode.setImageBitmap(qrBitmap);
        }

        // 延迟一下等待 View 渲染，然后截图分享
        binding.layoutPoster.post(() -> {
            Bitmap posterBitmap = createBitmapFromView(binding.layoutPoster);
            if (posterBitmap != null) {
                saveAndShareImage(posterBitmap);
            } else {
                Utils.showResponse("海报生成失败");
            }
        });
    }

    /**
     * 使用 ZXing 生成二维码 Bitmap
     */
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

    /**
     * 将 View 转化为 Bitmap
     */
    private Bitmap createBitmapFromView(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(view.getLayoutParams().width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * 保存图片并调用系统分享
     */
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

                // 通知图库刷新，否则相册里不能立刻看到
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(galleryFile));
                requireContext().sendBroadcast(mediaScanIntent);
            }

            // 执行相册写入
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

            // 构造标准分享 Intent
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