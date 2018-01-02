package kingkong.facerecognize.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import kingkong.facerecognize.app.ui.DemoActivity;
import kingkong.facerecognize.app.ui.FaceRecognizeActivity;
import kingkong.facerecognize.app.ui.VoiceprintRecognizeActivity;

/**
 * Created by KingKong-HE on 2017/12/26.
 *
 * @author KingKong-HE
 * @Time 2017/12/26
 * @Email 709872217@QQ.COM
 */
public class MainActivity extends AppCompatActivity{


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btu01).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FaceRecognizeActivity.class));
            }
        });

        findViewById(R.id.btu02).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, VoiceprintRecognizeActivity.class));
            }
        });

        findViewById(R.id.btu03).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DemoActivity.class));
            }
        });

    }
}
