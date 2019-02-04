package com.coal.projects.chat.presentation.contacts;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.coal.projects.chat.ImageLoader;
import com.coal.projects.chat.R;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private List<String> items = new ArrayList<>();

    private ImageLoader imageLoader;

    public List<String> getItems() {
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

    void setItems(List<String> items) {
        this.items = items;
    }

    void setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView avatar;
        final TextView displayName;
        final TextView login;

        ViewHolder(View v) {
            super(v);
            avatar = v.findViewById(R.id.contacts_user_avatar);
            displayName = v.findViewById(R.id.contacts_user_displayname);
            login = v.findViewById(R.id.contacts_user_login);
        }

        void bind(String model) {
            login.setText(model);
        }
    }
}