package com.coal.projects.chat.domain.utils;

import android.support.v7.util.DiffUtil;
import com.coal.projects.chat.presentation.contacts.SelectableUser;

import java.util.List;

public class ContactsDiffUtilCallback extends DiffUtil.Callback {

    private final List<SelectableUser> oldList;
    private final List<SelectableUser> newList;

    public ContactsDiffUtilCallback(List<SelectableUser> oldList, List<SelectableUser> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        SelectableUser oldString = oldList.get(oldItemPosition);
        SelectableUser newString = newList.get(newItemPosition);
        return oldString.equals(newString);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        SelectableUser oldString = oldList.get(oldItemPosition);
        SelectableUser newString = newList.get(newItemPosition);
        return oldString.equals(newString);
    }
}
