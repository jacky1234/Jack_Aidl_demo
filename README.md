# This is an android aidl demo
* 具体原理请戳我的简书博客: [Binder 和 AIDL](http://www.jianshu.com/p/91f690c7656e)
![gif](https://github.com/jacky1234/Jack_Aidl_demo/blob/master/srcfolder/demo.gif)


## 分析BinderPool
分析 ` public void onServiceConnected(ComponentName name, IBinder service)` service 实例
    1. 当 BinderPoolService 运行在单独进程。
        service 实例为 BinderProxy
    2. 当 BinderPoolService 运行在主进程。
        service 实例为 com.jack.ps.pool.BinderPool$BinderPoolImpl


# 参考
- [写给 Android 应用工程师的 Binder 原理剖析](https://zhuanlan.zhihu.com/p/35519585)