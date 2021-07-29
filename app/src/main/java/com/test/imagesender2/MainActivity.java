package com.test.imagesender2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class MainActivity extends AppCompatActivity  {

    final int PICK_IMAGE=100;
    final int PICKFILE_RESULT_CODE=101;
    Context context;
    ServerSocket serverSocket;
    Socket socket;
    final static int PORT=8080;
    EncryptedFile encryptedFile;
    Bitmap choosedImage;
    boolean encrypting=false;
    String encryptingKeyName;
    String decryptingkeyname;
    String destina;
    boolean toggle=true;
    boolean listenFlag=true;
    String imagePath="";
    Bitmap decryptedBitmap;
    String bitmapfilename;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context =MainActivity.this;
        TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
        final Spinner keySizeSpinner=(Spinner)findViewById(R.id.keySizeSpinner);
        final EditText destinationBt=(EditText)findViewById(R.id.destinationbt);
        final Button genKey=(Button)findViewById(R.id.genkey);
        final EditText keyname=(EditText)findViewById(R.id.keyName);
        final Button listenKey=(Button)findViewById(R.id.listenButton);
        final TextView myIpView=(TextView)findViewById(R.id.myIp);
        final Button encryptAndSend=(Button)findViewById(R.id.encryptBt);
        final EditText destinationIp=(EditText)findViewById(R.id.destinationIp);
        final EditText encryptKeyName=(EditText)findViewById(R.id.encryptKeyName);
        final Button decryptBt=(Button)findViewById(R.id.decryptBt);
        final ImageView decryptedImage=(ImageView) findViewById(R.id.imgView2);
        final EditText chooseImageText=(EditText)findViewById(R.id.chooseImage);

        //making clikable edit text to choose image
        chooseImageText.setClickable(true);
        chooseImageText.setFocusable(false);

        //making the tabhost
        tabHost.setup();

        TabHost.TabSpec keyspec=tabHost.newTabSpec("keyTabSpec");
        keyspec.setContent(R.id.tab1);
        keyspec.setIndicator("KEY");
        tabHost.addTab(keyspec);

        TabHost.TabSpec encryptspec=tabHost.newTabSpec("EncryptTabSpec");
        encryptspec.setContent(R.id.tab2);
        encryptspec.setIndicator("ENCRYPT");
        tabHost.addTab(encryptspec);

        TabHost.TabSpec decryptspec=tabHost.newTabSpec("DecryptTabSpec");
        decryptspec.setContent(R.id.tab3);
        decryptspec.setIndicator("DECRYPT");
        tabHost.addTab(decryptspec);

        TabHost.TabSpec listenspec=tabHost.newTabSpec("ListenTabSpec");
        listenspec.setContent(R.id.tab4);
        listenspec.setIndicator("Listen");
        tabHost.addTab(listenspec);



        View.OnClickListener requestKey=new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String keyName=keyname.getText().toString();
                String keySize=keySizeSpinner.getSelectedItem().toString();
                String destination=destinationBt.getText().toString();

                String ip=destination;
                if(keyname.equals("")||destination.equals("")){
                    final AlertDialog.Builder aleBuilder=new AlertDialog.Builder(context);
                    setAlertDialog(aleBuilder,"Error","Destination or Key name is empty");
                }else{
                    //requesting the key using the async task RequestKeyTest
                    RequestKeyTest requestKeyTest=new RequestKeyTest();
                    requestKeyTest.execute(ip,keySize,keyName);
                }


            }
        };
        View.OnClickListener listenReq=new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                myIpView.setText(getIpAddr());

                try {
                    serverSocket = new ServerSocket(PORT);
                    Log.e("AsyncTask", "server socket created");
                    RequestListnerTest listner= new RequestListnerTest();

                    if(toggle){
                        listner.execute(serverSocket);
                        toggle=false;
                        listenFlag=true;
                    }else{
                        listner.cancel(true);
                        toggle=true;
                        listenFlag=false;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        };
        View.OnClickListener sendData=new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                encryptingKeyName=encryptKeyName.getText().toString();

                String destination=destinationIp.getText().toString();
                String data="This is test data";
                destina=destination; //chooseImageText
                if(destination.equals("")||encryptingKeyName.equals("")||choosedImage==null){  //choosedImage==null
                    final AlertDialog.Builder aleBuilder=new AlertDialog.Builder(context);
                    setAlertDialog(aleBuilder,"Error","Destination or Key name is empty or no selected image");
                }else{
                    EncryptImage encryptImage=new EncryptImage();
                    encrypting=true;
                    encryptImage.execute(choosedImage); //choosedImage
                }


            }
        };
        View.OnClickListener decrypt=new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                DecryptImage decryptImage=new DecryptImage();

                EditText decryptKey=(EditText)findViewById(R.id.keyNameForDecryption);
                decryptingkeyname=decryptKey.getText().toString();

                if(decryptingkeyname.equals("")||encryptedFile==null){
                    final AlertDialog.Builder aleBuilder=new AlertDialog.Builder(context);
                    setAlertDialog(aleBuilder,"Error","Key name is empty or no received encrypted files");
                }else{
                    decryptImage.execute(encryptedFile);
                }

            }
        };
        View.OnClickListener chooseImageForEncryption=new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, PICK_IMAGE);
            }
        };
        decryptBt.setOnClickListener(decrypt);
        encryptAndSend.setOnClickListener(sendData);
        listenKey.setOnClickListener(listenReq);
        genKey.setOnClickListener(requestKey);
        //chooseImage.setOnClickListener(chooseImageListner);
        chooseImageText.setOnClickListener(chooseImageForEncryption);

    }
    public String getIpAddr() {
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();

        String ipString = String.format(
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));

        return ipString;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();


            choosedImage=BitmapFactory.decodeFile(picturePath);
            EditText ed=(EditText)findViewById(R.id.chooseImage);
            ed.setText(picturePath);
            imagePath=picturePath;
            bitmapfilename= imagePath.substring(imagePath.lastIndexOf("/") + 1);

        }
        if (requestCode == PICKFILE_RESULT_CODE && resultCode == RESULT_OK && null != data) {
            String FilePath = data.getData().getPath();

        }




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setAlertDialog(AlertDialog.Builder aleBuilder,String title,String message){
        aleBuilder.setTitle(title);
        aleBuilder.setMessage(message);
        aleBuilder.setPositiveButton("Ok",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        AlertDialog alertDialog=aleBuilder.create();
        alertDialog.show();
    }
    public boolean addImageToGallery(String fname,Bitmap bm,String datetime) throws IOException {
        boolean ok=false;
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/req_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        File file = new File(myDir, fname);
        ExifInterface exifInterface=new ExifInterface(file.getPath());
        exifInterface.setAttribute(ExifInterface.TAG_DATETIME,datetime);

        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            ok=true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ok;
    }
    //async task to encrypt the image
    public class EncryptImage extends AsyncTask<Bitmap,String,String>{
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            encrypting=true;
            progressDialog = ProgressDialog.show(MainActivity.this, "Wait", "Encrypting...");


        }
        @Override
        protected String doInBackground(Bitmap... params) {
            Bitmap bitmap=params[0];
            Log.e("imagesender2","before encryption");
            try {
                encryptedFile=new EncryptedFile(bitmap);
                int width=bitmap.getWidth();
                int height=bitmap.getHeight();

                //getting data from the image file


                ExifInterface exifInterface=new ExifInterface(imagePath);
                String datetime="";
                String imageWidth="";
                try{
                    datetime=exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                    this.publishProgress("date from image",datetime);
                }catch(Exception e){
                    Date date = new Date(System.currentTimeMillis());
                    DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
                    datetime = formatter.format(date);
                }



                encryptedFile.encryptedPixels=new EncryptedPixel[width][height];

                //read the encoded public key from a file
                File mydir = context.getDir("keyfiles", Context.MODE_PRIVATE); //Creating an internal dir;
                String hashedkeyName=Hash.SHA1(encryptingKeyName);
                File fileWithinMyDir = new File(mydir, hashedkeyName);
                FileInputStream fis = new FileInputStream(fileWithinMyDir);
                byte[] encodedPublicKey = new byte[(int) fileWithinMyDir.length()];
                fis.read(encodedPublicKey);
                fis.close();

                //generating the public key from retreived encoded public key
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");

                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                        encodedPublicKey);
                PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

                int pixel;
                int A,R,G,B;


                for(int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        // get one pixel color
                        pixel = bitmap.getPixel(x, y);
                        // retrieve color of all channels
                        A = Color.alpha(pixel);
                        R = Color.red(pixel);
                        G = Color.green(pixel);
                        B = Color.blue(pixel);
                        // take conversion up to one single value
                        EncryptedPixel encryptedPixel = new EncryptedPixel();

                        encryptedPixel.data[0]=encryptedFile.encoder.EncryptFromKey(Integer.toString(A),publicKey);
                        encryptedPixel.data[1]=encryptedFile.encoder.EncryptFromKey(Integer.toString(R),publicKey);
                        encryptedPixel.data[2]=encryptedFile.encoder.EncryptFromKey(Integer.toString(G),publicKey);
                        encryptedPixel.data[3]=encryptedFile.encoder.EncryptFromKey(Integer.toString(B),publicKey);
                        encryptedFile.encryptedPixels[x][y] = encryptedPixel;

                    }
                }

                encryptedFile.filename=encryptedFile.encoder.EncryptFromKey(bitmapfilename,publicKey);
                encryptedFile.datetime=encryptedFile.encoder.EncryptFromKey(datetime,publicKey);


                this.publishProgress(destina,encryptingKeyName);
                Log.e("imagesender2","after encryption");

            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            progressDialog.setMessage("finished encrypting");
            encrypting=false;


            progressDialog.dismiss();
        };
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            SendEncryptedData sendEncryptedData=new SendEncryptedData();
            sendEncryptedData.execute(values[0],values[0]);

        }
    }

    //async task to decrypt the image
    public class DecryptImage extends AsyncTask<EncryptedFile,String,String>{
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this, "Wait", "Decrypting...");
        }
        @Override
        protected String doInBackground(EncryptedFile... params) {
            EncryptedFile encryptedFile1=params[0];
            try{
                Bitmap.Config config=encryptedFile1.config;
                int width=encryptedFile1.width;
                int height=encryptedFile1.height;
                Encoder encoder=encryptedFile1.encoder;
//                PrivateKey privateKey=encryptedFile1.encoder.privateKey;
                //read the encoded private key from the file
                File mydir = context.getDir("keyfiles", Context.MODE_PRIVATE); //Creating an internal dir;
                String hashedkeyName=Hash.SHA1(decryptingkeyname);
                File fileWithinMyDir = new File(mydir, hashedkeyName);
                FileInputStream fis = new FileInputStream(fileWithinMyDir);
                byte[] encodedPrivateKey = new byte[(int) fileWithinMyDir.length()];
                fis.read(encodedPrivateKey);
                fis.close();

                //generate the private key from retrived encoded file
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");

                PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                        encodedPrivateKey);
                PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);




                final Bitmap bmOut = Bitmap.createBitmap(width, height, config);

                int pixel;
                int A,R,G,B;
                for(int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        // get one pixel color
                        EncryptedPixel encryptedPixel = encryptedFile.encryptedPixels[x][y];
                        // retrieve color of all channels
                        try {
                            A = Integer.parseInt(encoder.DecryptFromKey(encryptedPixel.data[0],privateKey));
                            R = Integer.parseInt(encoder.DecryptFromKey(encryptedPixel.data[1], privateKey));
                            G = Integer.parseInt(encoder.DecryptFromKey(encryptedPixel.data[2], privateKey));
                            B = Integer.parseInt(encoder.DecryptFromKey(encryptedPixel.data[3], privateKey));
                            bmOut.setPixel(x, y, Color.argb(A, R, G, B));


                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (NoSuchPaddingException e) {
                            e.printStackTrace();
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                        } catch (IllegalBlockSizeException e) {
                            e.printStackTrace();
                        } catch (BadPaddingException e) {
                            e.printStackTrace();
                        }

                        // take conversion up to one single value




                    }
                }
                //decoding the meta date of the image
                /*
                String stringDate=encoder.DecryptFromKey(encryptedFile.datetime,privateKey);
                SimpleDateFormat sdf = new SimpleDateFormat("\"HH:mm:ss:SSS\"");
                Date date = sdf.parse(stringDate);
                System.out.println(date.getTime());
                long dateTime;
                dateTime =date.getTime();
                String filename=encoder.DecryptFromKey(encryptedFile.filename,privateKey);
                addImageToGallery(filename,bmOut,stringDate);
                */
                //addImageToGallery("image.jpg",bmOut,Long.toString(System.currentTimeMillis()));
                decryptedBitmap=bmOut;
                this.publishProgress("Success","Successfully decrypted");
                storeImage(bmOut);

                Log.e("This application","decoded successfully");
            } catch (InvalidKeySpecException e) {
                this.publishProgress("Error","Invalid Key");
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                this.publishProgress("Error","File access error");
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                this.publishProgress("Error","algorithm error");
                e.printStackTrace();
            } catch (IOException e) {
                this.publishProgress("Error","IO access error");
                e.printStackTrace();
            }/* catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }*/

            return null;
        }
        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            progressDialog.dismiss();
        };
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            final AlertDialog.Builder aleBuilder=new AlertDialog.Builder(context);
            setAlertDialog(aleBuilder,values[0],values[1]);
            if(values[0].equals("Success")){
                final ImageView decrpt=(ImageView)findViewById(R.id.imgView2);
                decrpt.setImageBitmap(decryptedBitmap);

            }
        }
    }



    //async task to listen for socket connections
    public class SendEncryptedData extends AsyncTask<String,String,String>{
        ProgressDialog progressDialog;
        String result="no result";
        Socket socket;
        String keyName="";
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Encrypting and Sending Data...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
        @Override
        protected String doInBackground(String... params) {
            String ip=params[0];
            String keyName=params[1];
            keyName=params[1];


            try {
                socket =new Socket(ip,PORT);
                ObjectOutputStream outputStream=new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

                Frame frame =new Frame();
                frame.keyName=keyName;
                frame.message="encrypt_data";

                //write the frame object to the listners
                outputStream.writeObject(frame);

                //read dumy key object
                Key key=(Key)inStream.readObject();
                outputStream.flush();
                outputStream.reset();
                //send the encrypted file to the listner
                outputStream.writeObject(encryptedFile);

                socket.close();

                this.publishProgress("Success","Encrypted file sent successfully");


                //Close the client socket
                Log.e("AsyncTask", result);


            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


            return result;
        }
        @Override
        protected void onPostExecute(final String result)
        {

            progressDialog.dismiss();
        };
        @Override
            protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            final AlertDialog.Builder aleBuilder=new AlertDialog.Builder(context);
            setAlertDialog(aleBuilder,values[0],values[1]);
        }

    }

    public class RequestListnerTest extends AsyncTask<ServerSocket,String,String>{
        ProgressDialog progressDialog;
        String result="no result";
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Listening requests....");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
        @Override
        protected String doInBackground(ServerSocket... params) {
            ServerSocket server=params[0];
            Log.e("myapplication","before sending the object from the server");
            try {

                while(listenFlag){


                    socket=server.accept();
                    ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());

                    Frame frame=(Frame)objectInputStream.readObject();
                    result=frame.keyName;

                   // encryptingKeyName=frame.keyName;
                   // decryptingkeyname=frame.keyName;
                    String message=frame.message;
                    if(message.equals("req_key")){
                        //get data from frame object and create the keypair
                        String keySizeFromClient=frame.keySize;
                        Encoder encoder=new Encoder();
                        KeyPair kp=encoder.genNewKey(keySizeFromClient);
                        //set the values to key object to sent to client
                        Key key=new Key();
                        key.message=frame.keyName;
                        key.publicKey=kp.getPublic();
                        //send the key object to client
                        objectOutputStream.writeObject(key);
                        //get the private key from keypair object
                        PrivateKey pk=kp.getPrivate();
                        //save the private key in a file
                        File mydir = MainActivity.this.getDir("keyfiles", Context.MODE_PRIVATE);//Creating an internal dir;
                        if(!mydir.exists())
                        {
                            mydir.mkdirs();
                        }
                        String hashedkeyName=Hash.SHA1(frame.keyName);
                        File fileWithinMyDir = new File(mydir, hashedkeyName);
                        decryptingkeyname=frame.keyName;

                        //the way to store a private key in a file
                        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                                pk.getEncoded());
                        FileOutputStream fos = new FileOutputStream(fileWithinMyDir);
                        fos.write(pkcs8EncodedKeySpec.getEncoded());
                        fos.close();

                        Log.e("myapplication","object sent from the server");
                        this.publishProgress("Success","public key sent from server. \n Key Name :"+frame.keyName+" \n Key Size :"+frame.keySize);


                        socket.close();
                    }else if(message.equals("encrypt_data")){

                        Key key=new Key();

                        objectOutputStream.writeObject(key);
                        //this.publishProgress("waiting for encrypted message");

                        //read the encrypted object from the client and store that in the memory
                        encryptedFile=(EncryptedFile)objectInputStream.readObject();
                        this.publishProgress("Success","encrypted message received");
                        socket.close();

                    }
                }
            } catch (IOException e) {
                this.publishProgress("Error","IO access error");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return result;
        }
        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            progressDialog.setMessage(result);
            progressDialog.dismiss();
        };
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            final AlertDialog.Builder aleBuilder=new AlertDialog.Builder(context);
            setAlertDialog(aleBuilder,values[0],values[1]);
        }


    }
    public class RequestKeyTest extends AsyncTask<String,String,String>{
        ProgressDialog progressDialog;
        String result="no result";
        Socket socket;
        String keyName1="not successs";
        String keyString="this is the key";
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Requesting key file");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
        @Override
        protected String doInBackground(String... params) {
            String ip=params[0];
            String keySize=params[1];
            String keyName=params[2];

            Log.e("myaplication","before receive the object");
            try {
                socket =new Socket(ip,PORT);
                ObjectOutputStream outputStream=new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

                Frame frame =new Frame();
                frame.keyName=keyName;
                frame.keySize=keySize;
                frame.message="req_key";
                //send the frame object to listner
                outputStream.writeObject(frame);

                //receive the key object from the listner
                Key key = (Key) inStream.readObject();
                keyName1=key.message;
                keyString=key.publicKey.toString();

                //get the public key from received key file
                PublicKey pbk=key.publicKey;
                File mydir = context.getDir("keyfiles", Context.MODE_PRIVATE);//Creating an internal dir;
                if(!mydir.exists())
                {
                    mydir.mkdirs();
                }
                String hashedkeyName=Hash.SHA1(keyName);
                File fileWithinMyDir = new File(mydir, hashedkeyName); //Getting a file within the dir.

                //save the public key in a file
                X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                        pbk.getEncoded());
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(fileWithinMyDir);
                    fos.write(x509EncodedKeySpec.getEncoded());
                    fos.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    this.publishProgress("Error","IO access error");
                    e.printStackTrace();
                }

                socket.close();
                Log.e("myaplication","object received");

                this.publishProgress("Success","Key retrieved and stored ");

            } catch (UnknownHostException e1) {
                this.publishProgress("Error","Destination unreachable ");
                e1.printStackTrace();
            } catch (IOException e1) {
                this.publishProgress("Error","IO access error");
                e1.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                this.publishProgress("Error","Encryption algorithm error");
                e.printStackTrace();
            }

            return result;
        }
        @Override
        protected void onPostExecute(String result)
        {

            super.onPostExecute(result);
            progressDialog.setMessage(keyString);
            progressDialog.dismiss();

        };
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            final AlertDialog.Builder aleBuilder=new AlertDialog.Builder(context);
            setAlertDialog(aleBuilder,values[0],values[1]);
        }
    }
    private  File getOutputMediaFile(){

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");



        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }
    private void storeImage(Bitmap image) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();

        String fname = "img.jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, out);;
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}



class Frame implements Serializable {
    String keyName;
    String keySize;
    String message;
}

class Key implements Serializable {
    PrivateKey privateKey;
    PublicKey publicKey;
    String message;
}

class EncryptedFile implements Serializable{
    int height;
    int width;
    public Bitmap.Config config;
    Encoder encoder;
    //image meta data
    String datetime;
    String imagewidth;
    String filename;
    public EncryptedPixel [][] encryptedPixels;

    //constructor of encrypted file
    public EncryptedFile(Bitmap bitmap) throws InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {
        this.height=bitmap.getHeight();
        this.width=bitmap.getWidth();
        config=bitmap.getConfig();



    }


}
class EncryptedPixel implements Serializable{
    String []data=new String[4];
    public EncryptedPixel() {

    }
    public String[] getData(){
        return data;
    }
}


