package luxun.waveloadingview;

import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;
import luxun.waterwaveview.CircleWaterWaveView;


public class MainActivity extends AppCompatActivity {


    @InjectView(R.id.waveview)
    CircleWaterWaveView waveview;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        random = new Random();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i <= 90) {
                    waveview.setProgress(i);
                    int[] color = new int[3];
                    for (int j = 0; j < color.length; j++) {
                        int r = random.nextInt(256);
                        int g = random.nextInt(256);
                        int b = random.nextInt(256);
                        color[j] = Color.rgb(r, g, b);
                    }
                    waveview.setColor(color[0], color[1], color[2]);
                    i = i + 10;
                    SystemClock.sleep(2000);
                }
            }
        }).start();
    }

}
