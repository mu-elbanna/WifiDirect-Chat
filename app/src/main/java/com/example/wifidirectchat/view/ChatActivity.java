package com.example.wifidirectchat.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.wifidirectchat.Constants;
import com.example.wifidirectchat.R;
import com.example.wifidirectchat.model.MessageEntity;
import com.example.wifidirectchat.viewmodel.ChatPageViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    private View chatBox;
    private EditText newMessage;
    private MessageListAdapter adapter;
    private String addressee;
    private String startDate;
    private boolean isOffline;
    private ChatPageViewModel model;
    private ConstraintLayout loadingScreen;
    private ConstraintLayout messengerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initChatPage();
        setupToolbar();


        final AlertDialog.Builder adb = new AlertDialog.Builder(ChatActivity.this);
        final Boolean[] dialogActive = {Boolean.FALSE};
        final AlertDialog[] dialogs = {null};

        if (isOffline) {
            chatBox.setVisibility(View.GONE);
            model.setAddressee(addressee);
            model.getMessageList().observe(this, new Observer<List<MessageEntity>>() {
                @Override
                public void onChanged(@Nullable List<MessageEntity> messageEntities) {
                    adapter.updateData(messageEntities);
                }
            });
        } else {
            loadingScreen.setVisibility(View.VISIBLE);
            messengerLayout.setVisibility(View.GONE);
            Objects.requireNonNull(getSupportActionBar()).hide();

            model.startSearch();
            model.chatIsReady().observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                        loadingScreen.setVisibility(View.GONE);
                        messengerLayout.setVisibility(View.VISIBLE);
                        Objects.requireNonNull(getSupportActionBar()).show();
                        addressee = model.getAddressee();
                        getSupportActionBar().setTitle(addressee);
                        if (dialogActive[0]) {
                            dialogs[0].dismiss();
                        }
                        model.getMessageList().observe(ChatActivity.this, new Observer<List<MessageEntity>>() {
                            @Override
                            public void onChanged(@Nullable List<MessageEntity> messageEntities) {
                                adapter.updateData(messageEntities);
                            }
                        });
                }
            });


            model.getPeerList().observe(this, new Observer<List<WifiP2pDevice>>() {
                @Override
                public void onChanged(@Nullable final List<WifiP2pDevice> peers) {
                    // If we changed the list of peers, then we have to update the popup window as well
                    assert peers != null;
                    Log.d("", peers.toString());
                    if (peers.size() == 0)
                        return;
                    CharSequence[] items = new CharSequence[peers.size()];
                    final boolean[] checkedItems= new boolean[peers.size()];
                    int i = 0;
                    for (WifiP2pDevice wifiP2pDevice : peers) {
                        items[i] = wifiP2pDevice.deviceName;
                        i++;
                    }
                    adb.setMultiChoiceItems(items,checkedItems,new DialogInterface.OnMultiChoiceClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {

                            checkedItems[which]=isChecked;

                        }
                    });
                    adb.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            for (int i = 0; i < checkedItems.length; i++) {
                                boolean checked = checkedItems[i];
                                if (checked) {
                                    model.connectToPeer(peers.get(i));
                                    dialog.cancel();
                                }
                            }
                        }
                    });
                    adb.setNegativeButton("Cancel", null);
                    adb.setTitle("Which one?");
                    if (!dialogActive[0]) {
                        dialogs[0] = adb.show();
                        dialogActive[0] = true;
                    } else {
                        dialogs[0].dismiss();
                        dialogs[0] = adb.show();
                    }
                }
            });

            model.getChatClosed().observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                    // Action to close the chat
                    if (aBoolean == null || aBoolean) {
                        Toast.makeText(ChatActivity.this, "One of the speakers has left the chat", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }

    }

    private void initChatPage() {
        messengerLayout = findViewById(R.id.messengerLayout);
        chatBox = findViewById(R.id.layout_chatbox);
        loadingScreen = findViewById(R.id.loading_screen);
        loadingScreen.setVisibility(View.GONE);
        findViewById(R.id.stopSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOffline) {
                    model.closeChat();
                }
                finish();
            }
        });
        isOffline = getIntent().getBooleanExtra(Constants.IS_OFFLINE, false);
        model = ViewModelProviders.of(this).get(ChatPageViewModel.class);
        addressee = getIntent().getStringExtra(Constants.ADDRESAT_NAME);
        startDate = getIntent().getStringExtra(Constants.DATE);
        newMessage = findViewById(R.id.edittext_chatbox);
        ImageButton sendMessage = findViewById(R.id.button_chatbox_send);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newMessage.getText().toString().length() == 0)
                    return;
                model.sendMessage(newMessage.getText().toString());
                newMessage.setText("");
            }
        });

        RecyclerView messages = findViewById(R.id.reyclerview_message_list);
        messages.setLayoutManager(new LinearLayoutManager(this, 1, true));
        adapter = new MessageListAdapter(new ArrayList<MessageEntity>());
        messages.setAdapter(adapter);
    }


    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(addressee);
        if (isOffline)
            getSupportActionBar().setSubtitle(startDate);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOffline) {
                    model.closeChat();
                }
                finish();
            }
        });
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left_24dp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isOffline)
            return false;
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.delete_button) {
            model.deleteChat();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        model.registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        model.unregisterBroadcast();
    }
}
