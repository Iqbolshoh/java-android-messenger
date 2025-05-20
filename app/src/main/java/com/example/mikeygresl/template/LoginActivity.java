package com.example.mikeygresl.template;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.ExtractedTextRequest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

//class for login

public class LoginActivity extends AppCompatActivity {

    private final static String TAG = "Main Activity";


    //layout elements
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signinButton;
    private FloatingActionButton signupFab;
    private TextView singInText;

    //signin input elements
    private String email;
    private String password;

    //all users in FB
    private static List<User> users;

    //Firebase elements
    private FirebaseAuth authentication;                //Firebase authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initLayout();
        authentication = FirebaseAuth.getInstance();

        if (authentication.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), Contacts.class));
            finish();
            return;
        }

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = emailEditText.getText().toString();
                password = passwordEditText.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {        //check for empty fields

                    Toast.makeText(getApplicationContext(), "Empty Fields!", Toast.LENGTH_SHORT).show();
                } else {
                    signIn(email, password);
                }


            }
        });

        //link to singup if user doesn't have an account yet
        signupFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), Signup.class);
                finish();
                startActivity(intent);
            }
        });

    }

    //implementing native Firebase signIn function
    //if successful -> go to contacts activity, which contains all user contacts
    private void signIn(final String email, final String password) {

        authentication.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (authentication.getCurrentUser() != null) {

                    finish();
                    startActivity(new Intent(getApplicationContext(), Contacts.class));
                } else {

                    if (task.isSuccessful()) {

                        Log.d(TAG, "signInWithEmail:success");
                        Intent intent = new Intent(getApplicationContext(), Contacts.class);
                        finish();
                        startActivity(intent);
                    } else {

                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });


    }

    //initialize xml layout elements
    private void initLayout() {

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signinButton = findViewById(R.id.signupButton);
        signupFab = findViewById(R.id.signUpFab);
        singInText = findViewById(R.id.singInTxt);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/AvenirNextCyr-Bold.ttf");
        singInText.setTypeface(typeface);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        authentication = null;
    }
}
