package com.example.mdpcoursework;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

//incomplete yet
public class HelpDesk extends AppCompatActivity implements HideSystemBars{

    Button back_btn;
    View screen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        screen = getWindow().getDecorView();
        hidesystembars_oncreate(screen);

        setContentView(R.layout.help_page);
        back_btn = findViewById(R.id.back_btn);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        hidesystembars(screen);
    }
}

