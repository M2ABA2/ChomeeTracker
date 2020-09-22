package com.makoele.chomeetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button login, register,btnResetPassword;
    ProgressBar progressBar;
    FirebaseAuth auth;

    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.txtEmail);
        password = findViewById(R.id.txtPassword);
        login = findViewById(R.id.btnLogin);
        register = findViewById(R.id.btnRegister);
        btnResetPassword =  findViewById(R.id.btnResetPass);
        progressBar = findViewById(R.id.progressBar);



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                login();
            }
        });



        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(LoginActivity.this, SignUp.class);
                startActivity(i);
            }
        });

        //when resetPassword button is clicked, the ResetPassword activity opens
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });
    }

    //Login Method
    public void login()
    {
        String emailInput = email.getText().toString();
        String passwordInput = password.getText().toString();

        if (TextUtils.isEmpty(emailInput))
        {
            Toast.makeText(this,"Please enter your email!",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(passwordInput))
        {
            Toast.makeText(this,"Please type in your Password!",Toast.LENGTH_SHORT).show();
        }
        else
        {
            progressBar.setVisibility(View.VISIBLE);
            //Login using Firebase authentication
            auth.signInWithEmailAndPassword(emailInput,passwordInput)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(LoginActivity.this,"You have Logged In Successfully!",Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(i);
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"Sorry, Error Occured: "+ message, Toast.LENGTH_SHORT).show();
                                email.setText("");
                                password.setText("");
                                progressBar.setVisibility(View.GONE);
                                return;
                            }
                        }
                    });
            {

            }

        }

    }

}
