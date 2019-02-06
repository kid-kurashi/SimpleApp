package com.coal.projects.chat.presentation.chat;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Intent;
import android.os.Bundle;
import com.coal.projects.chat.data.FirebaseRepository;
import com.coal.projects.chat.domain.PushManager;
import com.coal.projects.chat.firestore_constants.Chats;
import com.coal.projects.chat.presentation.base.BaseViewModel;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatViewModel extends BaseViewModel {

    private final PushManager pushManager;
    private Disposable getMessagesDisposable;

    public MutableLiveData<ArrayList<HashMap<String, String>>> messages = new MutableLiveData<>();
    public MutableLiveData<Boolean> isProgress = new MutableLiveData<>();
    private String chatId;

    public ChatViewModel(FirebaseRepository firebaseRepository, PushManager pushManager) {
        super(firebaseRepository);
        this.pushManager = pushManager;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void loadMessages() {
        if (allowLifecycleOperations) {
            isProgress.postValue(true);
            getMessagesDisposable = firebaseRepository
                    .observeChatMessages(chatId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(this::onReceivedMessages, this::onErrorReceived);
        }
    }

    private void onReceivedMessages(ArrayList<HashMap<String, String>> receivedMessages) {
        isProgress.postValue(false);
        messages.postValue(receivedMessages);
    }

    public void setChatId(Intent intent) {
        this.chatId = intent.getStringExtra(Chats.FIELD_CHAT_ID);
    }

    public String getOwner() {
        return firebaseRepository.getLogin();
    }

    public void sendMessage(String message) {
        if (message != null && !message.isEmpty()) {
            pushManager.sendPush(message, chatId);
            firebaseRepository.sendMessage(message, chatId);
        }
    }

    @Override
    protected void setSavedInstanceState(Bundle savedInstanceState) {
        super.setSavedInstanceState(savedInstanceState);
    }
}
