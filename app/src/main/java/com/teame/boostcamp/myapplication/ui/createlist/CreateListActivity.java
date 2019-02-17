package com.teame.boostcamp.myapplication.ui.createlist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.airbnb.lottie.LottieDrawable;
import com.teame.boostcamp.myapplication.R;
import com.teame.boostcamp.myapplication.adapter.GoodsListRecyclerAdapter;
import com.teame.boostcamp.myapplication.databinding.ActivityCreateListBinding;
import com.teame.boostcamp.myapplication.model.entitiy.Goods;
import com.teame.boostcamp.myapplication.model.entitiy.GoodsListHeader;
import com.teame.boostcamp.myapplication.model.repository.GoodsListRepository;
import com.teame.boostcamp.myapplication.ui.base.BaseMVPActivity;
import com.teame.boostcamp.myapplication.ui.createlistinfo.CreateListInfo;
import com.teame.boostcamp.myapplication.ui.goodscart.GoodsCartActivity;
import com.teame.boostcamp.myapplication.ui.goodsdetail.GoodsDetailActivity;
import com.teame.boostcamp.myapplication.util.Constant;
import com.teame.boostcamp.myapplication.util.DLogUtil;
import com.teame.boostcamp.myapplication.util.view.ListSpaceItemDecoration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.disposables.CompositeDisposable;

public class CreateListActivity extends BaseMVPActivity<ActivityCreateListBinding, CreateListContract.Presenter> implements CreateListContract.View {

    private static final String EXTRA_GOODS_LIST_HDAER = "EXTRA_GOODS_LIST_HDAER";
    private static final String EXTRA_SELECTED_GOODS_LIST = "EXTRA_SELECTED_GOODS_LIST";
    private static final int SCROLL_DIRECTION_UP = -1;
    private AppCompatTextView tvBadge;

    private SearchView svGoods;
    CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected CreateListContract.Presenter getPresenter() {
        return new CreateListPresenter(this, GoodsListRepository.getInstance());
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_create_list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cart, menu);
        final MenuItem showCartItem = menu.findItem(R.id.btn_show_cart);
        final MenuItem goodsSearchItem = menu.findItem(R.id.goods_search);
        View cartItemActionView = showCartItem.getActionView();
        View goodsSearchItemActionView = goodsSearchItem.getActionView();
        tvBadge = cartItemActionView.findViewById(R.id.cart_badge);
        tvBadge.setVisibility(View.GONE);
        svGoods = goodsSearchItemActionView.findViewById(R.id.goods_search);
        ImageView icon = svGoods.findViewById(androidx.appcompat.R.id.search_button);
        icon.setColorFilter(Color.BLACK);
        ImageView iconClose = svGoods.findViewById(androidx.appcompat.R.id.search_close_btn);
        iconClose.setColorFilter(Color.BLACK);

        EditText editText = svGoods.findViewById(androidx.appcompat.R.id.search_src_text);
        editText.setHintTextColor(ContextCompat.getColor(this,R.color.colorIphoneBlack));
        editText.setTextColor(Color.BLACK);
        binding.toolbarTitle.setTextSize(editText.getTextSize());
        svGoods.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Toast like print
                showToast("SearchOnQueryTextSubmit: " + query);
                if (!svGoods.isIconified()) {
                    svGoods.setIconified(true);
                }
                svGoods.onActionViewCollapsed();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // UserFeedback.show( "SearchOnQueryTextChanged: " + s);
                return false;
            }
        });

        presenter.getShoppingListCount();

        cartItemActionView.setOnClickListener(v -> onOptionsItemSelected(showCartItem));
        goodsSearchItemActionView.setOnClickListener(v -> onOptionsItemSelected(showCartItem));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_show_cart:
                GoodsCartActivity.startActivity(this);
                // TODO : 쇼핑리스트로 이동
//                presenter.decideShoppingList();
                break;
            case android.R.id.home:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage(getString(R.string.cancel_create_list))
                        .setPositiveButton(getString(R.string.confirm), (__, ___) -> {
                            presenter.removeCart();
                            finish();
                        })
                        .setCancelable(true)
                        .show();
                break;
            default:
                break;
        }

        return true;
    }

    public static void startActivity(Context context, GoodsListHeader header) {
        Intent intent = new Intent(context, CreateListActivity.class);
        intent.putExtra(EXTRA_GOODS_LIST_HDAER, header);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, GoodsListHeader header, ArrayList<Goods> goodslist) {
        Intent intent = new Intent(context, CreateListActivity.class);
        intent.putExtra(EXTRA_GOODS_LIST_HDAER, header);
        intent.putParcelableArrayListExtra(EXTRA_SELECTED_GOODS_LIST, goodslist);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tvBadge != null) {
            presenter.getShoppingListCount();
        }
    }

    public void initView() {
        binding.includeLoading.lavLoading.playAnimation();
        binding.includeLoading.lavLoading.setRepeatCount(LottieDrawable.INFINITE);
        GoodsListHeader header = getIntent().getParcelableExtra(EXTRA_GOODS_LIST_HDAER);
        if (header == null) {
            // 테스트 코드
            header = new GoodsListHeader();
            header.setNation("JP");
            header.setCity("osaka");
            header.setStartDate(Calendar.getInstance().getTime());
            header.setEndDate(Calendar.getInstance().getTime());
            header.setLat(11.1);
            header.setLng(11.2);
        }
        presenter.saveListHeader(header);
        setSupportActionBar(binding.toolbarScreen);
        getSupportActionBar().setDisplayShowHomeEnabled(true); //홈 아이콘을 숨김처리합니다.
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.btn_all_back);
        binding.toolbarTitle.setText(header.getCity());
        GoodsListRecyclerAdapter adapter = new GoodsListRecyclerAdapter();
        binding.rvRecommendList.setLayoutManager(new GridLayoutManager(this, 3));
        adapter.setOnItemDetailListener((v, position) -> {
            presenter.getDetailItemUid(position);
        });
        binding.rvRecommendList.setAdapter(adapter);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.space_line);
        binding.rvRecommendList.addItemDecoration(new ListSpaceItemDecoration(spacingInPixels, 3));
        //TODO API 21 이하 대응 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.rvRecommendList.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (binding.rvRecommendList.canScrollVertically(SCROLL_DIRECTION_UP)) {
                        binding.ablTopControl.setElevation(10);
                    } else {
                        binding.ablTopControl.setElevation(0);
                    }
                }
            });
        }
        presenter.loadListData(adapter, header.getNation(), header.getCity());

    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onDetach();
        if (disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @Override
    public void goNextStep(List<Goods> list) {
        // TODO : 저장된 아이템 해쉬테그, 제목 결정하는 곳으로 넘겨주기
        GoodsListHeader header = getIntent().getParcelableExtra(EXTRA_GOODS_LIST_HDAER);
        if (header == null) {
            // 테스트 코드
            header = new GoodsListHeader();
            header.setNation("JP");
            header.setCity("osaka");
            header.setStartDate(Calendar.getInstance().getTime());
            header.setEndDate(Calendar.getInstance().getTime());
            header.setLat(11.1);
            header.setLng(11.2);
        }
        DLogUtil.d(header.toString());
        DLogUtil.d(list.toString());
        CreateListInfo.startActivity(this, header);
    }


    @Override
    public void emptyCheckGoods() {
        showToast(getString(R.string.empty_goods));
    }

    @Override
    public void showDetailItem(Goods item) {
        GoodsDetailActivity.startActivity(this, item);
    }

    @Override
    public void finishLoad(int size) {
        binding.includeLoading.lavLoading.cancelAnimation();
        binding.includeLoading.lavLoading.setVisibility(View.GONE);
        if (size == Constant.LOADING_NONE_ITEM) {
            showLongToast(String.format(getString(R.string.none_item), getString(R.string.toast_goods)));
        } else if (size == Constant.FAIL_LOAD) {
            showLongToast(getString(R.string.fail_load));
        }
    }

    @Override
    public void setBadge(String count) {
        if (count == null || Integer.valueOf(count) == 0) {
            tvBadge.setVisibility(View.GONE);
            return;
        }
        if (Integer.valueOf(count) >= 99) {
            tvBadge.setText("99+");
            tvBadge.setVisibility(View.VISIBLE);
        }
        tvBadge.setText(count);
        tvBadge.setVisibility(View.VISIBLE);
    }

}
