package com.example.myandroidgame;

import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameActivity extends AppCompatActivity {
    private GameView gameView;
    MediaPlayer gameMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Obtém o tamanho da tela em x e y
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);

        gameMusic = MediaPlayer.create(this, R.raw.ingame_music);
        gameMusic.setLooping(true);
        gameMusic.setVolume(0.7f, 0.7f);
        gameView = new GameView(this, point.x, point.y);

        setContentView(gameView);
        gameMusic.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameMusic != null && gameMusic.isPlaying()) {
            gameMusic.pause();
        }
        gameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameMusic != null && !gameMusic.isPlaying()) {
            gameMusic.start();
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
}