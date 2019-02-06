package com.coal.projects.chat.domain.utils;

import android.support.v7.util.DiffUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class MessagesDiffUtilCallback extends DiffUtil.Callback {

    private final ArrayList<HashMap<String, String>> oldList;
    private final ArrayList<HashMap<String, String>> newList;

    public MessagesDiffUtilCallback(ArrayList<HashMap<String, String>> oldList, ArrayList<HashMap<String, String>> newList) {
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
        HashMap<String, String> oldMessage = oldList.get(oldItemPosition);
        HashMap<String, String> newMessage = newList.get(newItemPosition);
        return oldMessage.equals(newMessage);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        HashMap<String, String> oldMessage = oldList.get(oldItemPosition);
        HashMap<String, String> newMessage = newList.get(newItemPosition);
        return oldMessage.equals(newMessage);
    }
}
