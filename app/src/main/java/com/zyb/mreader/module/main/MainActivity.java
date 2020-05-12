package com.zyb.mreader.module.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.gyf.barlibrary.ImmersionBar;
import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialog.util.BaseDialog;
import com.zyb.base.base.activity.MVPActivity;
import com.zyb.base.di.component.AppComponent;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.router.RouterConstants;
import com.zyb.base.router.RouterUtils;
import com.zyb.base.utils.CommonUtils;
import com.zyb.base.utils.LogUtil;
import com.zyb.base.utils.QMUIViewHelper;
import com.zyb.base.utils.constant.ApiConstants;
import com.zyb.base.utils.constant.Constants;
import com.zyb.base.widget.WebActivity;
import com.zyb.base.widget.decoration.GridItemSpaceDecoration;
import com.zyb.common.db.DBFactory;
import com.zyb.common.db.bean.Book;
import com.zyb.mreader.AboutActivity;
import com.zyb.mreader.R;
import com.zyb.mreader.di.component.DaggerActivityComponent;
import com.zyb.mreader.di.module.ActivityModule;
import com.zyb.mreader.di.module.ApiModule;
import com.zyb.mreader.module.addBook.AddBookActivity;
import com.zyb.mreader.module.webdav.WebdavActivity;
import com.zyb.mreader.widget.ContentScaleAnimation;
import com.zyb.mreader.widget.Rotate3DAnimation;
import com.zyb.reader.Config;
import com.zyb.reader.ReadActivity;
import com.zyb.reader.util.FileUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
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
    @BindView(R.id.layoutBooksEmpty)
    LinearLayout layoutBooksEmpty;

    Vibrator vibrator;

    private BooksAdapter booksAdapter;
    List<Book> books = new ArrayList<>();

    private BaseQuickAdapter.OnItemClickListener onItemClickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            if (isAnimating) return;
            if (booksAdapter.isCanSelect()) {
                setBookSelected(position, !books.get(position).isSelected());
                return;
            }
            bookPosition = position;

            String filePath = books.get(bookPosition).getPath();
            File file = new File(filePath);
            if (!file.exists()) {
                onFileNotExist();
                return;
            }

            onBookItemClick(view);
        }
    };

    private void onFileNotExist() {
        showDialog(true, "本地文件已被删除，\n是否从书架移除它？", "取消", "移除",
                new OnDialogButtonClickListener() {
                    @Override
                    public boolean onClick(BaseDialog baseDialog, View v) {
                        mPresenter.removeBook(books.get(bookPosition));
                        refreshBooks();
                        return false;
                    }
                }, null);
    }

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
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        ImmersionBar.setTitleBar(this, layoutActionTop);
        layoutActionTop.setOnTitleBarListener(onTopActionBarListener);

        booksAdapter = new BooksAdapter(books);
        booksAdapter.setOnItemClickListener(onItemClickListener);
        rvBooks.setLayoutManager(new GridLayoutManager(this, 3));
        int space = CommonUtils.dp2px(20);
        rvBooks.addItemDecoration(new GridItemSpaceDecoration(3, space, true, 0));
        rvBooks.setAdapter(booksAdapter);
        mItemHelper.attachToRecyclerView(rvBooks);
    }

    @Override
    protected void initData() {
        refreshBooks();
        mPresenter.preloadBooks();
    }

    private void refreshBooks() {
        mPresenter.getBooks();
    }

    @Override
    public void onLeftClick(View v) {
        drawerLayout.openDrawer(Gravity.START);
    }

    @OnClick(R.id.tvAddBook)
    public void onAddBookClick() {
        toAddBook();
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
        layoutBooksEmpty.setVisibility(this.books.size() > 0 ? View.GONE : View.VISIBLE);
    }

    @OnClick({R.id.addBook, R.id.feedBack, R.id.about, R.id.share, R.id.backup})
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
            case R.id.share:
                mPresenter.drawerAction(MainContract.DRAWER_ACTION.TO_SHARE);
                break;
            case R.id.backup:
                mPresenter.drawerAction(MainContract.DRAWER_ACTION.TO_BACKUP);
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

    @Override
    public void toShare() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mreader_app_name));
        intent.putExtra(Intent.EXTRA_TEXT, "猫豆阅读——简约无广告的本地图书阅读器，快来下载吧！\n" + ApiConstants.APP_HOME_KUAN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, "分享至"));
    }

    @Override
    public void toLogin() {
    }

    @Override
    public void toBackup() {
        startActivity(WebdavActivity.class);
    }

    @Override
    public void onBackPressed() {
        if (isAnimating) return;
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
            return;
        } else if (layoutActionTop.getVisibility() == View.VISIBLE) {
            exitEditMode();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //==============书本打开动画Start================
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
    // 是否打开书籍 其实是是否已离开当前界面，跳转到阅读界面
    private boolean isBookOpened = false;
    private boolean isAnimating = false;

    private int bookPosition;

    @Override
    protected void onRestart() {
        super.onRestart();
        // 当界面重新进入的时候进行合书的动画
        if (isBookOpened) {
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
        if (Config.getInstance().getDayOrNight()) {
            mContent.setBackgroundColor(Color.BLACK);
            return;
        }
        switch (Config.getInstance().getBookBgType()) {
            case Config.BOOK_BG_DEFAULT:
                //为了性能 不设置图片了
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
            long time = System.currentTimeMillis();
            book.setLastReadTime(time);
        }

        Intent intent = new Intent(MainActivity.this, ReadActivity.class);
        intent.putExtra(ReadActivity.EXTRA_BOOK, book);
        startActivity(intent);
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //合书时返回桌面 直接把动画取消（调用onAnimationEnd)
        if (isBookOpened && isAnimating) {
            if (scaleAnimation != null) scaleAnimation.cancel();
            if (threeDAnimation != null) threeDAnimation.cancel();
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {
        isAnimating = true;
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        LogUtil.e("onAnimationEnd");
        isAnimating = false;
        if (scaleAnimation.hasEnded() && threeDAnimation.hasEnded()) {
            // 两个动画都结束的时候再处理后续操作
            if (!isBookOpened) {
                isBookOpened = true;
                readBook();
            } else {
                isBookOpened = false;
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
    //==============书本打开动画End================

    //==============书架拖拽功能Start================
    @BindView(R.id.layoutActionTop)
    TitleBar layoutActionTop;
    @BindView(R.id.layoutActionBottom)
    ConstraintLayout layoutActionBottom;
    private static final int ANIM_SHOW_DURATION = 400;
    private static final int ANIM_HIDE_DURATION = 300;

    OnTitleBarListener onTopActionBarListener = new OnTitleBarListener() {
        @Override
        public void onLeftClick(View v) {
            exitEditMode();
        }

        @Override
        public void onTitleClick(View v) {
        }

        @Override
        public void onRightClick(View v) {
            layoutActionTop.setRightTitle(layoutActionTop.getRightTitle().equals("全选") ? "取消" : "全选");
            booksAdapter.selectOrUnselectAll();
        }
    };

    ItemTouchHelper mItemHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            LogUtil.e("mItemHelper", "getMovementFlags()");
            //指定可以拖拽的方向
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            //指定可侧滑方向 0为不可侧滑
            final int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        //当拖拽移动的时候回调的方法
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            //书籍分类
//            View itemView = viewHolder.itemView;
//            View targetView = target.itemView;
//            boolean widthIn = (itemView.getX() + itemView.getWidth() / 2f) > (targetView.getX() + targetView.getWidth() * 0.25)
//                    && (itemView.getX() + itemView.getWidth() / 2f) < (targetView.getX() + targetView.getWidth() * 0.75);
//            boolean heightIn = (itemView.getY() + itemView.getHeight() / 2f) > (targetView.getY() + targetView.getHeight() * 0.25)
//                    && (itemView.getY() + itemView.getHeight() / 2f) < (targetView.getY() + targetView.getHeight() * 0.75);
//
//            LogUtil.e("mItemHelper onMove()", "widthIn:" + widthIn + " heightIn:" + heightIn);
//            if (widthIn && heightIn) { //判断两个拖拽的Item是否进入目标item（书籍归类）
//                onItemMerge(viewHolder, target);
//                return true;
//            }


            //得到当拖拽的viewHolder的Position
            int fromPosition = viewHolder.getAdapterPosition();
            //拿到当前拖拽到的item的viewHolder
            int toPosition = target.getAdapterPosition();
//            if (fromPosition != toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(books, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(books, i, i - 1);
                }
            }
            booksAdapter.notifyItemMoved(fromPosition, toPosition);
//            }
            return true;
        }

        /**
         * 长按选中Item的时候开始调用
         */
        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            LogUtil.e("mItemHelper", "onSelectedChanged:" + actionState);
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder.itemView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,
                        R.anim.book_drag_scale_large));
                if (layoutActionBottom.getVisibility() == View.VISIBLE)
                    QMUIViewHelper.slideOut(layoutActionBottom, ANIM_HIDE_DURATION, QMUIViewHelper.QMUIDirection.TOP_TO_BOTTOM);
                if (vibrator != null) vibrator.vibrate(20);
                viewHolder.itemView.findViewById(R.id.btnUnselected).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.btnSelected).setVisibility(View.GONE);
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            Log.e("mItemHelper", "clearView()");


            //这里再判断一下 防止和点击事件同时执行
            if (isAnimating) {
                viewHolder.itemView.clearAnimation();
                return;
            }

            //排序
            mPresenter.sortBook(books.get(viewHolder.getLayoutPosition()), viewHolder.getLayoutPosition());
            //选择当前项并进入编辑模式
            setBookSelected(viewHolder.getLayoutPosition(), true);
            enterEditMode();
            super.clearView(recyclerView, viewHolder);
        }

        //重写拖拽是否可用
        @Override
        public boolean isLongPressDragEnabled() {
            Log.e("hsjkkk", "isLongPressDragEnabled()");
            return true;
        }

        /**
         * 返回true：拖拽会把其他项“挤”到后面
         * 返回false：拖拽其他项不动
         */
        @Override
        public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
            LogUtil.e("mItemHelper", "canDropOver:");
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            LogUtil.e("mItemHelper", "拖拽完成 方向" + direction);
        }


    });

    /**
     * 进入编辑模式
     */
    private void enterEditMode() {
        booksAdapter.setCanSelect(true);
        toggleActionBar(true);
        getStatusBarConfig().statusBarDarkFont(false).init();
    }

    /**
     * 退出编辑模式
     */
    private void exitEditMode() {
        booksAdapter.setCanSelect(false);
        toggleActionBar(false);
        getStatusBarConfig().statusBarDarkFont(true).init();
    }

    public void onItemMerge(RecyclerView.ViewHolder oldViewHolder, RecyclerView.ViewHolder targetViewHolder) {
        LogUtil.e("mItemHelper", "onItemMerge");
        //记录 position
        int oldPosition = oldViewHolder.getLayoutPosition();
        int newPosition = targetViewHolder.getLayoutPosition();
        //移动到新的item上方时，给它设置选中颜色
        rvBooks.getChildAt(newPosition).setBackgroundColor(Color.parseColor("#444444"));
    }

    /**
     * 设置书本选中状态
     */
    private void setBookSelected(int position, boolean isSelected) {
        books.get(position).setSelected(isSelected);
        booksAdapter.notifyItemChanged(position);
        int unselectedCount = 0;
        for (Book book : books) {
            if (!book.isSelected()) unselectedCount++;
        }
        layoutActionTop.setRightTitle(unselectedCount == 0 ? "取消" : "全选");

    }

    /**
     * 切换书本操作菜单栏可视状态
     */
    private void toggleActionBar(boolean isShown) {
        if (isShown) {
            if (layoutActionTop.getVisibility() == View.GONE)
                QMUIViewHelper.slideIn(layoutActionTop, ANIM_SHOW_DURATION, QMUIViewHelper.QMUIDirection.TOP_TO_BOTTOM);
            if (layoutActionBottom.getVisibility() == View.GONE)
                QMUIViewHelper.slideIn(layoutActionBottom, ANIM_SHOW_DURATION, QMUIViewHelper.QMUIDirection.BOTTOM_TO_TOP);
            return;
        }
        if (layoutActionTop.getVisibility() == View.VISIBLE)
            QMUIViewHelper.slideOut(layoutActionTop, ANIM_HIDE_DURATION, QMUIViewHelper.QMUIDirection.BOTTOM_TO_TOP);
        if (layoutActionBottom.getVisibility() == View.VISIBLE)
            QMUIViewHelper.slideOut(layoutActionBottom, ANIM_HIDE_DURATION, QMUIViewHelper.QMUIDirection.TOP_TO_BOTTOM);
    }

    @OnClick(R.id.btnDelete)
    public void btnDelete() {
        List<Book> deleteBooks = new ArrayList<>();
        for (Book book : books) {
            if (book.isSelected()) deleteBooks.add(book);
        }
        if (deleteBooks.size() <= 0) {
            showToast("请选择书本");
            return;
        }
        showDialog(true, "是否移除这些书本？", "取消", "移除",
                new OnDialogButtonClickListener() {
                    @Override
                    public boolean onClick(BaseDialog baseDialog, View v) {
                        mPresenter.removeBooks(deleteBooks);
                        refreshBooks();
                        exitEditMode();
                        return false;
                    }
                }, null);
    }
    @OnClick(R.id.btnShare)
    public void btnShare() {
        List<Book> deleteBooks = new ArrayList<>();
        for (Book book : books) {
            if (book.isSelected()) deleteBooks.add(book);
        }
        if (deleteBooks.size() <= 0) {
            showToast("请选择书本");
            return;
        }
        if (deleteBooks.size() > 1) {
            showToast("只能分享单个文件哦~");
            return;
        }
        File file =new File(deleteBooks.get(0).getPath());
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.addCategory("android.intent.category.DEFAULT");
        Uri pdfUri;
        pdfUri = Uri.fromFile(file);
        intent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        intent.setType("application/txt");
        try {
            pdfUri = FileProvider.getUriForFile(this, Constants.DEFAULT_FILEPROVIDER, file);
            intent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            startActivity(Intent.createChooser(intent, "分享书籍"));
        } catch (Exception e) {
            startActivity(Intent.createChooser(intent, "分享书籍"));
            e.printStackTrace();
        }
    }
    //==============书架拖拽功能End================


}
