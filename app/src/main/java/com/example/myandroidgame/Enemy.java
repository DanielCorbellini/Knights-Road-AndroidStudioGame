package com.example.myandroidgame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public class Enemy {
    private Bitmap flySpriteSheet;
    private Bitmap[] flyFrames;
    private Bitmap[] flyFramesFlipped;
    private int width;
    private int height;
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private float x;
    private float y;
    private final int frameDelay = 100;
    private static final int TOTAL_FLY_FRAMES = 8;
    public float velocityX = 2;
    public float velocityY = 0;
    private static final float scale = 2.5f;

    public Enemy(Resources resources, int screenX, int screenY) {
        setupFlyFrames(resources);

        // Posicao inicial Se aumenta o y ele diminui
        x = screenX + 10;
        y = screenY * 0.05f;
    }

    public void setupFlyFrames(Resources resources) {
        flySpriteSheet = BitmapFactory.decodeResource(resources, R.drawable.enemy1_flight);

        width = flySpriteSheet.getWidth() / TOTAL_FLY_FRAMES;
        height = flySpriteSheet.getHeight();

        // Corta cada frame da sprite sheet
        flyFrames = new Bitmap[TOTAL_FLY_FRAMES];
        for (int i = 0; i < TOTAL_FLY_FRAMES; i++) {
            // Espelha horizontalmente
            Matrix matrix = new Matrix();
            matrix.preScale(-1, 1);
            // Corta cada frame da sprite sheet original
            Bitmap frame = Bitmap.createBitmap(flySpriteSheet, i * width, 0, width, height, matrix, false);

            // Redimensiona o frame se necessário
            int scaleWidth = (int) (width * scale);
            int scaleHeight = (int) (height * scale);
            flyFrames[i] = Bitmap.createScaledBitmap(frame, scaleWidth, scaleHeight, false);
        }
    }

    public void update() {
        x -= velocityX;
        y -= velocityY;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime > frameDelay) {
            currentFrame = (currentFrame + 1) % TOTAL_FLY_FRAMES;
            lastFrameTime = currentTime;
        }
    }

    public void draw(Canvas canvas, Paint paint) {
        canvas.drawBitmap(flyFrames[currentFrame], x, y, paint);
    }

    public void setVelocity(float velocityX, float velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }
}
