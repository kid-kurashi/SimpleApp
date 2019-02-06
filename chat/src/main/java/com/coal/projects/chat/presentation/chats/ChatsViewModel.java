package com.coal.projects.chat.presentation.chats;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.coal.projects.chat.data.FirebaseRepository;
import com.coal.projects.chat.firestore_constants.Chats;
import com.coal.projects.chat.presentation.base.BaseViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.coal.projects.chat.presentation.chats.ChatsAdapter.IS_CHECKED;
import static com.coal.projects.chat.presentation.contacts.ContactsActivity.ARRAY_IDENTIFIER_VALUES;

public class ChatsViewModel extends BaseViewModel {

    public MutableLiveData<Boolean> isProgress = new MutableLiveData<>();
    public MutableLiveData<List<Map<String, Object>>> chats = new MutableLiveData<>();
    public MutableLiveData<CreatedChat> chatCreated = new MutableLiveData<>();
    public MutableLiveData<Boolean> isConnectingProgress = new MutableLiveData<>();
    public MutableLiveData<String> conditionTitle = new MutableLiveData<>();

    private Disposable observeChatsDisposable;
    private Disposable createNewChatDisposable;
    private Disposable connectToFirebaseDisposable;
    private RemoveCallback removeCallback;

    public ChatsViewModel(FirebaseRepository firebaseRepository) {
        super(firebaseRepository);
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void connectToFirestore() {
        if (allowLifecycleOperations) {
            conditionTitle.postValue("Connecting...");
            isConnectingProgress.postValue(true);
            connectToFirebaseDisposable = firebaseRepository
                    .connectToFirestore()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onFirestoreConnected, this::onErrorReceived);
        }
    }

    private void onFirestoreConnected(Boolean aBoolean) {
        conditionTitle.postValue("Connected");
        isConnectingProgress.postValue(!aBoolean);
        new Handler().postDelayed(this::observeChats, 300);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void observeChats() {
        if (allowLifecycleOperations && isConnectingProgress.getValue() != null && !isConnectingProgress.getValue()) {
            conditionTitle.postValue("Loading chats...");
            isProgress.postValue(true);
            observeChatsDisposable = firebaseRepository
                    .observeUserChats()
                    .observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .subscribe(this::onChatsLoaded, this::onErrorReceived);
        }
    }

    private void onChatsLoaded(List<Map<String, Object>> maps) {
        if (maps != null && !maps.isEmpty()) {
            conditionTitle.postValue("Chats");
            chats.postValue(maps);
            isProgress.postValue(false);
        } else {
            conditionTitle.postValue("Chats");
            chats.postValue(new ArrayList<>());
            isProgress.postValue(false);
        }
    }

    @Override
    protected void onErrorReceived(Throwable throwable) {
        conditionTitle.postValue("Loading error");
        isProgress.postValue(false);
        isConnectingProgress.postValue(false);
        super.onErrorReceived(throwable);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void dispose() {
        if (connectToFirebaseDisposable != null && !connectToFirebaseDisposable.isDisposed()) {
            connectToFirebaseDisposable.dispose();
        }
        if (observeChatsDisposable != null && !observeChatsDisposable.isDisposed()) {
            observeChatsDisposable.dispose();
        }
        if (createNewChatDisposable != null && !createNewChatDisposable.isDisposed()) {
            createNewChatDisposable.dispose();
        }
    }


    void createNewChat(List<String> chatMembers) {
        createNewChatDisposable = firebaseRepository
                .createNewChat(chatMembers)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::onNewChatIdReceived, this::onErrorReceived);
    }

    private void onNewChatIdReceived(CreatedChat createdChat) {
        chatCreated.postValue(createdChat);
    }

    ArrayList<String> transformResult(Intent data) {
        return data.getStringArrayListExtra(ARRAY_IDENTIFIER_VALUES);
    }

    @Override
    protected void setSavedInstanceState(Bundle savedInstanceState) {
        super.setSavedInstanceState(savedInstanceState);
    }

    void removeSelected(List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            if (item.get(IS_CHECKED) != null && (Boolean) item.get(IS_CHECKED))
                firebaseRepository.removeChat((String) item.get(Chats.FIELD_CHAT_ID))
                        .addOnSuccessListener(snap -> {
                            observeChats();
                            removeCallback.onRemove();
                        });
        }
    }

    void setRemoveCallback(RemoveCallback removeCallback) {
        this.removeCallback = removeCallback;
    }
}
