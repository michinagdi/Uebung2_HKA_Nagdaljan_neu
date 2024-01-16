package de.augsburgermichi.hka.uebung2_hka_nagdaljan;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Activity_Weiter extends AppCompatActivity {

    private TextView textViewWeiter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weiter);

        textViewWeiter = this.findViewById(R.id.txtMessageWeiter);
    }


}