package com.coal.projects.chat.presentation.base;


import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import com.coal.projects.chat.creation.ChatInstance;
import com.coal.projects.chat.data.FirebaseRepository;
import com.coal.projects.chat.data.SharedPreferecesManager;
import com.coal.projects.chat.domain.PushManager;
import com.coal.projects.chat.presentation.chat.ChatViewModel;
import com.coal.projects.chat.presentation.chats.ChatsViewModel;
import com.coal.projects.chat.presentation.contacts.ContactsViewModel;
import org.jetbrains.annotations.NotNull;

public class ModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final FirebaseRepository firebaseRepository;
    private final PushManager pushManager;
    private final SharedPreferecesManager sharedPreferecesManager;

    public ModelFactory() {
        firebaseRepository = ChatInstance.getInstance().getFirebaseRepository();
        pushManager = ChatInstance.getInstance().getPushManager();
        sharedPreferecesManager = ChatInstance.getInstance().getSharedPreferecesManager();
    }

    @SuppressWarnings("unchecked")
    @Override
    @NotNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass == ContactsViewModel.class) {
            return (T) new ContactsViewModel(firebaseRepository, sharedPreferecesManager);
        }
        if (modelClass == ChatsViewModel.class) {
            return (T) new ChatsViewModel(firebaseRepository);
        }
        if (modelClass == ChatViewModel.class) {
            return (T) new ChatViewModel(firebaseRepository, pushManager);
        }
        return (T) new BaseViewModel(firebaseRepository);
    }
}
