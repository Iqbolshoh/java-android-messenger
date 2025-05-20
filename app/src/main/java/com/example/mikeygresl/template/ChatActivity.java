package com.example.mikeygresl.template;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.mikeygresl.template.simplegallery_v2.GalleryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity
    implements View.OnClickListener, View.OnLongClickListener {

  //whenever a user signs in
  //contacts activity opens
  //there is a recycler view of contacts
  //whenever a user selects a contact
  //new activity chat is opened
  //all his messages are read
  //and added to the layout
  //whenever a user writes a message

  //need to implement messagesRef here

  private final TextView.OnEditorActionListener onEditingActions =
      new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
          if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
              && event.getAction() == KeyEvent.ACTION_DOWN) {
            return true;
          }
          return false;
        }
      };

  private RecyclerView messagesRecyclerView;          //recycler view to display messages
  private MessagesAdapter messagesAdapter;            //adapter for messagesRecyclerView
  private EditText messageEditText;                   //input for messages
  private Dialog pendingDisplayedDialog;              //dialog to paste image URL
  private LinearLayoutManager lm;                     //layout manager for messagesRecyclerView
  private ImageView action_send_message;              //btn to send message
  private ImageView action_select_image;              //btn to attach image
  private TextView backToMain;

  private FirebaseAuth authentication;
  private FirebaseUser currentFBUser;
  private DatabaseReference dbref;
  private DatabaseReference chatsRef;
  private DatabaseReference messagesRef;                 //reference to chats db to write messages

  private String CID;
  private String sender_email;
  private String rec_email;
  private String sender_id;
  private String rec_fullname;
  private List<Message> currentMessages;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);

    Intent intent = getIntent();
    CID = intent.getStringExtra("CID");
    sender_id = intent.getStringExtra("sender_id");
    sender_email = intent.getStringExtra("sender_email");
    rec_email = intent.getStringExtra("rec_email");
    rec_fullname = intent.getStringExtra("rec_fname") + " " + intent.getStringExtra("rec_lname");

    initLayout();
    initFirebase();

    //        Picasso.setSingletonInstance(new Picasso.Builder(this)
    //                .loggingEnabled(true)
    //                .build());

    //get information about sender and receiver

    messagesRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        lm = null;

        currentMessages = new ArrayList<>();
        currentMessages = initMessages(dataSnapshot);
        Log.d("CMsgs", Integer.toString(currentMessages.size()));
        messagesAdapter = new MessagesAdapter();

        for (int i = 0; i < currentMessages.size(); i++) {

          Message message = new Message(
              currentMessages.get(i).getMID(),
              currentMessages.get(i).getTimestamp(),
              currentMessages.get(i).getType(),
              currentMessages.get(i).getSender_email(),
              currentMessages.get(i).getRec_email(),
              currentMessages.get(i).getContent(),
              currentMessages.get(i).getURL());
          message.setSender_id(sender_id);

          //MUST SET GRAVITY HERE

          messagesAdapter.addMessage(message);
        }

        messagesRecyclerView.setAdapter(messagesAdapter);

        Log.d("Listener1", "EXECUTED");
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

        Log.w("FIREBASE", "chatsRef:onCancelled", databaseError.toException());
      }
    });

    action_send_message.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onMessageSend("None");
      }
    });

    //when you press attach btn
    backToMain.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });

    action_select_image.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), GalleryActivity.class);
        startActivityForResult(intent, 100);
      }
    });

    messagesAdapter.setOnMessageClickListener(this);
    messagesAdapter.setOnLongClickListener(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    getMenuInflater().inflate(R.menu.menu_chat, menu);
    return true;
  }

  @Override
  public boolean onSupportNavigateUp() {
    onBackPressed();
    return super.onSupportNavigateUp();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    int id = item.getItemId();

    if (id == R.id.signout) {

      authentication.signOut();
      finish();
      Contacts.getInstance().finish();
    }

    return super.onOptionsItemSelected(item);
  }

  //function to send messages with and without image attached
  private void onMessageSend(String imageUrl) {
    if (messageEditText.length() > 0 || !TextUtils.isEmpty(imageUrl)) {
      final String text = messageEditText.getText().toString();

      //        chatsRef.child(CID).setValue(true);
      String MID = chatsRef.child(CID).push().getKey();
      final Message message = new Message();
      message.setMID(MID);
      message.setType("text");
      message.setSender_email(sender_email);
      message.setRec_email(rec_email);
      message.setContent(text);
      message.setURL(imageUrl);

      chatsRef.child(CID).child(message.getMID()).setValue(message);

      messageEditText.setText(null);

      Log.d("email", sender_email);
      messagesRecyclerView.scrollToPosition(messagesAdapter.getItemCount() + 1);
    }
  }

  @Override
  public void onClick(View v) {

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 100 && resultCode == RESULT_OK) {
      if (data != null && data.getData() != null) {
        final Uri photoUri = data.getData();
        onMessageSend(photoUri.toString());
      }
    }
  }

  @Override
  public boolean onLongClick(View v) {
    final int adapterPosition = messagesRecyclerView.getChildAdapterPosition(v);
    final Message message = messagesAdapter.getMessage(adapterPosition);

    new AlertDialog.Builder(this)
        .setTitle("Message Actions")
        .setItems(new CharSequence[] { "Copy" }, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            switch (which) {
              case 0:
                final ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (cm != null) {
                  cm.setPrimaryClip(ClipData.newPlainText("message", message.getContent()));
                }
                break;
            }
          }
        })
        .show();
    return true;
  }

  //init layout from xml
  private void initLayout() {

    //        toolbar = findViewById(R.id.toolbar_chats);
    //        setSupportActionBar(toolbar);
    //        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    //        getSupportActionBar().setDisplayShowHomeEnabled(true);
    //        toolbar.setNavigationIcon(R.drawable.back_arrow);

    messageEditText = findViewById(R.id.messageEditText);
    messageEditText.setOnEditorActionListener(onEditingActions);

    messagesAdapter = new MessagesAdapter();
    action_send_message = findViewById(R.id.action_send_message);
    action_select_image = findViewById(R.id.action_select_image);

    lm = new LinearLayoutManager(getApplicationContext());
    lm.setReverseLayout(false);
    lm.setStackFromEnd(true);

    messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
    messagesRecyclerView.setLayoutManager(lm);
    messagesRecyclerView.setAdapter(messagesAdapter);

    TextView chatName = findViewById(R.id.chatName);
    chatName.setText(rec_fullname);
    Typeface tgFont = Typeface.createFromAsset(getAssets(), "fonts/AvenirNextCyr-Medium.ttf");
    chatName.setTypeface(tgFont);
    backToMain = findViewById(R.id.backToMain);
  }

  private void initFirebase() {

    authentication = FirebaseAuth.getInstance();
    currentFBUser = authentication.getCurrentUser();
    dbref = FirebaseDatabase.getInstance().getReference();
    chatsRef = dbref.child("chats");
    messagesRef = dbref.child("chats/" + CID);
  }

  private List<Message> initMessages(DataSnapshot dataSnapshot) {

    //get only the chats
    //where CID contains current UID
    final List<Message> messages = new ArrayList<>();

    if (dataSnapshot.getChildren() != null) {

      for (DataSnapshot messagesSnapshot : dataSnapshot.getChildren()) {

        Message message = messagesSnapshot.getValue(Message.class);
        messages.add(message);
      }
    }
    return messages;
  }

  @Override
  public void finish() {
    super.finish();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    chatsRef = null;
    messagesRef = null;
    dbref = null;
    currentFBUser = null;
    authentication = null;
  }
}

