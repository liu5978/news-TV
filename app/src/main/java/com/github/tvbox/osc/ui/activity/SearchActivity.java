package com.github.tvbox.osc.ui.activity;

// ... 原有导入 ...
import okhttp3.*;                                                             // 新增网络库导入
import org.json.JSONObject;
import org.json.JSONException;
import com.github.tvbox.osc.util.DeepSeekHelper;                              // 新增工具类

public class SearchActivity extends BaseActivity {
    // ... 原有变量声明 ...
    private Dialog mLoadingDialog;                                            // 新增加载对话框变量

    @Override
    protected void init() {
        // ... 原有初始化代码 ...
    }

    // ... 其他原有方法 ...

    private void initView() {
        // ... 原有代码 ...

        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                hasKeyBoard = true;
                String originalQuery = etSearch.getText().toString().trim();
                if (!TextUtils.isEmpty(originalQuery)) {
                    showLoading();
                    // 调用 DeepSeek 优化搜索词
                    DeepSeekHelper.callDeepSeekAPI(
                        "优化影视搜索词，原始词：" + originalQuery + "。请输出更精准的搜索关键词（仅返回关键词）",
                        new DeepSeekHelper.ApiCallback() {
                            @Override
                            public void onSuccess(String response) {
                                try {
                                    JSONObject json = new JSONObject(response);
                                    String optimizedQuery = json.getJSONArray("choices")
                                            .getJSONObject(0)
                                            .getJSONObject("message")
                                            .getString("content")
                                            .replace("\"", "")
                                            .trim();
                                    
                                    runOnUiThread(() -> {
                                        hideLoading();
                                        if (Hawk.get(HawkConfig.FAST_SEARCH_MODE, false)) {
                                            Bundle bundle = new Bundle();
                                            bundle.putString("title", optimizedQuery);
                                            jumpActivity(FastSearchActivity.class, bundle);
                                        } else {
                                            search(optimizedQuery);
                                        }
                                    });
                                } catch (Exception e) {
                                    runOnUiThread(() -> {
                                        searchFallback(originalQuery, "解析响应失败");
                                    });
                                }
                            }

                            @Override
                            public void onFailure(String error) {
                                runOnUiThread(() -> {
                                    searchFallback(originalQuery, "API请求失败: " + error);
                                });
                            }
                        });
                } else {
                    Toast.makeText(mContext, "输入内容不能为空", Toast.LENGTH_SHORT).show();
                }
            }

            private void searchFallback(String query, String errorMsg) {
                hideLoading();
                Toast.makeText(mContext, "AI优化失败：" + errorMsg + "，使用原始搜索词", Toast.LENGTH_LONG).show();
                if (Hawk.get(HawkConfig.FAST_SEARCH_MODE, false)) {
                    Bundle bundle = new Bundle();
                    bundle.putString("title", query);
                    jumpActivity(FastSearchActivity.class, bundle);
                } else {
                    search(query);
                }
            }
        });

        // ... 其他原有代码 ...
    }

    // 新增加载对话框方法
    private void showLoading() {
        runOnUiThread(() -> {
            if (mLoadingDialog == null) {
                mLoadingDialog = new Dialog(this, R.style.LoadingDialog);
                mLoadingDialog.setContentView(R.layout.dialog_loading);
                mLoadingDialog.setCancelable(false);
            }
            mLoadingDialog.show();
        });
    }

    private void hideLoading() {
        runOnUiThread(() -> {
            if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }
        });
    }

    // ... 保留其他原有方法 ...
}
