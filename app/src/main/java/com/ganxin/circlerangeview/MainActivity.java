package com.ganxin.circlerangeview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private CircleRangeView circleRangeView;

    private String [] valueArray;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circleRangeView= (CircleRangeView) findViewById(R.id.circleRangeView);
        circleRangeView.setOnClickListener(this);

        valueArray=getResources().getStringArray(R.array.circlerangeview_values);
        random=new Random();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.circleRangeView:

                List<String> extras =new ArrayList<>();
                extras.add("收缩压：116");
                extras.add("舒张压：85");

                int i=random.nextInt(valueArray.length);
                circleRangeView.setValueWithAnim(valueArray[i],extras);

                break;
        }
    }
}
