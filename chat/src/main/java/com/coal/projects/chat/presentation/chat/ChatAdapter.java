package com.coal.projects.chat.presentation.chat;


import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.coal.projects.chat.R;
import com.coal.projects.chat.firestore_constants.Chats;
import com.coal.projects.chat.firestore_constants.Users;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private ArrayList<HashMap<String, String>> items = new ArrayList<>();
    private ChatAdapter.OnClickCallback onClickCallback;
    private String owner;


    public ChatAdapter(String owner) {
        this.owner = owner;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_item, viewGroup, false);
        return new ChatAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder viewHolder, int i) {
        viewHolder.bind(items.get(i));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnClickCallback {
        void onClick(String chatId);
    }

    void setItems(ArrayList<HashMap<String, String>> items) {
        this.items = items;
    }

    public ArrayList<HashMap<String, String>> getItems() {
        return items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final CardView cardView;
        private final TextView messageText;
        private final TextView messageTime;
        private final ConstraintLayout root;
        private final TextView displayName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.chat_display_name);
            cardView = itemView.findViewById(R.id.chat_card_view);
            messageText = itemView.findViewById(R.id.chat_message_text);
            messageTime = itemView.findViewById(R.id.chat_message_time);
            root = itemView.findViewById(R.id.chat_message_root);
        }

        public void bind(HashMap<String, String> messageMap) {
            Log.e("MAP", messageMap.toString());
            displayName.setText(messageMap.get(Users.FIELD_DISPLAY_NAME));
            messageText.setText(messageMap.get(Chats.FIELD_MESSAGE_TEXT));
            messageTime.setText(messageMap.get(Chats.FIELD_MESSAGE_TIME));
            if (messageMap.get(Chats.FIELD_MESSAGE_OWNER).equals(owner)) {
                int cardColor = super.itemView.getContext().getResources().getColor(R.color.bubble_own_background);
                cardView.setCardBackgroundColor(cardColor);
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(250, 8, 8, 8);
                root.setLayoutParams(params);
                int textColor = super.itemView.getContext().getResources().getColor(R.color.md_green_800);
                displayName.setTextColor(textColor);
            } else {
                int cardColor = super.itemView.getContext().getResources().getColor(R.color.bubble_background);
                cardView.setCardBackgroundColor(cardColor);
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(8, 8, 250, 8);
                root.setLayoutParams(params);
                int textColor = super.itemView.getContext().getResources().getColor(R.color.md_orange_900);
                displayName.setTextColor(textColor);
            }
        }
    }
}