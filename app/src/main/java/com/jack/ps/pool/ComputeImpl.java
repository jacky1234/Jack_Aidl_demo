package com.jack.ps.pool;

import android.os.RemoteException;

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
        return a + b;
    }
}
