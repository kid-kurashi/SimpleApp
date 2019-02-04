package com.coal.projects.chat.presentation.base;


import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.ViewModel;
import com.coal.projects.chat.data.FirebaseRepository;

public class BaseViewModel extends ViewModel implements LifecycleObserver {

    protected FirebaseRepository firebaseRepository;

    public BaseViewModel(FirebaseRepository firebaseRepository) {
        this.firebaseRepository = firebaseRepository;
    }

    protected void onErrorReceived(Throwable throwable) {
        throwable.printStackTrace();
    }
}
