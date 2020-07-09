package com.jack.ps.pool;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jack.ps.support.util.Util;

/**
 * Created by Jacky on 2020/7/8
 */
public class BinderPoolTestActivity extends AppCompatActivity {
    private static final String TAG = "BinderPoolTestActivity";

    private ISecurityCenter mSecurityCenter;
    private ICompute mCompute;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Button button = new Button(this);
        button.setText("Test");
        setContentView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doTest();
            }
        });
    }

    private void doTest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doWork();
            }
        }).start();
    }

    private void doWork() {
        BinderPool binderPool = BinderPool.getInstance(this);
        IBinder securityBinder = binderPool
            .queryBinder(BinderPool.BINDER_SECURITY_CENTER);

        mSecurityCenter = SecurityCenterImpl.asInterface(securityBinder);
        Log.d(TAG, "visit ISecurityCenter");
        String msg = "helloWorld-安卓";
        Util.i(TAG, "content:%s", msg);
        try {
            String password = mSecurityCenter.encrypt(msg);
            Util.i(TAG, "encrypt:" + password);
            Util.i(TAG, "decrypt:" + mSecurityCenter.decrypt(password));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "visit ICompute");
        IBinder computeBinder = binderPool
            .queryBinder(BinderPool.BINDER_COMPUTE);

        mCompute = ComputeImpl.asInterface(computeBinder);
        try {
            Util.i(TAG, "3+5=" + mCompute.add(3, 5));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
