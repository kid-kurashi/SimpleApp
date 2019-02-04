package com.coal.projects.chat.presentation.contacts;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class ContactsDiffUtilCallback extends DiffUtil.Callback {

    private final List<String> oldList;
    private final List<String> newList;

    public ContactsDiffUtilCallback(List<String> oldList, List<String> newList) {
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
        String oldString = oldList.get(oldItemPosition);
        String newString = newList.get(newItemPosition);
        return oldString.equals(newString);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        String oldString = oldList.get(oldItemPosition);
        String newString = newList.get(newItemPosition);
        return oldString.equals(newString);
    }
}
