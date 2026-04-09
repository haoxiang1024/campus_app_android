package com.hx.campus.fragment.look;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
import com.hx.campus.databinding.FragmentLostInfoDetailBinding;
import com.hx.campus.utils.Utils;
import com.hx.campus.utils.api.Result;
import com.hx.campus.utils.api.RetrofitClient;
import com.hx.campus.utils.common.CommentDataUtils;
import com.xuexiang.xpage.annotation.Page;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Page()
public class LostInfoDetailFragment extends BaseFragment<FragmentLostInfoDetailBinding> {

    public static final String KEY_LOST = "lost";
    LostFound lost;
    private CommentAdapter commentAdapter;
    private int currentParentId = 0;
    private int currentReplyUserId = 0;

    @Override
    protected void initArgs() {
        super.initArgs();
        if (getArguments() != null) {
            lost = (LostFound) getArguments().getSerializable(KEY_LOST);
        }
    }

    @Override
    protected String getPageTitle() {
        return getResources().getString(R.string.lost_info_detail);
    }

    @NonNull
    @Override
    protected FragmentLostInfoDetailBinding viewBindingInflate(@NonNull LayoutInflater inflater, ViewGroup container, boolean attachToRoot) {
        return FragmentLostInfoDetailBinding.inflate(inflater, container, attachToRoot);
    }

    @Override
    protected void initViews() {
        if (lost != null) {
            setViews();
            initSpinnerListener();
            updateSubmitBtnStatus(binding.state.getSelectedItem().toString());

            if (binding.sumbitBtn != null) {
                binding.sumbitBtn.setOnClickListener(v -> {
                    String selected = binding.state.getSelectedItem().toString();
                    submitState(selected);
                });
            }
        } else {
            Log.e("Check", "错误：lost 数据为空");
        }
        initCommentList();
        initCommentEvent();
        initEmojiPanel();

        if (binding.btnSharePoster != null) {
            binding.btnSharePoster.setOnClickListener(v -> {
                Utils.showResponse("正在生成分享海报...");
                sharePoster();
            });
        }
    }

    private void initSpinnerListener() {
        binding.state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = parent.getItemAtPosition(position).toString();
                updateSubmitBtnStatus(selectedState);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (binding.sumbitBtn != null) {
                    binding.sumbitBtn.setEnabled(false);
                    binding.sumbitBtn.setBackgroundColor(Color.parseColor("#CCCCCC"));
                }
            }
        });
    }

    private void updateSubmitBtnStatus(String selectedState) {
        if (binding.sumbitBtn == null) return;

        boolean isDisabled = "待审核".equals(selectedState) || "已驳回".equals(selectedState);

        binding.sumbitBtn.setEnabled(!isDisabled);

        if (isDisabled) {
            binding.sumbitBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
        } else {
            int primaryColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary);
            binding.sumbitBtn.setBackgroundTintList(ColorStateList.valueOf(primaryColor));
        }
    }

    private void loadComments() {
        if (lost == null) return;

        RetrofitClient.getInstance().getApi().getComments(lost.getId()).enqueue(new Callback<Result<List<Comment>>>() {
            @Override
            public void onResponse(Call<Result<List<Comment>>> call, Response<Result<List<Comment>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<List<Comment>> serverResponse = response.body();
                    if (serverResponse.getStatus() == 0) {
                        commentAdapter.setNewData(CommentDataUtils.flattenComments(serverResponse.getData()));
                    }
                }
            }

            @Override
            public void onFailure(Call<Result<List<Comment>>> call, Throwable t) {
                Utils.showResponse("网络异常");
            }
        });
    }

    private void initEmojiPanel() {
        String[] emojis = {
                "😀","😂","🤣","😅","😊","😍","😘","😜",
                "😝","🤩","😔","😢","😭","😡","🤯","👍",
                "👎","🙏","🤝","👏","🔥","💯","❤️","💔"
        };

        GridLayout gridLayout = new GridLayout(getContext());
        gridLayout.setColumnCount(8);
        gridLayout.setBackgroundColor(Color.parseColor("#F5F6F9"));
        gridLayout.setPadding(16, 16, 16, 16);

        for (String emoji : emojis) {
            TextView tv = new TextView(getContext());
            tv.setText(emoji);
            tv.setTextSize(26);
            tv.setPadding(12, 12, 12, 12);
            tv.setOnClickListener(v -> {
                int cursor = binding.etCommentInput.getSelectionStart();
                binding.etCommentInput.getText().insert(cursor, emoji);
            });
            gridLayout.addView(tv);
        }

        PopupWindow emojiPopup = new PopupWindow(gridLayout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        emojiPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        emojiPopup.setOutsideTouchable(true);

        binding.btnEmoji.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(binding.etCommentInput.getWindowToken(), 0);
            emojiPopup.showAsDropDown(binding.btnEmoji, 0, - (binding.btnEmoji.getHeight() + 500));
        });

        binding.etCommentInput.setOnClickListener(v -> {
            if (emojiPopup.isShowing()) {
                emojiPopup.dismiss();
            }
        });
    }

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
            submitComment(lost.getId(), currentUserId, content, currentParentId, currentReplyUserId);
        });
    }

    private void submitComment(int lostfoundId, int userId, String content, int parentId, int replyUserId) {
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
                        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(binding.etCommentInput.getWindowToken(), 0);
                        }
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

        loadComments();
    }

    private void setViews() {
        binding.tvLostTitle.setText(lost.getTitle());
        binding.tvLostContent.setText(lost.getContent());
        if (TextUtils.isEmpty(lost.getImg())) {
            binding.imgLost.setVisibility(View.GONE);
        } else {
            binding.imgLost.setVisibility(View.VISIBLE);
            Glide.with(this).load(lost.getImg()).into(binding.imgLost);
        }
        binding.tvAuthor.setText(lost.getNickname());
        binding.tvPhonenum.setText(lost.getPhone());
        binding.location.setText(lost.getPlace());

        String[] statuses = {"待审核","已驳回","寻找中", "已找到"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.state.setAdapter(adapter);
        int position = Arrays.asList(statuses).indexOf(lost.getState());
        if (position >= 0) binding.state.setSelection(position);
        binding.tvDate.setText(Utils.dateFormat(lost.getPubDate()));
    }

    private void submitState(String selectedState) {
        RetrofitClient.getInstance().getApi()
                .updateState(lost.getId(), selectedState, lost.getUserId())
                .enqueue(new Callback<Result<String>>() {
                    @Override
                    public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            lost.setState(selectedState);
                            Utils.showResponse("状态已更改");
                        } else {
                            String errorMsg = (response.body() != null) ? response.body().getMsg() : "返回体为空";
                            Utils.showResponse("操作失败: " + errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<String>> call, Throwable t) {
                        Utils.showResponse("网络异常");
                    }
                });
    }

    private void sharePoster() {
        if (lost == null) {
            Utils.showResponse("数据未加载完成");
            return;
        }

        binding.posterTitle.setText(lost.getTitle());
        binding.posterContent.setText(lost.getContent());

        if (binding.imgLost.getDrawable() != null) {
            binding.posterItemImage.setImageDrawable(binding.imgLost.getDrawable());
            binding.posterItemImage.setVisibility(View.VISIBLE);
        } else {
            binding.posterItemImage.setVisibility(View.GONE);
        }

        String baseUrl = Utils.getUrlFromAssets(getContext());
        String shareUrl = baseUrl + "share.html?id=" + lost.getId() + "&type=lost";

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