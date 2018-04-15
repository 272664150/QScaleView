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

        List<String> list = new ArrayList<>();
        list.add(0 + "");
        list.add(1 + "");
        list.add(6 + "");
        list.add(3 + "");
        list.add(9 + "");
        list.add(2 + "");
        list.add(4 + "");
        list.add(7 + "");
        list.add(5 + "");
        list.add(10 + "");
        list.add(8 + "");

        QScaleView scaleView1 = findViewById(R.id.select_scale_widget1);
        scaleView1.setScaleInfo(list, 0);
        scaleView1.setOnScaleChangeListener(new QScaleView.OnScaleChangeListener() {
            @Override
            public void onScaleChange(String scale, int position) {
                Log.e("QTest", "view1  ->  scale:  " + scale + "  position:  " + position);
            }
        });

        QScaleView scaleView2 = findViewById(R.id.select_scale_widget2);
        scaleView2.setScaleInfo(list, 0);
        scaleView2.setOnScaleChangeListener(new QScaleView.OnScaleChangeListener() {
            @Override
            public void onScaleChange(String scale, int position) {
                Log.e("QTest", "view2  ->  scale:  " + scale + "  position:  " + position);
            }
        });

        QScaleView scaleView3 = findViewById(R.id.select_scale_widget3);
        scaleView3.setScaleInfo(list, 0);
        scaleView3.setOnScaleChangeListener(new QScaleView.OnScaleChangeListener() {
            @Override
            public void onScaleChange(String scale, int position) {
                Log.e("QTest", "view3  ->  scale:  " + scale + "  position:  " + position);
            }
        });

        QScaleView scaleView4 = findViewById(R.id.select_scale_widget4);
        scaleView4.setScaleInfo(list, 0);
        scaleView4.setOnScaleChangeListener(new QScaleView.OnScaleChangeListener() {
            @Override
            public void onScaleChange(String scale, int position) {
                Log.e("QTest", "view4  ->  scale:  " + scale + "  position:  " + position);
            }
        });

        QScaleView scaleView5 = findViewById(R.id.select_scale_widget5);
        scaleView5.setScaleInfo(list, 0);
        scaleView5.setOnScaleChangeListener(new QScaleView.OnScaleChangeListener() {
            @Override
            public void onScaleChange(String scale, int position) {
                Log.e("QTest", "view5  ->  scale:  " + scale + "  position:  " + position);
            }
        });
    }
}
