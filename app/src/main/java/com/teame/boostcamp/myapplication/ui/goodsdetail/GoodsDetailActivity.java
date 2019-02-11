package com.teame.boostcamp.myapplication.ui.goodsdetail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.teame.boostcamp.myapplication.R;
import com.teame.boostcamp.myapplication.adapter.GoodsDetailRecyclerAdapter;
import com.teame.boostcamp.myapplication.adapter.OnItemClickListener;
import com.teame.boostcamp.myapplication.databinding.ActivityGoodsDetailBinding;
import com.teame.boostcamp.myapplication.model.entitiy.Goods;
import com.teame.boostcamp.myapplication.model.repository.GoodsDetailRepository;
import com.teame.boostcamp.myapplication.ui.base.BaseMVPActivity;
import com.teame.boostcamp.myapplication.util.DLogUtil;
import com.teame.boostcamp.myapplication.util.DividerItemDecorator;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GoodsDetailActivity extends BaseMVPActivity<ActivityGoodsDetailBinding, GoodsDetailContract.Presenter> implements GoodsDetailContract.View, View.OnClickListener {

    // 테스트 Default값
    private final static String EXTRA_GOODS = "EXTRA_GOODS";

    @Override
    protected GoodsDetailContract.Presenter getPresenter() {
        GoodsDetailRepository repository = GoodsDetailRepository.getInstance();
        return new GoodsDetailPresenter(this, repository);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_goods_detail;
    }

    public static void startActivity(Context context, Goods item) {
        Intent intent = new Intent(context, GoodsDetailActivity.class);
        intent.putExtra(EXTRA_GOODS, item);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        presenter.onDetach();
    }

    private void initView() {
        Intent intent = getIntent();
        final Goods item;
        item = intent.getParcelableExtra(EXTRA_GOODS);

        binding.setItem(item);
        // 밑줄 넣기
        binding.tvItemMinPrice.setPaintFlags(binding.tvItemMinPrice.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        GoodsDetailRecyclerAdapter adapter = new GoodsDetailRecyclerAdapter();
        adapter.setOnItemDeleteListener((v, position) -> {
            presenter.deleteReply(item.getKey(), position);
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                RecyclerView.VERTICAL,
                false);
        binding.rvReplyList.setLayoutManager(linearLayoutManager);
        binding.rvReplyList.setAdapter(adapter);
        RecyclerView.ItemDecoration dividerItemDecoration =
                new DividerItemDecorator(ContextCompat.getDrawable(getBaseContext()
                        , R.drawable.divider_decoration));
        binding.rvReplyList.addItemDecoration(dividerItemDecoration);

        presenter.loadReplyList(adapter, item.getKey());
        //Defailt 5점
        binding.etReview.setStarCount(5);
        binding.etReview.includeSelectRation.ivStar1.setOnClickListener(this);
        binding.etReview.includeSelectRation.ivStar2.setOnClickListener(this);
        binding.etReview.includeSelectRation.ivStar3.setOnClickListener(this);
        binding.etReview.includeSelectRation.ivStar4.setOnClickListener(this);
        binding.etReview.includeSelectRation.ivStar5.setOnClickListener(this);

        binding.tvItemMinPrice.setOnClickListener(view -> {
            Intent LpriceIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink()));
            startActivity(LpriceIntent);
        });

        binding.etReview.tvWriteReply.setOnClickListener(view -> {
            if (binding.etReview.tieWriteReview.getVisibility() == View.VISIBLE) {
                String replyText = binding.etReview.tieWriteReview.getText().toString();

                if (replyText.trim().length() >= 5) {
                    int ratio = binding.etReview.getStarCount();
                    String content = binding.etReview.tieWriteReview.getText().toString();
                    binding.etReview.setIsExtend(false);
                    hideSoftKeyboard(GoodsDetailActivity.this);

                    // TODO : key값 조정
                    presenter.writeReply(item.getKey(), content, ratio);
                } else {
                    showToast(getString(R.string.notice_reply_length));
                }
               }
        });

        binding.etReview.tvHintWrite.setOnClickListener(__ -> {
            DLogUtil.d("click");
            binding.viewFake.setVisibility(View.VISIBLE);
            binding.etReview.setIsExtend(true);
            binding.etReview.tieWriteReview.post(() -> {
                binding.etReview.tieWriteReview.setFocusableInTouchMode(true);
                binding.etReview.tieWriteReview.requestFocus();
                showKeyboard(this);
            });
        });
        binding.etReview.tieWriteReview.setOnClickListener(__ -> {
            binding.viewFake.setVisibility(View.VISIBLE);
        });
        binding.viewFake.setOnTouchListener((v, event) -> {
            DLogUtil.e("test");
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    binding.etReview.setIsExtend(false);
                    hideSoftKeyboard(GoodsDetailActivity.this);
                    binding.viewFake.setVisibility(View.GONE);
                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public static void showKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                    InputMethodManager.SHOW_FORCED, 0);
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void successWriteItem() {
        //TODO  스크롤 최상단으로
        binding.rvReplyList.smoothScrollToPosition(0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_star1:
                binding.etReview.setStarCount(1);
                break;
            case R.id.iv_star2:
                binding.etReview.setStarCount(2);
                break;
            case R.id.iv_star3:
                binding.etReview.setStarCount(3);
                break;
            case R.id.iv_star4:
                binding.etReview.setStarCount(4);
                break;
            case R.id.iv_star5:
                binding.etReview.setStarCount(5);
                break;

        }
    }
}
