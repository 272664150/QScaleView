package com.example.qscaleview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.qscaleview.view.QScaleView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        QScaleView scaleView = findViewById(R.id.select_scale_widget);

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            list.add(i + "");
        }
        scaleView.setScaleValue(list, 0);
        scaleView.setOnScaleChangeListener(new QScaleView.OnScaleChangeListener() {
            @Override
            public void onScaleChange(String scale) {
                Log.e("QTest", "scale:  " + scale);
            }
        });
    }
}
