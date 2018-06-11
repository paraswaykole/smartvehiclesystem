package com.project.smartvehicle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText et_name = (EditText) findViewById(R.id.editText1);
        final EditText et_phone = (EditText) findViewById(R.id.editText2);
        final EditText et_model = (EditText) findViewById(R.id.editText3);
        final EditText et_number = (EditText) findViewById(R.id.editText4);
        final EditText et_password = (EditText) findViewById(R.id.editText5);
        final EditText et_city = (EditText) findViewById(R.id.editText6);
        Button register = (Button) findViewById(R.id.btn_register);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                create_account(et_name.getEditableText().toString(),et_phone.getEditableText().toString(),et_model.getEditableText().toString()
                ,et_number.getEditableText().toString(),et_password.getEditableText().toString(),et_city.getEditableText().toString());
            }
        });

    }


    private void create_account(final String name,final String phone,final String model,final String number, final String password,final String city)
    {

        final ProgressDialog pd = ProgressDialog.show(RegisterActivity.this,null,"Creating your account...");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                final boolean success = APIManager.getInstance().register(RegisterActivity.this, name, phone,model,number,password,city);

                pd.dismiss();

                if(success)
                {
                    RegisterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(RegisterActivity.this,ConnectActivity.class);
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
