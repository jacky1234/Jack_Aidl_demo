package com.jack.jack_aidl_demo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private StringBuffer sb = new StringBuffer();
    private static int bookId = 5;
    private Executor mExecutor;
    private IBookManager mRemoteBookManager;
    private MyHandler myHandler = new MyHandler(this);

    private static final int MSG_REFRESH_INFO = 0x110;

    private TextView tv_info;

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

    private void initViews() {
        tv_info = (TextView) findViewById(R.id.tv_info);
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
    /*********************************事件处理*end**********************/

    private ServiceConnection mConnection = new ServiceConnection() {
        /**
         * 注意：
         * onServiceConnected 和 onServiceDisconnected运行在ui线程中，不应该在里面调用耗时的操作。
         * @param name
         * @param service
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放连接
        unbindService(mConnection);
    }

    private void refreshTextInfo() {
        tv_info.setText(sb.toString());
    }

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mOuter;

        public MyHandler(MainActivity activity) {
            mOuter = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MainActivity outer = mOuter.get();
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
