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
        list.add(0 + "");
        list.add(1 + "");
        list.add(2 + "");
        list.add(4 + "");
        list.add(7 + "");
        list.add(5 + "");
        list.add(6 + "");
        list.add(3 + "");
        list.add(9 + "");
        list.add(10 + "");
        list.add(8 + "");

        scaleView.setScaleValue(list, 0);
        scaleView.setOnScaleChangeListener(new QScaleView.OnScaleChangeListener() {
            @Override
            public void onScaleChange(String scale) {
                Log.e("QTest", "scale:  " + scale);
            }
        });
    }
}
