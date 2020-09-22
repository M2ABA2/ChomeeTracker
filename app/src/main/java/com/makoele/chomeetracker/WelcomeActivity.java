package com.makoele.chomeetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.makoele.chomeetracker.Tracking.TrackingActivity;

import org.json.JSONObject;

public class WelcomeActivity extends AppCompatActivity {

    LoginButton facebookLogin;
    Button login;
    Button register;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_welcome);


        login = (Button)findViewById(R.id.btnLogin);
        register = (Button)findViewById(R.id.btnRegister);
        facebookLogin = (LoginButton) findViewById(R.id.btnFBLogin);

        //Login Button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

        //Facebook Login
        facebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
            }
        });

        //Register Button
       register.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               Intent i = new Intent(WelcomeActivity.this, SignUp.class);
               startActivity(i);
           }
       });

    }

        private void setFacebookLogin(){
            CallbackManager  callbackManager = CallbackManager.Factory.create();
         facebookLogin.setReadPermissions("email", "public_profile");
         facebookLogin.registerCallback(callbackManager , new FacebookCallback<LoginResult>() {
             @Override
             public void onSuccess(LoginResult loginResult) {
                 final AccessToken accessToken = loginResult.getAccessToken();
                 GraphRequestAsyncTask request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                     @Override
                     public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                         LoginManager.getInstance().logOut();
                         String username = (user.optString("name"));
                     }
                 }).executeAsync();


                 Toast.makeText(getApplicationContext(), "Login Success with facebook", Toast.LENGTH_SHORT).show();
                 Intent i = new Intent(getApplicationContext(),MainActivity.class);
                 startActivity(i);
             }

             @Override
             public void onCancel() {

             }

             @Override
             public void onError(FacebookException error) {
                 Log.e("Facebook Login Error",error.toString());
             }
         });
     }
}
