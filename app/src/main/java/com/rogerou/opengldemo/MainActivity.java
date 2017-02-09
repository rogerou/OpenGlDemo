package com.rogerou.opengldemo;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ListActivity {
    private final String tests[] =
            {"Epilepsy", "Triangle2d", "TriangleColor", "Texture", "Demo", "Camera"};

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
                intent.setClass(this, EpilepsyActivity.class);
                break;
            case 1:
                intent.setClass(this, Triangle2dActivity.class);
                break;
            case 2:
                intent.setClass(this, TriangleColorActivity.class);
                break;
            case 3:
                intent.setClass(this, TextureActivity.class);

            case 4:
                intent.setClass(this, DemoActivity.class);
                break;
            default:
                intent.setClass(this, CameraActivity.class);
                break;
        }
        startActivity(intent);
    }
}
