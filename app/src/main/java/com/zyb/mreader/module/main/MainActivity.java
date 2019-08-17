package com.zyb.mreader.module.main;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.zyb.base.base.BaseDialog;
import com.zyb.base.base.activity.MVPActivity;
import com.zyb.base.di.component.AppComponent;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.router.RouterConstants;
import com.zyb.base.router.RouterUtils;
import com.zyb.base.utils.CommonUtils;
import com.zyb.base.utils.LogUtil;
import com.zyb.base.utils.constant.ApiConstants;
import com.zyb.base.widget.WebActivity;
import com.zyb.base.widget.decoration.GridItemSpaceDecoration;
import com.zyb.base.widget.dialog.MenuDialog;
import com.zyb.common.db.DBFactory;
import com.zyb.common.db.bean.Book;
import com.zyb.mreader.AboutActivity;
import com.zyb.mreader.R;
import com.zyb.mreader.di.component.DaggerActivityComponent;
import com.zyb.mreader.di.module.ActivityModule;
import com.zyb.mreader.di.module.ApiModule;
import com.zyb.mreader.module.addBook.AddBookActivity;
import com.zyb.mreader.widget.ContentScaleAnimation;
import com.zyb.mreader.widget.Rotate3DAnimation;
import com.zyb.reader.Config;
import com.zyb.reader.ReadActivity;
import com.zyb.reader.util.FileUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 *
 */
public class MainActivity extends MVPActivity<MainPresenter> implements
        MainContract.View, Animation.AnimationListener {

    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.rv_books)
    RecyclerView rvBooks;
    @BindView(R.id.smartRefresh)
    SmartRefreshLayout smartRefresh;

    private BooksAdapter booksAdapter;
    List<com.zyb.common.db.bean.Book> books = new ArrayList<>();

    private BaseQuickAdapter.OnItemClickListener onItemClickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            if (position < books.size() - 1) {
                if (isAnimating) return;
                bookPosition = position;
                onBookItemClick(view);
            } else {
                toAddBook();
            }
        }
    };

    private BaseQuickAdapter.OnItemLongClickListener onItemLongClickListener = new BaseQuickAdapter.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
            if (position < books.size() - 1) {
                longClickBook = books.get(position);
                showRemoveDialog();
            }
            return true;
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getTitleBarId() {
        return R.id.titleBar;
    }

    @Override
    protected boolean isRegisterEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventReceived(BaseEvent<Object> event) {
        if (event == null) return;
        switch (event.getCode()) {
            case EventConstants.EVENT_MAIN_REFRESH_BOOK_SHELF:
                refreshBooks();
                break;
        }
    }

    @Override
    protected void initView() {
        Config.createConfig(this);

        booksAdapter = new BooksAdapter(books);
        booksAdapter.setOnItemClickListener(onItemClickListener);
        booksAdapter.setOnItemLongClickListener(onItemLongClickListener);
        rvBooks.setLayoutManager(new GridLayoutManager(this, 3));
        int space = CommonUtils.dp2px(20);
        rvBooks.addItemDecoration(new GridItemSpaceDecoration(3, space, true, 0));
        rvBooks.setAdapter(booksAdapter);
    }

    @Override
    protected void initData() {
        refreshBooks();
    }

    private void refreshBooks() {
        mPresenter.getBooks();
    }

    @Override
    public void onLeftClick(View v) {
        drawerLayout.openDrawer(Gravity.START);
    }

    @Override
    protected void setupActivityComponent(AppComponent appComponent) {
        DaggerActivityComponent.builder()
                .appComponent(appComponent)
                .apiModule(new ApiModule())
                .activityModule(new ActivityModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onBooksLoaded(List<com.zyb.common.db.bean.Book> books) {
        this.books.clear();
        this.books.addAll(books);
        booksAdapter.notifyDataSetChanged();

        smartRefresh.finishRefresh();
    }

    @OnClick({R.id.addBook, R.id.feedBack, R.id.about})
    public void addBookClick(View view) {
        drawerLayout.closeDrawers();
        switch (view.getId()) {
            case R.id.addBook:
                mPresenter.drawerAction(MainContract.DRAWER_ACTION.TO_ADD_BOOK);
                break;
            case R.id.feedBack:
                mPresenter.drawerAction(MainContract.DRAWER_ACTION.TO_FEED_BACK);
                break;
            case R.id.about:
                mPresenter.drawerAction(MainContract.DRAWER_ACTION.TO_ABOUT);
                break;
        }
    }

    @Override
    public void toAddBook() {
        startActivity(AddBookActivity.class);
    }

    @Override
    public void toFeedBack() {
        RouterUtils.getInstance().build(RouterConstants.PATH_BASE_ATY_WEB_VIEW)
                .withString(WebActivity.URL_FLAG, ApiConstants.FEED_BACK_URL)
                .navigation();
    }

    @Override
    public void toAbout() {
        startActivity(AboutActivity.class);
    }

    BaseDialog removeDialog;
    Book longClickBook;
    MenuDialog.OnListener removeListener = new MenuDialog.OnListener() {

        @Override
        public void onSelected(Dialog dialog, int position, String text) {
            mPresenter.removeBook(longClickBook);
            refreshBooks();
        }

        @Override
        public void onCancel(Dialog dialog) {

        }
    };

    private void showRemoveDialog() {
        if (removeDialog == null) {
            List<String> strings = new ArrayList<>();
            strings.add("移除");
            removeDialog = new MenuDialog.Builder(this)
                    .setCancel("取消") // 设置 null 表示不显示取消按钮
                    //.setAutoDismiss(false) // 设置点击按钮后不关闭对话框
                    .setList(strings)
                    .setListener(removeListener)
                    .setGravity(Gravity.BOTTOM)
                    .setAnimStyle(BaseDialog.AnimStyle.BOTTOM)
                    .create();

        }
        removeDialog.show();
    }

    private void hideRemoveLawDialog() {
        if (removeDialog != null) removeDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        } else if (!isAnimating) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideRemoveLawDialog();
    }

    /**
     * 书本打开动画
     */
    // 记录View的位置
    private int[] location = new int[2];
    // 内容页
    @BindView(R.id.img_content)
    public ImageView mContent;
    // 封面
    @BindView(R.id.img_first)
    public RelativeLayout mPage;
    @BindView(R.id.book_title)
    public TextView tvBookTitle;
    // 缩放动画
    private ContentScaleAnimation scaleAnimation;
    // 3D旋转动画0
    private Rotate3DAnimation threeDAnimation;
    // 是否打开书籍 其实是是否离开当前界面，跳转到其他的界面
    private boolean isOpenBook = false;
    private boolean isAnimating = false;

    private int bookPosition;

    @Override
    protected void onRestart() {
        super.onRestart();
        // 当界面重新进入的时候进行合书的动画
        if (isOpenBook) {
            initPageColor();
            scaleAnimation.reverse();
            threeDAnimation.reverse();
            mPage.clearAnimation();
            mPage.startAnimation(threeDAnimation);
            mContent.clearAnimation();
            mContent.startAnimation(scaleAnimation);
        }
    }

    private void initPageColor() {
        switch (Config.getInstance().getBookBgType()) {
            case Config.BOOK_BG_DEFAULT:
                //为了性能 不设置图片了
//                mContent.setBackgroundResource(com.zyb.reader.R.drawable.reader_paper);
                mContent.setBackgroundColor(ContextCompat.getColor(this, com.zyb.reader.R.color.reader_read_bg_default));
                break;
            case Config.BOOK_BG_1:
                mContent.setBackgroundColor(ContextCompat.getColor(this, com.zyb.reader.R.color.reader_read_bg_1));
                break;
            case Config.BOOK_BG_2:
                mContent.setBackgroundColor(ContextCompat.getColor(this, com.zyb.reader.R.color.reader_read_bg_2));
                break;
            case Config.BOOK_BG_3:
                mContent.setBackgroundColor(ContextCompat.getColor(this, com.zyb.reader.R.color.reader_read_bg_3));
                break;
            case Config.BOOK_BG_4:
                mContent.setBackgroundColor(ContextCompat.getColor(this, com.zyb.reader.R.color.reader_read_bg_4));
                break;
        }
        if (Config.getInstance().getDayOrNight())
            mContent.setBackgroundColor(Color.BLACK);
    }

    private void readBook() {
        String filePath = books.get(bookPosition).getPath();
        LogUtil.e("onItemClick---" + filePath);
        File file = new File(filePath);
//                File file = new File("/storage/emulated/0/iBook/三体全集.txt");
        Book book = DBFactory.getInstance().getBooksManage().query(filePath);
        if (book == null) {
            book = new Book();
            String bookName = FileUtils.getFileNameNoEx(file.getName());
            book.setTitle(bookName);
            book.setPath(filePath);
            book.setId(filePath);
        }

        Intent intent = new Intent(MainActivity.this, ReadActivity.class);
        intent.putExtra(ReadActivity.EXTRA_BOOK, book);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        isAnimating = true;
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        isAnimating = false;
        if (scaleAnimation.hasEnded() && threeDAnimation.hasEnded()) {
            // 两个动画都结束的时候再处理后续操作
            if (!isOpenBook) {
                isOpenBook = true;
                readBook();
            } else {
                isOpenBook = false;
                mPage.clearAnimation();
                mContent.clearAnimation();
                mPage.setVisibility(View.GONE);
                mContent.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    public void onBookItemClick(View view) {
        mPage.setVisibility(View.VISIBLE);
        mContent.setVisibility(View.VISIBLE);

        // 计算当前的位置坐标
        view.getLocationInWindow(location);
        int width = view.getWidth();
        int height = view.getHeight();
//        location[1] = location[1] + height  > CommonUtils.getOriginScreenHight() ? CommonUtils.getOriginScreenHight() - height : location[1];

        // 两个ImageView设置大小和位置
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mPage.getLayoutParams();
        params.leftMargin = location[0];
        params.topMargin = location[1];
//        mContent.setX(location[0]);
//        mContent.setY(location[1]);
        params.width = width;
        params.height = height;
        mPage.setLayoutParams(params);
        mContent.setLayoutParams(params);

        initPageColor();

        tvBookTitle.setText(books.get(bookPosition).getTitle());

        initAnimation(view);
        LogUtil.e("left:" + mPage.getLeft() + "top:" + mPage.getTop());

        mContent.clearAnimation();
        mPage.clearAnimation();
        mContent.startAnimation(scaleAnimation);
        mPage.startAnimation(threeDAnimation);
    }

    // 初始化动画
    private void initAnimation(View view) {
        float viewWidth = view.getWidth();
        float viewHeight = view.getHeight();

        float screenWidth = CommonUtils.getScreenWidth();
        float screenHeight = CommonUtils.getOriginScreenHight();

        float horScale = screenWidth / viewWidth;
        float verScale = screenHeight / viewHeight;
        float scale = horScale > verScale ? horScale : verScale;

        scaleAnimation = new ContentScaleAnimation(viewWidth, viewHeight,
                location[0], location[1], scale, false);
        scaleAnimation.setInterpolator(new DecelerateInterpolator());  //设置插值器
        scaleAnimation.setDuration(1000);
        scaleAnimation.setFillAfter(true);  //动画停留在最后一帧
        scaleAnimation.setAnimationListener(this);

        threeDAnimation = new Rotate3DAnimation(viewWidth, viewHeight, this, -180, 0
                , location[0], location[1], scale, true);
        threeDAnimation.setDuration(1000);                         //设置动画时长
        threeDAnimation.setFillAfter(true);                        //保持旋转后效果
        threeDAnimation.setInterpolator(new DecelerateInterpolator());
    }

}
