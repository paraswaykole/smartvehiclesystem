package com.project.smartvehicle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sp = getSharedPreferences(APIManager.SHAREDPREFS_NAME, MODE_PRIVATE);
        int userid = sp.getInt("userid",-1);
        if(userid != -1){
            Intent i = new Intent(MainActivity.this,ConnectActivity.class);
            startActivity(i);
            finish();
        }

        Button btn_register = (Button) findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(i);
            }
        });

        Button btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et_username = (EditText) findViewById(R.id.editText1);
                EditText et_password = (EditText) findViewById(R.id.editText2);

                login(et_username.getEditableText().toString(),et_password.getEditableText().toString());

            }
        });

    }

    private void login(final String username,final String password)
    {

        final ProgressDialog pd = ProgressDialog.show(MainActivity.this,null,"Logging in...");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                final boolean success = APIManager.getInstance().login(MainActivity.this, username,password);

                pd.dismiss();

                if(success)
                {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(MainActivity.this,ConnectActivity.class);
                            startActivity(i);
                            finish();
                        }
                    });
                }

            }
        });
        t.start();
    }
}
