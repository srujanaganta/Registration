package com.example.srujana.registration;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public AlertDialog.Builder popAlert;
    private Uri mUri ;
    SessionManager session;
    int REQUEST_CAMERA=100;int SELECT_FILE=200;String uripath,visipotourl;
    private ProgressDialog progDialog;
    public static final String UPLOAD_URL = "http://192.168.2.10/upload.php";
    public static final String UPLOAD_KEY = "image";
    public static final String TAG = "MY MESSAGE";

    private int PICK_IMAGE_REQUEST = 1;

    private Button buttonChoose;
    private Button buttonUpload;
    private Button buttonView;

    private ImageView imageView;

    private Bitmap bitmap;

    private Uri filePath;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        session=new SessionManager(MainActivity.this);
        progDialog=new ProgressDialog(MainActivity.this);
        popAlert = new AlertDialog.Builder(MainActivity.this);
        popAlert.setCancelable(true)
                .setNeutralButton("Ok. Got it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });



        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);
        buttonView = (Button) findViewById(R.id.buttonViewImage);

        imageView = (ImageView) findViewById(R.id.imgView);
        mButton = (Button) findViewById(R.id.button);
        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);


        mButton.setOnClickListener(    new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent i = new Intent(MainActivity.this,MainActivity1.class);
                startActivity(i);
                //startActivityForResult(i, REQUEST_CODE_CHEAT);
            }
        });
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }



    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void uploadImage(){
        class UploadImage extends AsyncTask<Bitmap,Void,String>{

            ProgressDialog loading;
            RequestHandler rh = new RequestHandler();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MainActivity.this, "Uploading Image", "Please wait...",true,true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(Bitmap... params) {
                Bitmap bitmap = params[0];
                String uploadImage = getStringImage(bitmap);

                HashMap<String,String> data = new HashMap<>();
                data.put(UPLOAD_KEY, uploadImage);

                String result = rh.sendPostRequest(UPLOAD_URL,data);

                return result;
            }
        }

        UploadImage ui = new UploadImage();
        ui.execute(bitmap);
    }
    private Uri mImageCaptureUri;
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Gallery",
                "Cancel" };

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {


                if (items[item].equals("Take Photo")) {
                    cameraIntent();

                } else if (items[item].equals("Choose from Gallery")) {


                    galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onClick(View v) {
        if (v == buttonChoose) {
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionsResultAction() {

                            @Override
                            public void onGranted() {
                                File nfile = new File(Environment.getExternalStorageDirectory() + "/mprep");
                                if (!nfile.isDirectory()) {
                                    nfile.mkdir();
                                }
                                File nfile1 = new File(Environment.getExternalStorageDirectory() + "/mprep/notes");
                                if (!nfile1.isDirectory()) {
                                    nfile1.mkdir();
                                }
                                SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
                                final String format = s.format(new Date());
                                session.storeVal("notesvisiname", format.toString() + ".jpg");
                                selectImage();
                            }

                            @Override
                            public void onDenied(String permission) {
                                Toast.makeText(MainActivity.this,
                                        "Sorry, we need the Storage Permission to do that",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            Intent kl=new Intent(MainActivity.this,MainActivity1.class);
            startActivity(kl);finish();
        }
        if(v == buttonUpload){
            Map<String, String> params = new LinkedHashMap<String, String>();
            params.put("uploadFile","true");
            new uploadService().execute(params);
        }
    }
    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }
    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE){
                onSelectFromGalleryResult(data);}
            else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
            }

        }
    }
    public String getRealPathFromURI(Uri contentUri) {
        String [] proj      = {MediaStore.Images.Media.DATA};
        Cursor cursor       = managedQuery( contentUri, proj, null, null,null);

        if (cursor == null) return null;

        int column_index    = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(column_index);
    }
    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mImageCaptureUri = data.getData();
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
        if(isKitKat)
            mUri=Uri.parse(ImageFilePath.getPath(MainActivity.this,mImageCaptureUri));
        else
            mUri=Uri.parse(getRealPathFromURI(mImageCaptureUri));
        // visipotourl = Environment.getExternalStorageDirectory().getPath() + "/mprep/notes/";
        visipotourl = Environment.getExternalStorageDirectory().getPath() + "/mprep/notes/" +session.getStrVal("notesvisiname");
        session.storeVal("visipotourl",visipotourl);
        if(isKitKat)
            copyFile(ImageFilePath.getPath(MainActivity.this,mImageCaptureUri),session.getStrVal("notesvisiname") ,visipotourl);
        else
            copyFile(getRealPathFromURI(mImageCaptureUri),session.getStrVal("notesvisiname") ,visipotourl);
        session.storeVal("zoomname",session.getStrVal("notesvisiname"));
        imageView .setVisibility(View.VISIBLE);
        imageView .setImageBitmap(bm);

    }
    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        mImageCaptureUri = data.getData();
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
        if(isKitKat)
            mUri=Uri.parse(ImageFilePath.getPath(MainActivity.this,mImageCaptureUri));
        else
            mUri=Uri.parse(getRealPathFromURI(mImageCaptureUri));
        visipotourl = Environment.getExternalStorageDirectory().getPath() + "/mprep/notes/" +session.getStrVal("notesvisiname");
        session.storeVal("visipotourl",visipotourl);
        File   visifile = new File(visipotourl);
        FileOutputStream fo;
        try {
            visifile.createNewFile();
            fo = new FileOutputStream(visifile);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // new ImageCompressionAsyncTask(true).execute(Environment.getExternalStorageDirectory().getPath() + "/mprep/notes/"+session.getStrVal("notesvisiname"));
        session.storeVal("zoomname",session.getStrVal("notesvisiname"));
        imageView .setVisibility(View.VISIBLE);
        imageView .setImageBitmap(thumbnail);


    }
    private void copyFile(String inputPath, String inputFile, String outputPath){

        InputStream in = null;
        OutputStream out = null;
        try {
            Log.d("params",inputPath+"=="+inputFile+"===="+outputPath);
            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                //  dir.mkdirs();
            }


            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        }  catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
        //new ImageCompressionAsyncTask(true).execute(Environment.getExternalStorageDirectory().getPath() + "/mprep/notes/"+session.getStrVal("notesvisiname"));
    }

    private class uploadService extends AsyncTask<Map, Integer, String>

    {


        @Override
        protected String doInBackground(Map... params) {

            return postData(params[0]);
        }

        @Override
        protected void onPreExecute() {



            super.onPreExecute();
            progDialog = ProgressDialog.show(MainActivity.this, "Please Wait", "Requesting Our Server.......", true);
            progDialog.setProgress(0);
            progDialog.setCancelable(true);


        }

        protected void onPostExecute(String response){


            progDialog.dismiss();
            JSONObject result = null;

            Log.d("webService", "HTTP Request Result: " + response);

            try {

                result = new JSONObject(response);

                String res = result.getString("result");

                //Log.d("HTTP result filesync",response.toString());

                if (res.trim().equals("success")) {

                    popAlert.setTitle("Information");
                    popAlert.setMessage("file uploaded successfully");
                    popAlert.create().show();
                    //Log.d("DB Sync","Updating Sync status:"+ret);
                }
                else
                {
                    popAlert.setTitle("Information");
                    popAlert.setMessage(result.getString("error"));
                    popAlert.create().show();progDialog.dismiss();
                }



            } catch (JSONException e) {
                e.printStackTrace();progDialog.dismiss();
            }


        }
        protected void onProgressUpdate(Integer... progress){
        }

        public String postData(Map data) {

            String response = "{\"result\":\"failed\"}";

            try {

                String destPath =visipotourl;
                //Environment.getExternalStorageDirectory() + "/DCIM/"+String.valueOf(ratePrefs.getString("filename","file"));

                HttpRequest request = HttpRequest.post("http://itsmygrid.com/demo_amrut/landimages/upload.php");
                request.part("filename", session.getStrVal("notesvisiname"));
                request.part("uploadFile", "uploadfile");
                request.part("file",session.getStrVal("notesvisiname"), new File(destPath));
                if (request.ok())
                    System.out.println("Status was updated");
                Log.d("params",session.getStrVal("visiname")+"==="+visipotourl);
                response = request.body();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return response;



        }


    }
}