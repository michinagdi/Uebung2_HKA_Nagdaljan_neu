package de.augsburgermichi.hka.uebung2_hka_nagdaljan;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private Button button_map;
    private CheckBox opnvCheck;
    private static boolean opnvBoolean;
    private static boolean nextbikeBoolean;
    private static int laufenValue;
    private CheckBox nextbikeCheck;
    private SeekBar laufen;
    private Button saveButton;




    private String[] listOfRandomWords = new String[6];

    /**
     * Getter Methode zum Übertragen von Variablen an andere Klassen bzw. Activities
     */
    public static boolean isOpnvBoolean() {
        return opnvBoolean;
    }

    /**
     * Getter Methode zum Übertragen von Variablen an andere Klassen bzw. Activities
     */
    public static boolean isNextbikeBoolean() {
        return nextbikeBoolean;
    }

    /**
     * Getter Methode zum Übertragen von Variablen an andere Klassen bzw. Activities
     */
    public static int getLaufenValue() {
        return laufenValue;
    }


    /**
     * Diese Methode wird aufgerufen, wenn die MainActivity erstellt wird.
     * Hier werden Layout-Ressourcen initialisiert, Views referenziert
     * und Click-Listener für Buttons eingerichtet. Außerdem wird eine Log-Nachricht ausgegeben.
     * Der Button {@link MainActivity#button_map} startet die MapActivity beim Klicken.
     * Der Button {@link MainActivity#saveButton} liest den Zustand von Checkboxen und den Fortschritt
     * einer SeekBar aus und speichert die Werte in entsprechenden Variablen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "Hello World!");

        button_map = this.findViewById(R.id.btn_map);
        opnvCheck = this.findViewById(R.id.opnvCheck);
        nextbikeCheck = this.findViewById(R.id.nextbikeCheck);
        laufen = this.findViewById(R.id.seekBarLaufen);
        saveButton = this.findViewById(R.id.buttonSave);

        button_map.setOnClickListener(view -> {
            Intent activity_map = new Intent(this, MapActivity.class);
            this.startActivity(activity_map);
        });

        saveButton.setOnClickListener(view -> {
            opnvBoolean = opnvCheck.isChecked();
            nextbikeBoolean = nextbikeCheck.isChecked();
            laufenValue = laufen.getProgress();
        });



    }

}