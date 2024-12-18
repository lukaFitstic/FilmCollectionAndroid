package com.example.verificabarbieriluka;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Bundle filmBundle;
    ArrayList<Bundle> filmAL = new ArrayList<>();
    BaseAdapter adapter;
    DBManager dbManager;
    void openFilmDetail(Bundle filmB){
        Intent intent = new Intent(MainActivity.this, OnNewFilm.class);
        if(filmB != null){
            intent.putExtras(filmB);
        }
        startActivity(intent);
    }

    void initAdapter(){
        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return filmAL.size();
            }

            @Override
            public Object getItem(int position) {
                return filmAL.get(position);
            }

            @Override
            public long getItemId(int position) {
                return filmAL.get(position).getInt("id");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){

                    LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

                    convertView = layoutInflater.inflate(R.layout.film_list_layout, parent, false);
                }

                Bundle filmBundle = (Bundle) getItem(position);
                TextView titleTV = convertView.findViewById(R.id.titleTextView);
                TextView genereTV = convertView.findViewById(R.id.genereTextView);
                TextView annoProduzioneTV = convertView.findViewById(R.id.annoProduzioneTextView);
                ImageView photoIV = convertView.findViewById(R.id.picImageView);

                titleTV.setText(filmBundle.getString("title"));
                genereTV.setText(filmBundle.getString("genere"));
                annoProduzioneTV.setText(filmBundle.getString("annoproduzione"));
                String photoPath = filmBundle.getString("image");

                if(photoPath == null) photoPath = "";

                if(!photoPath.equals("")){
                    File file = new File(photoPath);
                    if(file.exists()){
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        photoIV.setImageBitmap(bitmap);
                    }else{
                        photoIV.setImageDrawable(getDrawable(R.drawable.placeholder_image));
                    }
                }else{
                    photoIV.setImageDrawable(getDrawable(R.drawable.placeholder_image));
                }
                return convertView;
            }
        };

    }

    void loadFilms(){
        filmAL.clear();
        filmAL.addAll(dbManager.getFilm());

        adapter.notifyDataSetChanged();
    }

    void openDeleteConfirmation(Bundle filmB){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ATTENZIONE");
        String msq = "";

        //se non è stato selezionato un id particolare intenderà che vorrai eliminare tutti i libri, altrimenti
        if(filmB == null){
            msq = "Seri sicruo di volere eliminare tutti i film?";
        }
        //altrimenti ti chiederà se sei sicuro di voler eliminare l'oggetto da te selezionato
        else{
            msq = "Sei sicuro di volere eliminare l'elelmento selezionato?";
        }
        builder.setMessage(msq);
        //se si, richiama la funzione da noi definita nel DB, che riceve l'id ed esegue l'operazione sul singolo oggetto
        //o su tutti, in base a se l'id specifico è stato passato o meno
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbManager.deleteFilm(filmB);
                loadFilms();
            }
        });
        //altrimenti sta fermo
        builder.setNegativeButton("No", null);

        builder.create().show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbManager = new DBManager(this, DBManager.DATABASE_NAME, null, DBManager.DATABASE_VERSION);

        initAdapter();

        ListView filmLV = findViewById(R.id.filmListView);
        filmLV.setAdapter(adapter);

        filmLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle film = filmAL.get(position);
                openFilmDetail(film);
            }
        });
        filmLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                openDeleteConfirmation(filmAL.get(position));
                return true;
            }
        });

        ImageView addIV = findViewById(R.id.addImageView);
        addIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilmDetail(null);
            }
        });

        ImageView deleteAllIV = findViewById(R.id.deleteImageView);
        deleteAllIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDeleteConfirmation(null);
            }
        });
    }
    protected void onStart(){
        super.onStart();
        loadFilms();
    }
}