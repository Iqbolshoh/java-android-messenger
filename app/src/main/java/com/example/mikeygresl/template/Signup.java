package com.example.mikeygresl.template;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//class for registration

public class Signup extends AppCompatActivity {

    private final static String TAG = "Signup Activity";

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText fnameEditText;
    private EditText lnameEditText;
    private Button singupButton;
    private FloatingActionButton singinFab;
    private TextView signupTxt;

    private String UID;
    private String email;
    private String password;
    private String fname;
    private String lname;

    private FirebaseAuth authentication;            //authentication
    private DatabaseReference dbref;                //reference for whole DB
    private DatabaseReference usersRef;                //reference for users table
    private DatabaseReference contactsRef;          //reference for contacts table

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initLayout();

        authentication = FirebaseAuth.getInstance();            //initialize authentication
        dbref = FirebaseDatabase.getInstance().getReference();  //initialize DB
        usersRef = dbref.child("users");                           //get reference to all users
        contactsRef = dbref.child("contacts");                  //get reference to all contacts

        singupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fname = fnameEditText.getText().toString();
                lname = lnameEditText.getText().toString();
                email = emailEditText.getText().toString();
                password = passwordEditText.getText().toString();
                signUp(fname, lname, email, password);

            }
        });

        singinFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                finish();
                startActivity(intent);
            }
        });

    }

    //create new user by email
    //authenticate it in native authentication of Firebase
    //create new entry in users table
    //create new entry in contacts table
    //each UID in users is mapped to Authentication and contacts
    private void signUp(final String fname, final String lname, final String email, final String password) {

        authentication.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                try {

                    if (task.isSuccessful()) {

                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser currentUser = authentication.getCurrentUser();

                        UID = currentUser.getUid();
                        User user = new User(UID, fname, lname, email);

                        usersRef.child(user.getUID()).child("uid").setValue(user.getUID());
                        usersRef.child(user.getUID()).child("fname").setValue(user.getFname());
                        usersRef.child(user.getUID()).child("lname").setValue(user.getLname());
                        usersRef.child(user.getUID()).child("email").setValue(user.getEmail());

                        //encrypt password
                        usersRef.child(user.getUID()).child("password").setValue(password);

                        contactsRef.child(user.getUID()).setValue(true);
                        Toast.makeText(getApplicationContext(), "Email " + email + " successfully created", Toast.LENGTH_LONG).show();
                    }
                    else {

                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Failure", Toast.LENGTH_LONG).show();
                    }
                }
                catch (Exception e) {

                    e.printStackTrace();
                }
            }
        });
    }


    //initialize xml layout elements
    private void initLayout() {

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        fnameEditText = findViewById(R.id.fnameEditText);
        lnameEditText = findViewById(R.id.lnameEditText);
        singinFab = findViewById(R.id.signInFab);
        singupButton = findViewById(R.id.signupButton);
        signupTxt = findViewById(R.id.signupText);

        Typeface avenir = Typeface.createFromAsset(getAssets(), "fonts/AvenirNextCyr-Bold.ttf");
        signupTxt.setTypeface(avenir);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        usersRef = null;
        contactsRef = null;
        dbref = null;
        authentication = null;
    }

}
