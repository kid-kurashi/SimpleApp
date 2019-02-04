package com.coal.projects.chat.presentation.chats;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import com.coal.projects.chat.data.FirebaseRepository;
import com.coal.projects.chat.presentation.base.BaseViewModel;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.List;
import java.util.Map;

public class ChatsViewModel extends BaseViewModel {

    public MutableLiveData<Boolean> isProgress = new MutableLiveData<>();
    public MutableLiveData<List<Map<String, Object>>> chats = new MutableLiveData<>();
    public MutableLiveData<String> chatCreated = new MutableLiveData<>();

    private Disposable loadChatsDisposable;
    private Disposable createNewChatDisposable;

    public ChatsViewModel(FirebaseRepository firebaseRepository) {
        super(firebaseRepository);
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void loadChats() {
        isProgress.postValue(true);
        loadChatsDisposable = firebaseRepository
                .getUserChats()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(this::onChatsLoaded, this::onErrorReceived);
    }

    private void onChatsLoaded(List<Map<String, Object>> maps) {
        chats.postValue(maps);
        isProgress.postValue(false);
    }

    @Override
    protected void onErrorReceived(Throwable throwable) {
        super.onErrorReceived(throwable);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void disposeChats() {
        if (loadChatsDisposable != null && !loadChatsDisposable.isDisposed()) {
            loadChatsDisposable.dispose();
        }
        if (createNewChatDisposable != null && !createNewChatDisposable.isDisposed()) {
            createNewChatDisposable.dispose();
        }
    }

    public void createNewChat(Map<String, String> chatMembers) {
        createNewChatDisposable = firebaseRepository
                .createNewChat(chatMembers)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::onNewChatIdReceived, this::onErrorReceived);
    }

    private void onNewChatIdReceived(String id) {
        chatCreated.postValue(id);
    }

}
