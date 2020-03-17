package com.example.outbrake;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Popup extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_maps);

        Bundle bundle = getIntent().getExtras();
        TextView name = findViewById(R.id.popup_name_tbe);
        name.setText(bundle.getString("name"));
        TextView address = findViewById(R.id.popup_address_tbe);
        address.setText(bundle.getString("address"));
        TextView number = findViewById(R.id.popup_tele_tbe);
        number.setText(bundle.getString("tele"));
        TextView type = findViewById(R.id.popup_type_tbe);
        type.setText(bundle.getString("type"));
    }
}
