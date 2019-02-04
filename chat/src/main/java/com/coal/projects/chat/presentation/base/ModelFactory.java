package com.coal.projects.chat.presentation.base;


import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import com.coal.projects.chat.ChatInstance;
import com.coal.projects.chat.presentation.chats.ChatsViewModel;
import com.coal.projects.chat.presentation.chats.chat.ChatViewModel;
import com.coal.projects.chat.presentation.contacts.ContactsViewModel;

public class ModelFactory extends ViewModelProvider.NewInstanceFactory {


    public ModelFactory() {
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass == ContactsViewModel.class) {
            return (T) new ContactsViewModel(ChatInstance.firebaseRepository, ChatInstance.sharedPreferecesManager);
        }
        if (modelClass == ChatsViewModel.class) {
            return (T) new ChatsViewModel(ChatInstance.firebaseRepository);
        }
        if (modelClass == ChatViewModel.class) {
            return (T) new ChatViewModel(ChatInstance.firebaseRepository);
        }
        return null;
    }
}
