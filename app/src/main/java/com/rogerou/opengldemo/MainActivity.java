package com.rogerou.opengldemo;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ListActivity {
    private final String tests[] =
            {"Triangle", "TriangleColor", "Demo", "Camera", "FrameAnimation", "CameraFrame"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tests));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent intent = new Intent();
        switch (position) {
            case 0:
                intent.setClass(this, TriangleActivity.class);
                break;
            case 1:
                intent.setClass(this, ColorTriangleActivity.class);
                break;

            case 2:
                intent.setClass(this, DemoActivity.class);
                break;
            case 3:
                intent.setClass(this, CameraActivity.class);
                break;
            default:
            case 4:
                intent.setClass(this, FrameAnimationActivity.class);
                break;
            case 5:
                intent.setClass(this, FrameCameraActivity.class);
                break;
        }
        startActivity(intent);
    }
}
