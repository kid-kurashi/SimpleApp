package com.coal.projects.chat.presentation.contacts;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import com.coal.projects.chat.data.FirebaseRepository;
import com.coal.projects.chat.data.SharedPreferecesManager;
import com.coal.projects.chat.presentation.base.BaseViewModel;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactsViewModel extends BaseViewModel {

    private final SharedPreferecesManager sharedPreferecesManager;
    public MutableLiveData<Boolean> isProgress = new MutableLiveData<>();
    public MutableLiveData<List<String>> listContacts = new MutableLiveData<>();
    public MutableLiveData<Boolean> dismissDialog = new MutableLiveData<>();

    private Disposable connectToFirebaseDisposable;
    private Disposable getContactsDisposable;
    private Disposable addContactDisposable;

    public ContactsViewModel(FirebaseRepository repository, SharedPreferecesManager sharedPreferecesManager) {
        super(repository);
        this.sharedPreferecesManager = sharedPreferecesManager;
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void connectToFirestore() {
        dismissDialog.postValue(false);
        isProgress.postValue(true);
        connectToFirebaseDisposable = firebaseRepository
                .connectToFirestore()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::onFirestoreConnected, this::onErrorReceived);
    }

    @Override
    protected void onErrorReceived(Throwable throwable) {
        super.onErrorReceived(throwable);

    }

    private void onFirestoreConnected(Boolean aBoolean) {
        isProgress.postValue(!aBoolean);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void getContacts() {
        isProgress.postValue(true);
        getContactsDisposable = firebaseRepository
                .observeUserContacts()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::onContactsReceived, this::onErrorReceived);
    }

    private void onContactsReceived(List<String> strings) {
        listContacts.postValue(strings);
        isProgress.postValue(false);
    }

    public void addContact(String contact) {
        addContactDisposable = firebaseRepository
                .addContact(contact)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::onContactAdd, this::onErrorReceived);
    }

    private void onContactAdd(Boolean isAdd) {
        dismissDialog.postValue(isAdd);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void destroySubscribers() {
        if (connectToFirebaseDisposable != null && !connectToFirebaseDisposable.isDisposed()) {
            connectToFirebaseDisposable.dispose();
        }
        if (getContactsDisposable != null && !getContactsDisposable.isDisposed()) {
            getContactsDisposable.dispose();
        }
        if (addContactDisposable != null && !addContactDisposable.isDisposed()) {
            addContactDisposable.dispose();
        }
    }

    public void writeContacts(List<String> contacts) {
        Set<String> contactsSet = new HashSet<>(contacts);
        sharedPreferecesManager.writeContacts(contactsSet);
    }
}
