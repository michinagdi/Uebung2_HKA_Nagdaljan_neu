package de.augsburgermichi.hka.uebung2_hka_nagdaljan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button button_click;
    private Button button_weiter;
    private Button button_map;

    private static TextView textViewMiddle;
    private String[] listOfRandomWords = new String[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "Hello World!");

        button_click = this.findViewById(R.id.btn_click);
        button_weiter = this.findViewById(R.id.btn_next);
        button_map = this.findViewById(R.id.btn_map);

        textViewMiddle = this.findViewById(R.id.txtMessage);

        listOfRandomWords[0] = "Heute ist ein schöner Tag!";
        listOfRandomWords[1] = "1+1=3";
        listOfRandomWords[2] = "Tomaten sind lecker";
        listOfRandomWords[3] = "Augsburg ist die beste Stadt";
        listOfRandomWords[4] = "Ich mag Züge";
        listOfRandomWords[5] = "Miau :3";

        button_click.setOnClickListener(view -> {
            Random r = new Random();
            textViewMiddle.setText(listOfRandomWords[r.nextInt(6)]);
        });

        button_weiter.setOnClickListener(view -> {
            Intent activity_weiter = new Intent(this, Activity_Weiter.class);
            this.startActivity(activity_weiter);
        });

        button_map.setOnClickListener(view -> {
            Intent activity_map = new Intent(this, MapActivity.class);
            this.startActivity(activity_map);
        });

    }

    public static TextView getTextViewMiddle() {
        return textViewMiddle;
    }
}