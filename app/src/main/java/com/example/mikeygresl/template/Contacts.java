package com.example.mikeygresl.template;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Activity containing all info about current User
//his personal data
//his contacts
//his chats
//all users - to search for a new contact

public class Contacts extends AppCompatActivity implements View.OnClickListener {

  private static Contacts contactsActivity;

  private RecyclerView contactsRecyclerView;
  private RecyclerView.Adapter contactsAdapter;
  private RecyclerView.LayoutManager contactsLayoutManager;
  private EditText searchEditText;
  private ImageView searchBtn;

  private User currentUser;
  private static List<User> users = Collections.emptyList();
  private List<Contact> contacts;
  private final List<Conversation> chats = new ArrayList<>();

  private FirebaseAuth authentication;
  private FirebaseUser currentFbUser;
  private DatabaseReference dbref;
  private DatabaseReference usersRef;
  private DatabaseReference contactsRef;
  private DatabaseReference chatsRef;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contacts);

    contactsActivity = this;

    initLayout();
    initFirebase();

    usersRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        initUsers(dataSnapshot);
        initCurrentUser();
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {
        Log.w("FIREBASE", "usersRef:onCancelled", databaseError.toException());
      }
    });

    contactsRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        initContacts(dataSnapshot);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {
        Log.w("FIREBASE", "contactsRef:onCancelled", databaseError.toException());
      }
    });

    chatsRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        initChats(dataSnapshot);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

        Log.w("FIREBASE", "chatsRef:onCancelled", databaseError.toException());
      }
    });

    searchBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String searchEmail = searchEditText.getText().toString();
        boolean found = false;

        if (searchEmail.isEmpty()) {
          Toast.makeText(getApplicationContext(), "Email field is empty!", Toast.LENGTH_SHORT)
              .show();
        } else {
          for (int i = 0; i < users.size(); i++) {
            if (searchEmail.equals(users.get(i).getEmail())) {
              found = true;
              contactsRef.child(users.get(i).getUID()).setValue(true);
              break;
            }
          }

          if (found) {
            Toast.makeText(getApplicationContext(), searchEmail + " added to your contacts!",
                Toast.LENGTH_SHORT).show();
          } else {
            Toast.makeText(getApplicationContext(), searchEmail + " doesn't exist!",
                Toast.LENGTH_SHORT).show();
          }
        }
      }
    });
  }

  public static Contacts getInstance() {
    return contactsActivity;
  }

  @Override
  public void finish() {
    super.finish();
    authentication.signOut();
  }

  private void initLayout() {

    //        toolbar = findViewById(R.id.toolbar);
    //        setSupportActionBar(toolbar);
    //        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    //        getSupportActionBar().setDisplayShowHomeEnabled(true);
    //        toolbar.setNavigationIcon(R.drawable.back_arrow);

    searchEditText = findViewById(R.id.addContact);
    searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
          searchBtn.performClick();
          return true;
        }
        return false;
      }
    });
    searchBtn = findViewById(R.id.searchBtn);
  }

  private void initFirebase() {

    authentication = FirebaseAuth.getInstance();
    currentFbUser = authentication.getCurrentUser();
    dbref = FirebaseDatabase.getInstance().getReference();
    usersRef = dbref.child("users");
    contactsRef = dbref.child("contacts/" + currentFbUser.getUid());
    chatsRef = dbref.child("chats");
  }

  private void initUsers(DataSnapshot dataSnapshot) {

    users = new ArrayList<User>();

    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

      User user = snapshot.getValue(User.class);
      users.add(user);
    }
  }

  private void initCurrentUser() {

    currentUser = new User();
    currentUser.setUID(currentFbUser.getUid());
    Log.d("UID", currentUser.getUID());

    for (int i = 0; i < users.size(); i++) {

      if (currentUser.getUID().equals(users.get(i).getUID())) {

        currentUser.setFname(users.get(i).getFname());
        currentUser.setLname(users.get(i).getLname());
        currentUser.setEmail(users.get(i).getEmail());
      }
    }
  }

  private void initContacts(DataSnapshot dataSnapshot) {

    contacts = new ArrayList<Contact>();

    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

      Contact contact = new Contact();
      contact.setUID(snapshot.getKey());

      for (int i = 0; i < users.size(); i++) {

        if (contact.getUID().equals(users.get(i).getUID())) {

          contact.setEmail(users.get(i).getEmail());
          contact.setFname(users.get(i).getFname());
          contact.setLname(users.get(i).getLname());
          contacts.add(contact);
        }
      }
    }

    //initialize recycler view contacts of current user
    contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
    contactsRecyclerView.setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          clearSearchInputFocus();
        }
        return false;
      }
    });

    findViewById(R.id.parent).setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          clearSearchInputFocus();
        }
        return false;
      }
    });

    contactsRecyclerView.setHasFixedSize(true);
    contactsLayoutManager = new LinearLayoutManager(getApplicationContext());
    contactsRecyclerView.setLayoutManager(contactsLayoutManager);
    contactsRecyclerView.setItemAnimator(new DefaultItemAnimator());
    contactsAdapter = new ContactsAdapter(contacts);
    ((ContactsAdapter) contactsAdapter).setOnContactClickListener(this);
    contactsRecyclerView.setAdapter(contactsAdapter);
  }

  private void clearSearchInputFocus() {
    if (searchEditText.isFocused()) {
      searchEditText.clearFocus();
    }

    final InputMethodManager service = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    if (service != null) {
      service.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }
  }

  private void initChats(DataSnapshot dataSnapshot) {

    //get only the chats
    //where CID contains current UID
    if (dataSnapshot.getChildren() != null) {

      for (DataSnapshot chatsSnapshot : dataSnapshot.getChildren()) {

        if (chatsSnapshot.getKey().contains(currentFbUser.getUid())) {

          Conversation chat = new Conversation(chatsSnapshot.getKey());
          chats.add(chat);
        }
      }
    }
  }

  @Override
  public void onClick(View v) {

    //check CID already exist
    //if so, load the data
    //else create new CID

    final Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
    final int adapterPosition = contactsRecyclerView.getChildAdapterPosition(v);

    String CID = currentUser.getUID() + contacts.get(adapterPosition).getUID();
    currentUser.setChats(chats);

    for (int i = 0; i < currentUser.getChats().size(); i++) {

      if (currentUser.getChats().get(i).getCID().contains(contacts.get(adapterPosition).getUID())) {

        CID = currentUser.getChats().get(i).getCID();
        Log.d("mosby", "true");
      }
    }

    chatIntent.putExtra("CID", CID);
    chatIntent.putExtra("sender_id", currentUser.getUID());
    chatIntent.putExtra("rec_fname", contacts.get(adapterPosition).getFname());
    chatIntent.putExtra("rec_lname", contacts.get(adapterPosition).getLname());
    chatIntent.putExtra("sender_email", currentUser.getEmail());
    chatIntent.putExtra("rec_email", contacts.get(adapterPosition).getEmail());

    startActivity(chatIntent);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    usersRef = null;
    contactsRef = null;
    chatsRef = null;
    dbref = null;
    currentFbUser = null;
    authentication = null;
  }
}
