package com.coal.projects.chat.presentation.chat;


import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.MenuItem;
import com.coal.projects.chat.R;
import com.coal.projects.chat.databinding.ActivityChatBinding;
import com.coal.projects.chat.domain.utils.MessagesDiffUtilCallback;
import com.coal.projects.chat.firestore_constants.Chats;
import com.coal.projects.chat.presentation.base.BaseActivity;
import com.coal.projects.chat.presentation.base.ModelFactory;

import java.util.ArrayList;
import java.util.HashMap;

import static com.coal.projects.chat.NotificationHelper.CHAT_INNER_ACTION;

public class ChatActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ActivityChatBinding binding;
    private ChatViewModel viewModel;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        notifyScreen();
        setTitle(getToolbarTitle());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        viewModel = ViewModelProviders.of(this, new ModelFactory()).get(ChatViewModel.class);
        viewModel.setSavedInstanceState(savedInstanceState);
        getLifecycle().addObserver(viewModel);
        viewModel.setChatId(getIntent());


        binding.chatRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.chatRecycler.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(viewModel.getOwner());
        binding.chatRecycler.setAdapter(chatAdapter);

        setupSwipeRefresh();

        viewModel.isProgress.observe(this, this::toggleProgress);

        binding.sendButton.setOnClickListener(v -> {
            viewModel.sendMessage(binding.inputMessage.getText().toString());
            binding.inputMessage.setText("");
        });

        viewModel.messages.observe(this, this::updateMessages);

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.e("TITLE", getToolbarTitle());
        setTitle(getToolbarTitle());
    }

    private void notifyScreen() {
        Intent intent = getIntent();
        intent.setAction(CHAT_INNER_ACTION);
        sendBroadcast(intent);
    }

    private String getToolbarTitle() {
        return getIntent().getStringExtra(Chats.FIELD_DISPLAY_NAMES);
    }

    private void setupSwipeRefresh() {
        binding.chatRefresh.setOnRefreshListener(this);
        binding.chatRefresh.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void toggleProgress(Boolean isProgress) {
        binding.chatRefresh.setRefreshing(isProgress);
    }

    private void updateMessages(ArrayList<HashMap<String, String>> messages) {
        MessagesDiffUtilCallback messagesDiffUtilCallback =
                new MessagesDiffUtilCallback(chatAdapter.getItems(), messages);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(messagesDiffUtilCallback);

        chatAdapter.setItems(messages);
        diffResult.dispatchUpdatesTo(chatAdapter);
        binding.chatRecycler.post(() -> {
            // Call smooth scroll
            if (chatAdapter.getItemCount() > 0)
                binding.chatRecycler.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        viewModel.loadMessages();
    }


}
