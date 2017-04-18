package com.evivian.admin.sailance;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import activity.ClubRegistration;
import activity.OpenClubAccount;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = OpenClubAccount.class.getSimpleName();
    Button getOpenClubAccount, getSignUpNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //button
        getOpenClubAccount = (Button)findViewById(R.id.openClubAccount);
        getSignUpNow = (Button)findViewById(R.id.signUpNow);

        //Login button click event
        getOpenClubAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OpenClubAccount.class);
                startActivity(intent);
            }
        });

        getSignUpNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ClubRegistration.class);
                startActivity(intent);
            }
        });
    }
}
