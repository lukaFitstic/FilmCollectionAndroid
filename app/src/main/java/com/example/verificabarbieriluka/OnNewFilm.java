package com.example.verificabarbieriluka;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActivityChooserView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class OnNewFilm extends AppCompatActivity {

    DBManager dbManager;
    EditText titleET;
    EditText genereET;
    EditText annoProduzioneET;
    ImageView picIV;
    Bundle photoBundle;
    AlertDialog actionAlertDialog;
    String picturePath = "";
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> requestPermissionLauncer;
    int actionType = 0;
    void saveFilm(){
        String title = titleET.getText().toString();
        String genere = genereET.getText().toString();
        String annoDiProduzione = annoProduzioneET.getText().toString();

        if(title.equals("") || genere.equals("") || annoDiProduzione.equals("")) return;
        boolean editMode = true;

        if(photoBundle == null){
            photoBundle = new Bundle();
            editMode = false;
        }
        photoBundle.putString("title", title);
        photoBundle.putString("genere", genere);
        photoBundle.putString("annoproduzione", annoDiProduzione);
        if(!picturePath.equals("")){
            BitmapDrawable drawable = (BitmapDrawable) picIV.getDrawable();
            Bitmap bitmap = drawable.getBitmap();

            String newImagePath = picturePath;
            if(!newImagePath.equals("")){
                Utility.saveImage(newImagePath, bitmap);
                photoBundle.putString("image", newImagePath);
            }else{
                photoBundle.putString("image", "");
            }
        }
        dbManager.saveFilm(photoBundle, editMode);

        Toast.makeText(this, "Salvataggio Completato", Toast.LENGTH_SHORT).show();

        finish();
    }
    void loadFilm(){
        //controlla che il Bundle non sia vuoto
        if(photoBundle != null){
            //se non lo è va a prendere i dati all suo interno dalla key corrispettiva da noi inseritaA
            titleET.setText(photoBundle.getString("title"));
            genereET.setText(photoBundle.getString("genere"));
            annoProduzioneET.setText(photoBundle.getString("annoproduzione"));

            picturePath = photoBundle.getString("image");
            if(picturePath == null) picturePath = "";
            if(picturePath != null && !picturePath.equals("")){
                File file = new File(picturePath);
                if(file.exists()){
                    Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    picIV.setImageBitmap(myBitmap);
                }else{
                    picIV.setImageDrawable(getDrawable(R.drawable.placeholder_image));
                }
            }
        }
    }
    void deleteImage(){
        //dichiariamo una variabile booleana che controlli se l'eliminazione del path (dell immagine)
        //sia andata a buon fine, se si entra nel if ed imposta a vuoto la stringa del path, ed l'immagine come placeHolderS
        boolean success = Utility.deleteImage(picturePath);
        if(success){
            picturePath = "";
            picIV.setImageDrawable(getDrawable(R.drawable.placeholder_image));
            actionAlertDialog.dismiss();
        }
    }
    void openActionList(){
        actionAlertDialog = null;
        View view = getLayoutInflater().inflate(R.layout.action_list_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle("Azioni");
        builder.setMessage("Seleziona quale delle seguenti azioni");
        Button galleryBtn = view.findViewById(R.id.galleryButton);
        Button cameraBtn = view.findViewById(R.id.fotocameraButton);
        Button deleteBtn = view.findViewById(R.id.deleteButton);
        Button cancelBtn = view.findViewById(R.id.cancelButton);

        if(!picIV.equals("")) deleteBtn.setEnabled(true);
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionType = 1;
                OpenGallery();
            }
        });
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionType = 2;
                CheckPermission();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImage();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionAlertDialog.dismiss();
            }
        });
        actionAlertDialog = builder.create();
        actionAlertDialog.show();

    }
    void CheckPermission(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissionLauncer.launch(android.Manifest.permission.CAMERA);
        }else {
            openCam();
        }
    }
    void openCam(){
        //Creo l'Uri di destinazione, file temp.jpg
        String tempImagePath = getFilesDir().getPath() + "/temp.jpg";
        Uri outputFileUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.verificabarbieriluka.FileProvider", new File(tempImagePath));
        //Create Intent to take a picture and return
        //controll to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        //intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //start the image capture Intent
        activityResultLauncher.launch(intent);
    }
    void initPermissionResultRequest(){
        requestPermissionLauncer = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
                if(o){
                    openCam();
                }
            }
        });
    }

    void OpenGallery(){
        Intent getImageFromGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        activityResultLauncher.launch(getImageFromGalleryIntent);
    }
    void activityForResultCallback(){
        //per riuscire a convalidare questa riga, e far si che riconosca i metodi di Android e ti crei il metodo, la classe ove stai lavorando deve estendere AppCompatActivity
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                actionAlertDialog.dismiss();
                switch (actionType) {
                    case 1:
                        if (o.getResultCode() == Activity.RESULT_OK) {
                            try {
                                final Uri imageUri = o.getData().getData();
                                //chiediamo ad getContenteResolver per aprire il file in ingresso (imageUri)
                                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                                //utilizzimao bitmap, ossia un tipo di file per lavorare sulle immagini
                                Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                                //se bitmap (l'immagine) esiste/è stata trovata
                                if (bitmap != null) {
                                    //se l'immagine è maggiore di queste dimensioni --> controllo fatto per essere sicuri di non lavorare immagini troppo pesanti
                                    if (bitmap.getWidth() > 300 || bitmap.getHeight() > 300) {
                                        float scalingFactor;
                                        if (bitmap.getWidth() >= bitmap.getHeight()) {
                                            scalingFactor = 300f / bitmap.getWidth();
                                        } else {
                                            Matrix fixRotation = new Matrix();
                                            fixRotation.postRotate(90);
                                            scalingFactor = 300f / bitmap.getHeight();
                                        }
                                        //ricrea un immagine scalata, e girata al senso giusto
                                        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scalingFactor), (int) (bitmap.getHeight() * scalingFactor), false);

                                    }
                                    //una volta che ho la bitmap finita, devo creare un immagine reale,
                                    // fino ad ora era solo un immagine logica, per avere quella fisica devo far si che me la comprima in un jpeg
                                    //e gli definisco l'output.
                                    try {
                                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                                        picIV.setImageBitmap(bitmap);
                                        //prendiamo su picture, e gli definiamo un valore.
                                        //picture = getFilesDir().getPath() + "/temp.jpg";
                                        picturePath = Utility.getImagesPath(OnNewFilm.this) + "/" + UUID.randomUUID().toString() + ".jpg";
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }

                        }
                        break;
                    case 2:
                        if (o.getResultCode() == Activity.RESULT_OK) {

                            try {

                                if (o.getData().getData() != null) {

                                    final Uri imageUri = o.getData().getData();
                                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                                    picIV.setImageBitmap(selectedImage);

                                } else {

                                    String tempImagePath = getFilesDir().getPath() + "/temp.jpg";

                                    File imageFile = new File(tempImagePath);

                                    if (imageFile.exists()) {

                                        Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));

                                        if (bitmap.getWidth() > 300 || bitmap.getHeight() > 300) {

                                            float scalingFactor;

                                            if (bitmap.getWidth() >= bitmap.getHeight()) {

                                                scalingFactor = 300f / bitmap.getWidth();

                                            } else {

                                                Matrix fixRotation = new Matrix();
                                                fixRotation.postRotate(90);
                                                scalingFactor = 300f / bitmap.getHeight();
                                            }

                                            bitmap = Bitmap.createScaledBitmap(
                                                    bitmap,
                                                    (int) (bitmap.getWidth() * scalingFactor),
                                                    (int) (bitmap.getHeight() * scalingFactor),
                                                    false
                                            );
                                        }

                                        picIV.setImageBitmap(bitmap);
                                    }

                                    picturePath = Utility.getImagesPath(OnNewFilm.this) + "/" + UUID.randomUUID().toString() + ".jpg";
                                }

                            } catch (IOException e) {

                                e.printStackTrace();
                            }
                        }

                        break;
                }
            }

        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_film_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbManager = new DBManager(this, DBManager.DATABASE_NAME, null, DBManager.DATABASE_VERSION);
        titleET = findViewById(R.id.titleEditText);
        genereET = findViewById(R.id.genereEditText);
        annoProduzioneET = findViewById(R.id.annoProduzioneEditText);
        picIV = findViewById(R.id.imageView);

        activityForResultCallback();
        initPermissionResultRequest();

        picIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActionList();
            }
        });

        if(getIntent().getExtras() != null){
            //Assegna il bundle di dati ricevuto dal Intent alla variabile photoBundle.
            photoBundle = getIntent().getExtras();
            //Chiama una funzione loadPhoto()
            // per caricare e gestire un'immagine o altre informazioni correlate.
            loadFilm();
        }

        Button saveBookBtn = findViewById(R.id.saveButton);

        saveBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFilm();
            }
        });
    }
}


