package com.jack.ps.pool;

import android.os.RemoteException;

import com.jack.ps.support.util.Util;

/**
 * Created by Jacky on 2020/7/8
 */
public class ComputeImpl extends ICompute.Stub {
    @Override
    public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

    }

    @Override
    public int add(int a, int b) throws RemoteException {
        // mock cost method
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final int i = a + b;
        Util.i("ComputeImpl", "add in server,%d + %d = %d", a, b, i);
        return i;
    }
}
