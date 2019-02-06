package com.coal.projects.chat.presentation.chats;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.coal.projects.chat.R;
import com.coal.projects.chat.creation.ChatInstance;
import com.coal.projects.chat.databinding.ActivityChatsBinding;
import com.coal.projects.chat.domain.utils.ChatsDiffUtilCallback;
import com.coal.projects.chat.firestore_constants.Chats;
import com.coal.projects.chat.presentation.base.ModelFactory;
import com.coal.projects.chat.presentation.chat.ChatActivity;
import com.coal.projects.chat.presentation.contacts.ContactsActivity;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.List;
import java.util.Map;

import static com.coal.projects.chat.NotificationHelper.CHAT_INNER_ACTION;

public class ChatsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ActivityChatsBinding binding;
    private ChatsViewModel viewModel;

    private Drawer drawer;
    public static final int CONTACTS_ID = 100;
    private PrimaryDrawerItem contactsItem;
    private ChatsAdapter adapter;
    public static int REQUEST_CODE_PICK_CONTACTS = 123;

    private View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notifyScreen();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chats);
        setSupportActionBar(binding.toolbar);
        headerView = getLayoutInflater().inflate(R.layout.account_header, null, false);

        viewModel = ViewModelProviders.of(this, new ModelFactory()).get(ChatsViewModel.class);
        viewModel.setSavedInstanceState(savedInstanceState);
        viewModel.setRemoveCallback(() -> toggleMenu(false));
        getLifecycle().addObserver(viewModel);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.chatsRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.chatsRecyclerView.getContext(),
                layoutManager.getOrientation());
        binding.chatsRecyclerView.addItemDecoration(dividerItemDecoration);

        adapter = new ChatsAdapter(ChatInstance.getInstance().getImageLoader(), this::toggleMenu);
        adapter.setOnClickCallback(this::openChatScreen);
        binding.chatsRecyclerView.setAdapter(adapter);

        setupSwipeRefresh();

        contactsItem = getContactsItem();
        drawer = getDrawer(findViewById(R.id.toolbar));

        viewModel.isProgress.observe(this, this::toggleProgress);
        viewModel.chats.observe(this, this::setChats);
        viewModel.chatCreated.observe(this, this::openChatScreen);
        viewModel.conditionTitle.observe(this, this::setTitle);
        viewModel.isConnectingProgress.observe(this, this::toggleConnectingProgress);

        binding.fabNewChat.setOnClickListener(v -> pickContacts());

        setOriginalStatusBar();
    }

    private void toggleMenu(boolean selectionMode) {
        binding.toolbar.getMenu().clear();
        if (selectionMode)
            binding.toolbar.inflateMenu(R.menu.delete);
        else
            binding.toolbar.inflateMenu(R.menu.empty);
    }

    private void setupSwipeRefresh() {
        binding.chatsSwipeRefresh.setOnRefreshListener(this);
        binding.chatsSwipeRefresh.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void toggleProgress(Boolean progress) {
        binding.setModel(viewModel);
        binding.chatsSwipeRefresh.setRefreshing(progress);
    }

    private void toggleConnectingProgress(Boolean aBoolean) {
        if (aBoolean)
            binding.connectionProgress.setVisibility(View.VISIBLE);
        else
            binding.connectionProgress.setVisibility(View.GONE);
    }

    private PrimaryDrawerItem getContactsItem() {
        return new PrimaryDrawerItem()
                .withIdentifier(CONTACTS_ID)
                .withName(getString(R.string.title_contacts))
                .withIcon(R.drawable.contacts);
    }

    private Drawer getDrawer(Toolbar toolbar) {
        return new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withToolbar(toolbar)
                .withSliderBackgroundColorRes(R.color.md_white_1000)
                .addDrawerItems(contactsItem)
                .withOnDrawerItemClickListener((view, position, drawerItem) -> itemClicked(drawerItem))
                .withCloseOnClick(true)
                .withMultiSelect(false)
//                .withAccountHeader(new AccountHeaderBuilder().withAccountHeader(headerView).build())
                .build();
    }

    private String getProfileUrl() {
        return null;
    }

    private void setChats(List<Map<String, Object>> chats) {
        ChatsDiffUtilCallback chatsDiffUtilCallback =
                new ChatsDiffUtilCallback(adapter.getItems(), chats);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(chatsDiffUtilCallback);
        adapter.setItems(chats);
        diffResult.dispatchUpdatesTo(adapter);
    }

    private void setOriginalStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
    }

    private void openChatScreen(CreatedChat chat) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Chats.FIELD_CHAT_ID, chat.getChatId());
        intent.putExtra(Chats.FIELD_DISPLAY_NAMES, chat.getChatTitle());
        startActivity(intent);
    }

    private void notifyScreen() {
        Intent intent = getIntent();
        intent.setAction(CHAT_INNER_ACTION);
        sendBroadcast(intent);
    }

    private boolean itemClicked(IDrawerItem drawerItem) {
        drawer.closeDrawer();
        switch ((int) drawerItem.getIdentifier()) {
            case CONTACTS_ID: {
                startActivity(new Intent(this, ContactsActivity.class));
            }
            return true;
            default:
                return false;
        }
    }

    private void pickContacts() {
        Intent intent = new Intent(this, ContactsActivity.class);
        intent.putExtra("SELECTION_MODE", "SELECTION_MODE");
        startActivityForResult(intent, REQUEST_CODE_PICK_CONTACTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            viewModel.createNewChat(viewModel.transformResult(data));
        }
    }

    @Override
    public void onRefresh() {
        viewModel.observeChats();
    }

    @Override
    public void onBackPressed() {
        if (adapter.detectedSelectMode()) {
            adapter.dropSelection();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            viewModel.removeSelected(adapter.getItems());
            return true;
        }
        return false;
    }
}
