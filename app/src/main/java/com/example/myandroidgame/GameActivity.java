package com.example.myandroidgame;

import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameActivity extends AppCompatActivity implements SensorEventListener {
    private GameView gameView;
    MediaPlayer gameMusic;

    // Acelerômetro
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float JUMP_THRESHOLD = -4f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Colocar em tela cheia e sem desligar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Obtém o tamanho da tela em x e y
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        // Música de fundo durante a gameplay
        gameMusic = MediaPlayer.create(this, R.raw.ingame_music);
        gameMusic.setLooping(true);
        gameMusic.setVolume(0.7f, 0.7f);

        gameView = new GameView(this, point.x, point.y);
        setContentView(gameView);
        gameMusic.start();

        // Configura acelerômetro
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameMusic != null && gameMusic.isPlaying()) {
            gameMusic.pause();
        }
        sensorManager.unregisterListener(this);
        gameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameMusic != null && !gameMusic.isPlaying()) {
            gameMusic.start();
        }
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        gameView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameMusic != null) {
            gameMusic.release();
            gameMusic = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float y = event.values[1]; // Eixo vertical (em retrato)
        if (y < JUMP_THRESHOLD && gameView != null) {
            gameView.jumpPlayer(); // delega o pulo para GameView
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Não utilizado
    }
}