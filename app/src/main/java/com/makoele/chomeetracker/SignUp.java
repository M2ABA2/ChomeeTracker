package com.makoele.chomeetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.ConnectException;

public class SignUp extends AppCompatActivity {

    private EditText inputName,inputEmail, inputPassword,confirmPassword;
    private Button btnSignIn, btnSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseAuth.AuthStateListener authStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        inputName = findViewById(R.id.txtName);
        inputEmail = findViewById(R.id.txtEmail);
        inputPassword = findViewById(R.id.txtPassword);
        confirmPassword= findViewById(R.id.txtConfirmPass);
        progressBar = findViewById(R.id.progressBar);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");


        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(SignUp.this, LoginActivity.class);
                startActivity(i);

            }
        });

        //SignUp button is clicked and authentication is performed before processing info
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                register();            }
        });
    }

    public void register()
    {
        final String email = inputEmail.getText().toString();
        final String password = inputPassword.getText().toString();
        String confirmPass = confirmPassword.getText().toString();
        final String username = inputName.getText().toString();


        if (TextUtils.isEmpty(username) && TextUtils.isEmpty(email) && TextUtils.isEmpty(password)
                && TextUtils.isEmpty(confirmPass)){

            Toast.makeText(SignUp.this,"Please Fill In all Required Fields!",Toast.LENGTH_SHORT).show();


        }

        else{

                    progressBar.setVisibility(View.VISIBLE);
                    //create user
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Toast.makeText(SignUp.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);

                                    if (task.isSuccessful()) {

                                        try {
                                            String user_id = auth.getCurrentUser().getUid();
                                            DatabaseReference current_user_db = databaseReference.child(user_id);
                                            current_user_db.child("name").setValue(username);
                                            current_user_db.child("email").setValue(email);

                                            startActivity(new Intent(SignUp.this, LoginActivity.class));
                                            //finish();
                                            throw task.getException();
                                        } catch(FirebaseAuthWeakPasswordException e) {
                                            inputPassword.setError(getString(R.string.error_weak_password));
                                            inputPassword.requestFocus();
                                        } catch(FirebaseAuthInvalidCredentialsException e) {
                                            inputEmail.setError(getString(R.string.error_invalid_email));
                                            inputEmail.requestFocus();
                                        } catch(FirebaseAuthUserCollisionException e) {
                                            inputEmail.setError(getString(R.string.error_user_exists));
                                            inputEmail.requestFocus();
                                        } catch(Exception e) {
                                            Log.e("Sorry Error!", e.getMessage());
                                        }


                                    } else {
                                        String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                                        switch (errorCode) {

                                            case "ERROR_INVALID_CUSTOM_TOKEN":
                                                Toast.makeText(SignUp.this, "The custom token format is incorrect. Please check the documentation.", Toast.LENGTH_LONG).show();
                                                break;

                                            case "ERROR_CUSTOM_TOKEN_MISMATCH":
                                                Toast.makeText(SignUp.this, "The custom token corresponds to a different audience.", Toast.LENGTH_LONG).show();
                                                break;

                                            case "ERROR_INVALID_CREDENTIAL":
                                                Toast.makeText(SignUp.this, "The supplied auth credential is malformed or has expired.", Toast.LENGTH_LONG).show();
                                                break;


                                            case "ERROR_INVALID_EMAIL":
                                                Toast.makeText(SignUp.this, "The email address is badly formatted.", Toast.LENGTH_LONG).show();
                                                inputEmail.setError("The email address is badly formatted.");
                                                inputEmail.requestFocus();
                                                break;
                                            case "ERROR_WRONG_PASSWORD":
                                                Toast.makeText(SignUp.this, "The password is invalid or the user does not have a password.", Toast.LENGTH_LONG).show();
                                                inputPassword.setError("password is incorrect ");
                                                inputPassword.requestFocus();
                                                inputPassword.setText("");
                                                break;

                                            case "ERROR_USER_MISMATCH":
                                                Toast.makeText(SignUp.this, "The supplied credentials do not correspond to the previously signed in user.", Toast.LENGTH_LONG).show();
                                                break;

                                            case "ERROR_REQUIRES_RECENT_LOGIN":
                                                Toast.makeText(SignUp.this, "This operation is sensitive and requires recent authentication. Log in again before retrying this request.", Toast.LENGTH_LONG).show();
                                                break;

                                            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                                                Toast.makeText(SignUp.this, "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.", Toast.LENGTH_LONG).show();
                                                break;

                                            case "ERROR_EMAIL_ALREADY_IN_USE":
                                                Toast.makeText(SignUp.this, "The email address is already in use by another account.   ", Toast.LENGTH_LONG).show();
                                                inputEmail.setError("The email address is already in use by another account.");
                                                inputEmail.requestFocus();
                                                break;

                                            case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                                                Toast.makeText(SignUp.this, "This credential is already associated with a different user account.", Toast.LENGTH_LONG).show();
                                                break;

                                            case "ERROR_USER_DISABLED":
                                                Toast.makeText(SignUp.this, "The user account has been disabled by an administrator.", Toast.LENGTH_LONG).show();
                                                break;

                                            case "ERROR_USER_TOKEN_EXPIRED":
                                                Toast.makeText(SignUp.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                                                break;

                                            case "ERROR_USER_NOT_FOUND":
                                                Toast.makeText(SignUp.this, "There is no user record corresponding to this identifier. The user may have been deleted.", Toast.LENGTH_LONG).show();
                                                break;

                                            case "ERROR_INVALID_USER_TOKEN":
                                                Toast.makeText(SignUp.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                                                break;

                                            case "ERROR_OPERATION_NOT_ALLOWED":
                                                Toast.makeText(SignUp.this, "This operation is not allowed. You must enable this service in the console.", Toast.LENGTH_LONG).show();
                                                break;

                                            case "ERROR_WEAK_PASSWORD":
                                                Toast.makeText(SignUp.this, "The given password is invalid.", Toast.LENGTH_LONG).show();
                                                inputPassword.setError("The password is invalid it must 6 characters at least");
                                                inputPassword.requestFocus();
                                                break;

                                        }
                                    }
                                }
                            });


        }


    }
    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}
