package com.coal.projects.chat.presentation.contacts;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.coal.projects.chat.data.FirebaseRepository;
import com.coal.projects.chat.data.SharedPreferecesManager;
import com.coal.projects.chat.data.pojo.User;
import com.coal.projects.chat.presentation.base.BaseViewModel;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.coal.projects.chat.presentation.contacts.ContactsActivity.ARRAY_DISPLAY_NAMES;
import static com.coal.projects.chat.presentation.contacts.ContactsActivity.ARRAY_IDENTIFIER_VALUES;

public class ContactsViewModel extends BaseViewModel {

    private final SharedPreferecesManager sharedPreferecesManager;
    public MutableLiveData<Boolean> isProgress = new MutableLiveData<>();
    public MutableLiveData<List<SelectableUser>> listContacts = new MutableLiveData<>();
    public MutableLiveData<Boolean> dismissDialog = new MutableLiveData<>();

    public MutableLiveData<Boolean> selection = new MutableLiveData<>();

    private Disposable getContactsDisposable;
    private Disposable addContactDisposable;
    private boolean selectionMode;
    private ArrayList<SelectableUser> selectableUsers;

    public ContactsViewModel(FirebaseRepository repository, SharedPreferecesManager sharedPreferecesManager) {
        super(repository);
        this.sharedPreferecesManager = sharedPreferecesManager;
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void connectToFirestore() {
        if(allowLifecycleOperations) {
            dismissDialog.postValue(false);
            isProgress.postValue(true);
        }
    }

    @Override
    protected void onErrorReceived(Throwable throwable) {
        super.onErrorReceived(throwable);

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void getContacts() {
        if(allowLifecycleOperations) {
            isProgress.postValue(true);
            getContactsDisposable = firebaseRepository
                    .observeUserContacts()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(this::onContactsReceived, this::onErrorReceived);
        }
    }

    private void onContactsReceived(List<SelectableUser> users) {
        Log.e("users", users.toString());
        listContacts.postValue(users);
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

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void destroySubscribers() {
        if (getContactsDisposable != null && !getContactsDisposable.isDisposed()) {
            getContactsDisposable.dispose();
        }
        if (addContactDisposable != null && !addContactDisposable.isDisposed()) {
            addContactDisposable.dispose();
        }
    }

    public void writeContacts(List<SelectableUser> contacts) {
        List<String> temp = new ArrayList<>();
        for (SelectableUser user : contacts) {
            temp.add(user.getLogin());
        }
        Set<String> contactsSet = new HashSet<>(temp);
        sharedPreferecesManager.writeContacts(contactsSet);
    }

    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public void toggleButton(boolean haveSelection) {
        selection.postValue(haveSelection);
    }

    public void setSelectedUsers(ArrayList<SelectableUser> selectableUsers) {
        this.selectableUsers = selectableUsers;
    }

    public ArrayList<SelectableUser> getSelectableUsers() {
        return selectableUsers;
    }

    public boolean makeResult(Intent intent) {
        ArrayList<String> usernames = new ArrayList<>();
        ArrayList<String> userLogins = new ArrayList<>();
        if(selectableUsers != null && !selectableUsers.isEmpty()) {
            for (SelectableUser user : selectableUsers) {
                usernames.add(user.getDisplayName());
                userLogins.add(user.getLogin());
            }
            intent.putExtra(ARRAY_DISPLAY_NAMES, usernames);
            intent.putExtra(ARRAY_IDENTIFIER_VALUES, userLogins);
            return true;
        }else{
            return false;
        }
    }

    @Override
    protected void setSavedInstanceState(Bundle savedInstanceState) {
        super.setSavedInstanceState(savedInstanceState);
    }
}
