package com.coal.projects.chat.presentation.chats;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.coal.projects.chat.R;
import com.coal.projects.chat.firestore_constants.Chats;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private List<Map<String, Object>> items = new ArrayList<>();
    private OnClickCallback onClickCallback;

    @NonNull
    @Override
    public ChatsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chats_item_layout, viewGroup, false);
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

    public interface OnClickCallback {
        void onClick(String chatId);
    }

    void setItems(List<Map<String, Object>> items) {
        this.items = items;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView chatTitle;
        private final TextView lastMessage;
        private final RoundedImageView chatLogo;
        private final ConstraintLayout root;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.chats_chat_root);
            chatTitle = itemView.findViewById(R.id.chats_chat_tag);
            lastMessage = itemView.findViewById(R.id.chats_last_message);
            chatLogo = itemView.findViewById(R.id.chats_chat_image);
        }

        public void bind(Map<String, Object> chat) {
            chatTitle.setText(chat.get(Chats.FIELD_MEMBERS).toString());
            List<Map<String, Object>> messages = (List<Map<String, Object>>) chat.get(Chats.FIELD_MESSAGES);
            if(messages.size() > 0) {
                lastMessage.setText(((String) messages.get(messages.size() - 1).get(Chats.FIELD_MESSAGE_TEXT)));
            }

            root.setOnClickListener(v -> onClickCallback.onClick((String) chat.get(Chats.FIELD_CHAT_ID)));
        }
    }
}
