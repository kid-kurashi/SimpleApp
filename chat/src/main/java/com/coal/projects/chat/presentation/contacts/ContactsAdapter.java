package com.coal.projects.chat.presentation.contacts;


import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.coal.projects.chat.ImageLoader;
import com.coal.projects.chat.R;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private final boolean selection_mode;
    private final TouchCallback callback;
    private List<SelectableUser> items = new ArrayList<>();

    private ImageLoader imageLoader;

    public ContactsAdapter(
            ImageLoader imageLoader,
            boolean selection_mode,
            TouchCallback callback) {
        this.imageLoader = imageLoader;
        this.selection_mode = selection_mode;
        this.callback = callback;
    }

    public List<SelectableUser> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contacts_user_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    void setItems(List<SelectableUser> list) {
        items = list;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView avatar;
        final TextView displayName;
        final TextView login;
        final CheckBox checkBox;
        final ConstraintLayout root;

        ViewHolder(View v) {
            super(v);
            root = v.findViewById(R.id.contact_root);
            avatar = v.findViewById(R.id.contacts_user_avatar);
            displayName = v.findViewById(R.id.contacts_user_displayname);
            login = v.findViewById(R.id.contacts_user_login);
            checkBox = v.findViewById(R.id.contacts_user_checkbox);
        }

        void bind(SelectableUser model) {
            if (imageLoader == null)
                throw new RuntimeException("Image loader must not be null ! (from \" void bind(User user) \" )");
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                model.setSelected(isChecked);
                callback.onTouch(items);
            });
            if (selection_mode) {
                root.setOnClickListener(v -> checkBox.performClick());
                checkBox.setVisibility(View.VISIBLE);
            } else
                checkBox.setVisibility(View.GONE);
            login.setText(model.getLogin());
            imageLoader.loadImageInto(model.getImageUrl(), avatar);
            displayName.setText(model.getDisplayName());
        }
    }

    public interface TouchCallback {
        void onTouch(List<SelectableUser> items);
    }
}