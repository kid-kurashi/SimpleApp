package com.coal.projects.chat.presentation.chats;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.coal.projects.chat.ImageLoader;
import com.coal.projects.chat.R;
import com.coal.projects.chat.creation.ChatInstance;
import com.coal.projects.chat.data.FirebaseRepository;
import com.coal.projects.chat.firestore_constants.Chats;
import com.makeramen.roundedimageview.RoundedImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    static final String IS_CHECKED = "IS_CHECKED";
    private final ImageLoader imageLoader;
    private List<Map<String, Object>> items = new ArrayList<>();
    private OnClickCallback onClickCallback;
    private SelectionListener selectionCallback;
    private boolean selectionMode;
    private BehaviorSubject<Map<String, Object>> selectionBehavior = BehaviorSubject.create();
    private Disposable selectionDisposable;

    ChatsAdapter(ImageLoader imageLoader, SelectionListener selectionCallback) {
        this.selectionCallback = selectionCallback;
        this.imageLoader = imageLoader;
    }

    @NonNull
    @Override
    public ChatsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chats_item_layout, viewGroup, false);
        selectionDisposable = selectionBehavior
                .subscribe(item -> detectedSelectMode());
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(items.get(i));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    void setOnClickCallback(OnClickCallback onClickCallback) {
        this.onClickCallback = onClickCallback;
    }

    void dropSelection() {
        for (Map<String, Object> item : items) {
            item.put(IS_CHECKED, false);
        }
        notifyDataSetChanged();
    }

    public interface OnClickCallback {
        void onClick(CreatedChat createdChat);
    }

    void setItems(List<Map<String, Object>> items) {
        this.items = items;
    }

    List<Map<String, Object>> getItems() {
        return items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView chatTitle;
        private final TextView lastMessage;
        private final RoundedImageView chatLogo;
        private final ConstraintLayout root;
        private final ImageView removedChat;

        private FirebaseRepository firebaseRepository = ChatInstance.getInstance().getFirebaseRepository();

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.chats_chat_root);
            chatTitle = itemView.findViewById(R.id.chats_chat_tag);
            lastMessage = itemView.findViewById(R.id.chats_last_message);
            chatLogo = itemView.findViewById(R.id.chats_chat_image);
            removedChat = itemView.findViewById(R.id.chats_removed_chat);
        }

        void bind(Map<String, Object> chat) {
            lastMessage.setText("");
            List<String> names = (List<String>) chat.get(Chats.FIELD_DISPLAY_NAMES);
            List<String> members = (List<String>) chat.get(Chats.FIELD_MEMBERS);

            if (members != null && names != null) {
                chatLogo.setBackground(null);
                chatLogo.setImageBitmap(null);
                loadAvatars(firebaseRepository.clear(members));
                chatTitle.setText(getTitle(names));
                setLastMessage(chat);

                int normalChat = super.itemView.getContext().getResources().getColor(R.color.md_white_1000);
                removedChat.setVisibility(View.GONE);
                root.setBackgroundColor(normalChat);

                root.setOnLongClickListener(v -> {
                    toggleSelection(chat);
                    return true;
                });
                root.setOnClickListener(v -> {
                    if (selectionMode) {
                        toggleSelection(chat);
                    } else
                        onClickCallback.onClick(
                                new CreatedChat(chatTitle.getText().toString(),
                                        (String) chat.get(Chats.FIELD_CHAT_ID)));
                });
            } else {
                root.setOnClickListener(v -> Toast.makeText(root.getContext(), "Chat is deleted", Toast.LENGTH_LONG).show());
                int removedColor = super.itemView.getContext().getResources().getColor(R.color.md_red_400);
                root.setBackgroundColor(removedColor);
                removedChat.setVisibility(View.VISIBLE);
            }
        }

        private void toggleSelection(Map<String, Object> chat) {
            if (chat.get(IS_CHECKED) != null) {
                chat.put(IS_CHECKED, !(Boolean) chat.get(IS_CHECKED));
            } else {
                chat.put(IS_CHECKED, true);
            }

            if ((Boolean) chat.get(IS_CHECKED)) {
                int selectedColor = super.itemView.getContext().getResources().getColor(R.color.md_blue_grey_50);
                root.setBackgroundColor(selectedColor);
            } else {
                int defaultColor = super.itemView.getContext().getResources().getColor(R.color.md_white_1000);
                root.setBackgroundColor(defaultColor);
            }
            selectionBehavior.onNext(chat);
        }

        private void loadAvatars(List<String> logins) {
            for (String login : logins) {
                firebaseRepository.getProfileUrl(login)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(url -> {
                            imageLoader.loadImageInto(url, chatLogo);
                            return true;
                        })
                        .subscribe();
            }
        }

        private void setLastMessage(Map<String, Object> chat) {
            List<Map<String, Object>> messages = (List<Map<String, Object>>) chat.get(Chats.FIELD_MESSAGES);
            if (messages != null && messages.size() > 0) {
                lastMessage.setText(((String) messages.get(messages.size() - 1).get(Chats.FIELD_MESSAGE_TEXT)));
            }
        }

        @NotNull
        private String getTitle(List<String> names) {
            StringBuilder builder = new StringBuilder();
            for (String name : names) {
                if (!name.isEmpty())
                    builder.append(name).append(" ");
            }
            return builder.toString();
        }

        @Override
        protected void finalize() throws Throwable {
            if (!selectionDisposable.isDisposed())
                selectionDisposable.dispose();
            super.finalize();
        }
    }

    boolean detectedSelectMode() {
        selectionMode = false;
        for (Map<String, Object> chat : items) {
            if (chat.get(IS_CHECKED) != null && (Boolean) chat.get(IS_CHECKED)) {
                selectionMode = true;
            }
        }
        selectionCallback.onSelectionChanged(selectionMode);
        return selectionMode;
    }

    public interface SelectionListener {
        void onSelectionChanged(boolean selectionMode);
    }
}
