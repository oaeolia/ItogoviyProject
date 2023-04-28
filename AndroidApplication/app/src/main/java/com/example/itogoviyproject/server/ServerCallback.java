package com.example.itogoviyproject.server;

public abstract class ServerCallback<T1, T2, T3> {
    public abstract void onDataReady(T1 arg1, T2 arg2, T3 arg3);
}
