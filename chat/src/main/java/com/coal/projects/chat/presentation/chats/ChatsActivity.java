package com.coal.projects.chat.presentation.chats;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import com.coal.projects.chat.R;
import com.coal.projects.chat.databinding.ActivityChatsBinding;
import com.coal.projects.chat.firestore_constants.Chats;
import com.coal.projects.chat.presentation.base.BaseActivity;
import com.coal.projects.chat.presentation.base.ModelFactory;
import com.coal.projects.chat.presentation.chats.chat.ChatActivity;
import com.coal.projects.chat.presentation.contacts.ContactsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

public class ChatsActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ActivityChatsBinding binding;
    private ChatsViewModel viewModel;

    private Drawer drawer;
    public static final int CONTACTS_ID = 100;
    private PrimaryDrawerItem item1;
    private ChatsAdapter adapter;
    public static int REQUEST_CODE_PICK_CONTACTS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_chats);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewModel = ViewModelProviders.of(this, new ModelFactory()).get(ChatsViewModel.class);
        getLifecycle().addObserver(viewModel);

        viewModel.isProgress.observe(this, progress -> {
            binding.setModel(viewModel);
            binding.chatsSwipeRefresh.setRefreshing(progress);
        });

        binding.chatsRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.chatsRecyclerView.setLayoutManager(layoutManager);
        adapter = new ChatsAdapter();
        adapter.setOnClickCallback(this::openChatScreen);
        binding.chatsRecyclerView.setAdapter(adapter);
        viewModel.chats.observe(this, chats -> {
            ChatsDiffUtilCallback chatsDiffUtilCallback =
                    new ChatsDiffUtilCallback(adapter.getItems(), chats);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(chatsDiffUtilCallback);
            adapter.setItems(chats);
            diffResult.dispatchUpdatesTo(adapter);
        });

        viewModel.chatCreated.observe(this, chatId -> new Handler().post(() -> openChatScreen(chatId)));

        binding.chatsSwipeRefresh.setOnRefreshListener(this);
        binding.chatsSwipeRefresh.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        item1 = new PrimaryDrawerItem()
                .withIdentifier(CONTACTS_ID)
                .withName(getString(R.string.title_contacts))
                .withIcon(R.drawable.contacts);

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withToolbar(toolbar)
                .withSliderBackgroundColorRes(R.color.md_white_1000)
                .addDrawerItems(item1)
                .withOnDrawerItemClickListener((view, position, drawerItem) -> itemClicked(drawerItem))
                .withCloseOnClick(true)
                .withMultiSelect(false)
                .build();

        binding.fabNewChat.setOnClickListener(v -> pickContacts());
    }

    private void openChatScreen(String chatId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Chats.FIELD_CHAT_ID, chatId);
        startActivity(intent);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_signout) {
            FirebaseAuth.getInstance().signOut();
            finish();
        }
        return true;
    }

    private void pickContacts() {
        startActivityForResult(new Intent(this, ContactsActivity.class), REQUEST_CODE_PICK_CONTACTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_CONTACTS && requestCode == RESULT_OK) {
//            viewModel.createNewChat(mSelectedItems);
        }
    }

    @Override
    public void onRefresh() {
        viewModel.loadChats();
    }
}
