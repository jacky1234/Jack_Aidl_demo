package com.jack.jack_aidl_demo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BookManagerService extends Service {
    List<Book> mBookList;

    @Override
    public void onCreate() {
        super.onCreate();

        mBookList = new CopyOnWriteArrayList<>();
        mBookList.add(new Book(0,10,"android开发艺术探索"));
        mBookList.add(new Book(1,100,"android群英传"));
        mBookList.add(new Book(2,1000,"android设计模式源码解析"));
    }

    public BookManagerService() {
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Binder mBinder = new IBookManager.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public List<Book> getBookList() throws RemoteException {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return mBookList;
        }

        @Override
        public void addBook(Book b) throws RemoteException {
            try {
                Thread.sleep(500);
                mBookList.add(b);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
}
