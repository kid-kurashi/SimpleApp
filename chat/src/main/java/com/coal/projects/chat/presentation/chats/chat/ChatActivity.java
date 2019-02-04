package com.coal.projects.chat.presentation.chats.chat;


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
import android.view.MenuItem;
import com.coal.projects.chat.R;
import com.coal.projects.chat.databinding.ActivityChatBinding;
import com.coal.projects.chat.firestore_constants.Chats;
import com.coal.projects.chat.presentation.base.BaseActivity;
import com.coal.projects.chat.presentation.base.ModelFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private BroadcastReceiver bReceiver;
    public static final String RECEIVE_MESSAGE = "com.example.firebasechat.RECEIVE_MESSAGE";
    private LocalBroadcastManager bManager;

    private ActivityChatBinding binding;
    private ChatViewModel viewModel;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        viewModel = ViewModelProviders.of(this, new ModelFactory()).get(ChatViewModel.class);
        getLifecycle().addObserver(viewModel);
        viewModel.setChatId(getIntent().getStringExtra(Chats.FIELD_CHAT_ID));


        binding.chatRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.chatRecycler.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(viewModel.getOwner());
        binding.chatRecycler.setAdapter(chatAdapter);

        binding.chatRefresh.setOnRefreshListener(this);
        binding.chatRefresh.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        viewModel.isProgress.observe(this, this::toggleProgress);


        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ((intent != null)
                        && (intent.getAction() != null)
                        && intent.getAction().equals(RECEIVE_MESSAGE)) {
                    onReceivedNewIntent(intent);
                }
            }
        };

        bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVE_MESSAGE);
        bManager.registerReceiver(bReceiver, intentFilter);

        binding.sendButton.setOnClickListener(v -> {
            viewModel.sendMessage(binding.inputMessage.getText().toString());
            binding.inputMessage.setText("");
        });

        viewModel.messages.observe(this, this::updateMessages);

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

    private void onReceivedNewIntent(Intent intent) {

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
