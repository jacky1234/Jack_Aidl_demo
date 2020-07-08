package com.jack.ps.aidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jack.ps.R;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BookTestActivity extends AppCompatActivity {
    private static final String TAG = "BookTestActivity";

    private StringBuffer sb = new StringBuffer();
    private static int bookId = 5;
    private Executor mExecutor;
    private IBookManager mRemoteBookManager;
    private MyHandler myHandler = new MyHandler(this);

    private static final int MSG_REFRESH_INFO = 0x110;

    private TextView tv_info;
    private ScrollView scrollView;

    private boolean shouldAutoScroll = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        mExecutor = Executors.newCachedThreadPool();

        /**
         * 绑定 Service
         */
        Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            myHandler.post(scrollRunnable);

            scrollView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    shouldAutoScroll = false;
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        myHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                shouldAutoScroll = true;
                                myHandler.post(scrollRunnable);
                            }
                        }, 2000);
                    }
                    return false;
                }
            });
        } else {
            shouldAutoScroll = false;
        }
    }

    private Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            //自动滑动
            int off = scrollView.getMeasuredHeight() - (scrollView.getChildAt(0)).getMeasuredHeight();
            int scrollY = scrollView.getScrollY();

            int step;
            if (off * (-1) > scrollY) {
                if (off * (-1) - scrollY > 30) {
                    step = 30;
                } else {
                    step = off * (-1) - scrollY;
                }

                scrollView.scrollBy(0, step);
            }

            if (shouldAutoScroll) {
                myHandler.postDelayed(this, 30);
            }
        }
    };

    private void initViews() {
        tv_info = (TextView) findViewById(R.id.tv_info);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
    }

    /**
     * 事件处理*begin
     */
    public void addBook(View view) {
        Book newBook = new Book(
            bookId, new Random().nextInt(100),
            "Book" + bookId
        );
        mExecutor.execute(new AddBookRunnable(newBook));

        bookId++;
    }

    public void queryBooks(View view) {
        mExecutor.execute(new GetBookListRunnable());
    }

    /*********************************
     * 事件处理*end
     **********************/

    private ServiceConnection mConnection = new ServiceConnection() {
        /**
         * 注意：
         * onServiceConnected 和 onServiceDisconnected运行在ui线程中，不应该在里面调用耗时的操作。
         * @param name      组建名字
         * @param service   服务器返回的Binder对象
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRemoteBookManager = IBookManager.Stub.asInterface(service);
            try {
                /**
                 * 为Binder设置死亡代理
                 */
                service.linkToDeath(mDeathRecipient, 0);

                /**
                 * action 1,获取书本信息列表
                 */
                mExecutor.execute(new GetBookListRunnable());

                /**
                 * action2,耗时操作，加入书籍
                 */
                mExecutor.execute(new AddBookRunnable(new Book(4, 50, "Android进阶")));
                mRemoteBookManager.registerListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 设置死亡代理
     * DeathRecipient 是个接口，实现代理过程
     */
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "binder died. tname:" + Thread.currentThread().getName());
            if (mRemoteBookManager == null)
                return;
            mRemoteBookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mRemoteBookManager = null;
            // TODO:这里重新绑定远程Service
        }
    };

    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            Log.d(TAG, String.format("new book arrive:%s", newBook.toString()));
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放连接
        if (mRemoteBookManager != null && mRemoteBookManager.asBinder().isBinderAlive()) {
            try {
                mRemoteBookManager.unregisterListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        unbindService(mConnection);
        shouldAutoScroll = false;
    }

    private void refreshTextInfo() {
        tv_info.setText(sb.toString());
    }

    private static class MyHandler extends Handler {
        private final WeakReference<BookTestActivity> mOuter;

        private MyHandler(BookTestActivity activity) {
            mOuter = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final BookTestActivity outer = mOuter.get();
            /**
             * Handler 是隶属于 Activity的
             * 如果Activity没有结束，继续执行页面的更新操作
             */
            if (outer != null) {
                switch (msg.what) {
                    case MSG_REFRESH_INFO:
                        outer.refreshTextInfo();
                        break;
                }
            }

            super.handleMessage(msg);
        }
    }

    private class GetBookListRunnable implements Runnable {

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            List<Book> bookList;
            try {
                if (mRemoteBookManager != null) {
                    String info;
                    info = "client try to query books";
                    sb.append("\n").append(info);
                    myHandler.sendEmptyMessage(MSG_REFRESH_INFO);

                    bookList = mRemoteBookManager.getBookList();
                    info = "server:query book list consumer time:" + (System.currentTimeMillis() - start) + "ms, list type:"
                        + bookList.toString();
                    Log.i(TAG, info);
                    sb.append("\n\n").append(info);
                    myHandler.sendEmptyMessage(MSG_REFRESH_INFO);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private class AddBookRunnable implements Runnable {
        private Book book;

        public AddBookRunnable(Book book) {
            this.book = book;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            if (mRemoteBookManager != null) {
                if (book != null) {
                    try {
                        String info;
                        info = "client try to add book:" + book;
                        sb.append("\n").append(info);
                        myHandler.sendEmptyMessage(MSG_REFRESH_INFO);


                        mRemoteBookManager.addBook(book);
                        info = "server:add book consumer time:" + (System.currentTimeMillis() - start) + "ms, bookInfo: " + book;
                        Log.i(TAG, info);
                        sb.append("\n\n").append(info);
                        myHandler.sendEmptyMessage(MSG_REFRESH_INFO);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
