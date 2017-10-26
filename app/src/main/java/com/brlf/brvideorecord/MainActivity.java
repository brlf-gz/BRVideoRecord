package com.brlf.brvideorecord;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private static final int REQUEST_RECORD = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button_start = (Button) findViewById(R.id.button_start);
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, VideoRecordActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("minTime", 3000);
                bundle.putInt("maxTime", 600000);
                bundle.putInt("videoRate", 1000);
                intent.putExtras(bundle);
                MainActivity.this.startActivityForResult(intent, REQUEST_RECORD);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_RECORD ) {
            if(resultCode == RESULT_OK) {

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(new File(data.getStringExtra("video")));
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
                Toast.makeText(this,
                        "视频路径：" + data.getStringExtra("video") + '\n'
                        + "时间：" + data.getStringExtra("length") + "秒" + '\n',
                        Toast.LENGTH_LONG).show();
            }
            else if(resultCode == VideoRecordActivity.RESULT_ERROR_CODE) {
                Toast.makeText(this, data.getStringExtra("message"), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
