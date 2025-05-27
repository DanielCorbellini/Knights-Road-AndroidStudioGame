package com.example.myandroidgame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

public class Enemy {
    private Bitmap flySpriteSheet;
    private Bitmap deathSpriteSheet;
    private Bitmap[] flyFrames;
    private Bitmap[] deathFrames;
    private int width;
    private int height;
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private float x;
    private float y;
    private final int frameDelay = 60;
    private final int frameDeathDelay = 120;
    private static final int TOTAL_FLY_FRAMES = 8;
    private static final int TOTAL_ENEMY2_FRAMES = 8;
    private static final int TOTAL_ENEMY3_FRAMES = 4;
    private static final int TOTAL_DEATH_FRAMES = 4;
    private int deathSprites;
    private int totalFrames;
    private boolean isDying = false;
    private boolean isDead = false;
    public float velocityX;
    public float velocityY = 0;
    private static final float scale = 2.5f;
    private final float gravity = 30f;
    private float groundY;

    public Enemy(Resources resources, int spriteId, int screenX, int screenY) {
        // Posicao inicial Se aumenta o y ele diminui
        x = screenX + 10;
        groundY = screenY * 0.3f;
        y = screenY * 0.3f;

        if (spriteId == R.drawable.enemy1_flight) {
            y = screenY * 0.05f;
            velocityX = 15;
            deathSprites = R.drawable.enemy1_death;
            totalFrames = TOTAL_FLY_FRAMES;
        }

        if (spriteId == R.drawable.enemy2_run) {
            velocityX = 20;
            deathSprites = R.drawable.enemy2_death;
            totalFrames = TOTAL_ENEMY2_FRAMES;
        }

        if (spriteId == R.drawable.enemy3_walk) {
            velocityX = 15;
            deathSprites = R.drawable.enemy3_death;
            totalFrames = TOTAL_ENEMY3_FRAMES;
        }

        setupEnemyFrames(resources,spriteId, totalFrames);
        setupEnemyDeathFrames(resources, deathSprites, TOTAL_DEATH_FRAMES);
    }

    public void setupEnemyFrames(Resources resources, int spriteId, int totalFrames) {
        flySpriteSheet = BitmapFactory.decodeResource(resources, spriteId);

        width = flySpriteSheet.getWidth() / totalFrames;
        height = flySpriteSheet.getHeight();

        // Corta cada frame da sprite sheet
        flyFrames = new Bitmap[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
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

    public void setupEnemyDeathFrames(Resources resources, int spriteId, int totalFrames) {
        deathSpriteSheet = BitmapFactory.decodeResource(resources, spriteId);

        deathFrames = new Bitmap[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
            Matrix matrix = new Matrix();
            matrix.preScale(-1, 1);
            Bitmap frame = Bitmap.createBitmap(deathSpriteSheet, i * width, 0, width, height, matrix, false);

            int scaleWidth = (int) (width * scale);
            int scaleHeight = (int) (height * scale);
            deathFrames[i] = Bitmap.createScaledBitmap(frame, scaleWidth, scaleHeight, false);
        }
    }

    public void update() {
        x -= velocityX;
        y -= velocityY;

        long currentTime = System.currentTimeMillis();

        if (isDying) {
            if (currentTime - lastFrameTime > frameDeathDelay) {
                velocityX = 10;
                if (deathSprites == R.drawable.enemy1_death) {
                    velocityY -= gravity;
                    if (y >= groundY) {
                        y = groundY;
                        velocityY = 0;
                    }
                }
                currentFrame++;
                lastFrameTime = currentTime;
                if (currentFrame >= TOTAL_DEATH_FRAMES) {
                    currentFrame = 0;
                    isDying = false;
                    isDead = true;
                }
            }
        } else {
            if (currentTime - lastFrameTime > frameDelay) {
                currentFrame = (currentFrame + 1) % totalFrames;
                lastFrameTime = currentTime;
            }
        }
    }

    // Para colisões (retorna retângulo do personagem)
    public Rect getBounds() {
        return new Rect((int)x, (int)y, (int)x + width, (int)y + height);
    }

    public void draw(Canvas canvas, Paint paint) {
        if (isDying) {
            canvas.drawBitmap(deathFrames[currentFrame], x, y, paint);
        } else {
            canvas.drawBitmap(flyFrames[currentFrame], x, y, paint);
        }
    }

    public void setVelocity(float velocityX, float velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    public void die() {
        if (!isDying) {
            isDying = true;
            currentFrame = 0;
            lastFrameTime = System.currentTimeMillis();
        }
    }

    public boolean isDead() {
        return this.isDead;
    }
    public void setDying(boolean dying) {
        isDying = dying;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
