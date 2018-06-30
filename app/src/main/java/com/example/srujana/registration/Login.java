package com.example.srujana.registration;

/**
 * Created by srujana on 11/6/18.
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    AlertDialog.Builder popAlert1;ProgressDialog progDialog;
    Button sub;EditText usr,pass;SessionManager session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        session = new SessionManager(Login.this);
        sub = (Button) findViewById(R.id.nxt);
        usr = (EditText) findViewById(R.id.mbl);
        pass = (EditText) findViewById(R.id.pwd);
        progDialog = new ProgressDialog(Login.this);
        popAlert1 = new AlertDialog.Builder(Login.this);
        popAlert1.setCancelable(true).setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });
        sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usr.getText().toString().length() == 0) {
                    popAlert1.setTitle("Opps error found...!");
                    popAlert1.setMessage("Username cannot be empty...!");
                    popAlert1.create().show();
                } else if (pass.getText().toString().length() == 0) {
                    popAlert1.setTitle("Opps error found...!");
                    popAlert1.setMessage("Password cannot be empty...!");
                    popAlert1.create().show();
                } else if (!session.haveNetworkConnection()) {
                    popAlert1.setTitle("Opps error found...!");
                    popAlert1.setMessage("Your are not connected to an active internet connection...!");
                    popAlert1.create().show();
                } else {
                    Map<String, String> params = new LinkedHashMap<String, String>();

                    params.put("login", "1");
                    params.put("username", usr.getText().toString());
                    params.put("password", pass.getText().toString());

                    Log.d("params", params.toString());
                    new webService().execute(params);
                }
            }
        });


        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(Login.this,
                    new PermissionsResultAction() {

                        @Override
                        public void onGranted() {

                        }


                        @Override
                        public void onDenied(String permission) {
                            Toast.makeText(Login.this,
                                    "Sorry, we need the Storage Permission to do that",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
        }
    }


    private class webService extends AsyncTask<Map, Integer, String>

    {


        @Override
        protected String doInBackground(Map... params) {

            return postData(params[0]);
        }

        @Override
        protected void onPreExecute() {
            progDialog.setTitle("Connecting to server..");
            progDialog.setMessage("Please wait while we authenticate..!!");
            progDialog.show();
            super.onPreExecute();


        }

        protected void onPostExecute(String response){


            progDialog.dismiss();
            JSONObject result = null;

            Log.d("webService", "HTTP Request Result: " + response);

            try {
                result = new JSONObject(response);
                String res = result.getString("result");

                if(res.trim().equals("success")){

                    session.storeVal("username",usr.getText().toString());
                    Intent jkl=new Intent(Login.this,MainActivity.class);
                    startActivity(jkl);finish();

                }
                else{
                    Toast toast = Toast.makeText(Login.this,result.getString("error"),Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    progDialog.dismiss();

                }


            } catch (JSONException e) {
                Log.d("error",e.toString());
                Log.d("error",response.toString());
                e.printStackTrace();progDialog.dismiss();
            }


        }
        protected void onProgressUpdate(Integer... progress){


        }

        public String postData(Map data) {

            String response = "{\"result\":\"failed\"}";

            try {

                response = HttpRequest.post("http://182.18.180.141/hospitalplus/mobile_api/mobile.php").form(data).body();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Log.d("error",response.toString());
            return response;



        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i("vals", "Activity-onRequestPermissionsResult() PermissionsManager.notifyPermissionsChange()");
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }
    @Override
    public void onBackPressed() {
        finish();
        // new android.support.v7.app.AlertDialog.Builder(this) .setIcon(android.R.drawable.ic_dialog_alert) .setTitle("Exit!") .setMessage("Are you sure you want to Exit?") .setPositiveButton("Yes", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { finish(); } }) .setNegativeButton("No", null) .show();
    }
}