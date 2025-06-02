package com.example.myandroidgame;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class Enemy {
    private Bitmap moveSpriteSheet;
    private Bitmap deathSpriteSheet;
    private Bitmap attackSpriteSheet;
    private Bitmap[] moveFrames;
    private Bitmap[] deathFrames;
    private Bitmap[] attackFrames;
    private int width;
    private int height;
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private float x;
    private float y;
    private final int frameDelay = 60;
    private final int frameDeathDelay = 120;
    private final int attackFrameDelay = 60;
    private static final int TOTAL_FLY_FRAMES = 8;
    private static final int TOTAL_ENEMY2_FRAMES = 8;
    private static final int TOTAL_ENEMY3_FRAMES = 4;
    private static final int TOTAL_DEATH_FRAMES = 4;
    private static final int TOTAL_ATTACK_FRAMES = 8;
    private int deathSprites;
    private int attackSprites;
    private int totalFrames;
    private int currentAttackFrame = 0;
    private long lastAttackFrameTime = 0;
    private boolean isDying = false;
    private boolean isDead = false;
    private boolean isAttacking = false;
    private boolean didAttackHit = false;
    private float enemiesVelocity;
    public float velocityX;
    public float velocityY = 0;
    private static final float scale = 2.5f;
    private final float gravity = 30f;
    private float groundY;
    // Som de ataque dos inimigos
    private final SoundPool soundPool;
    private int attackSoundId;
    private int soundId;
    private boolean isAttackSoundLoaded = false;
    private float soundValue = 0.2f;

    public Enemy(Resources resources, Context context, int spriteId, int screenX, int screenY) {
        // Posicao inicial Se aumenta o y ele diminui
        x = screenX + 10;
        groundY = screenY * 0.3f;
        y = screenY * 0.3f;

        // Audio de ataque dos inimigos
        soundPool = setupSounds();

        if (spriteId == R.drawable.enemy1_flight) {
            y = screenY * 0.05f;
            enemiesVelocity = 15;
            velocityX = 15;
            deathSprites = R.drawable.enemy1_death;
            attackSprites = R.drawable.enemy1_attack;
            totalFrames = TOTAL_FLY_FRAMES;
            soundId = R.raw.enemy1_attack;
        }

        if (spriteId == R.drawable.enemy2_run) {
            velocityX = 20;
            enemiesVelocity = 20;
            deathSprites = R.drawable.enemy2_death;
            attackSprites = R.drawable.enemy2_attack;
            totalFrames = TOTAL_ENEMY2_FRAMES;
            soundId = R.raw.enemy2_attack;
        }

        if (spriteId == R.drawable.enemy3_walk) {
            velocityX = 15;
            enemiesVelocity = 15;
            deathSprites = R.drawable.enemy3_death;
            attackSprites = R.drawable.enemy3_attack;
            totalFrames = TOTAL_ENEMY3_FRAMES;
            soundId = R.raw.enemy3_attack;
        }

        attackSoundId = soundPool.load(context, soundId, 1);
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                if (sampleId == attackSoundId) {
                    isAttackSoundLoaded = true;
                }
            }
        });

        setupEnemyFrames(resources,spriteId, totalFrames);
        setupEnemyDeathFrames(resources, deathSprites, TOTAL_DEATH_FRAMES);
        setupEnemyAttackFrames(resources, attackSprites, TOTAL_ATTACK_FRAMES);
    }

    public void setupEnemyFrames(Resources resources, int spriteId, int totalFrames) {
        moveSpriteSheet = BitmapFactory.decodeResource(resources, spriteId);

        width = moveSpriteSheet.getWidth() / totalFrames;
        height = moveSpriteSheet.getHeight();

        // Corta cada frame da sprite sheet
        moveFrames = new Bitmap[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
            // Espelha horizontalmente
            Matrix matrix = new Matrix();
            matrix.preScale(-1, 1);
            // Corta cada frame da sprite sheet original
            Bitmap frame = Bitmap.createBitmap(moveSpriteSheet, i * width, 0, width, height, matrix, false);

            // Redimensiona o frame se necessário
            int scaleWidth = (int) (width * scale);
            int scaleHeight = (int) (height * scale);
            moveFrames[i] = Bitmap.createScaledBitmap(frame, scaleWidth, scaleHeight, false);
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

    public void setupEnemyAttackFrames(Resources resources, int spriteId, int totalFrames) {
        attackSpriteSheet = BitmapFactory.decodeResource(resources, spriteId);

        attackFrames = new Bitmap[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
            Matrix matrix = new Matrix();
            matrix.preScale(-1, 1);
            Bitmap frame = Bitmap.createBitmap(attackSpriteSheet, i * width, 0, width, height, matrix, false);

            int scaleWidth = (int) (width * scale);
            int scaleHeight = (int) (height * scale);
            attackFrames[i] = Bitmap.createScaledBitmap(frame, scaleWidth, scaleHeight, false);
        }
    }


    public void update() {
        long currentTime = System.currentTimeMillis();

        // Morte
        if (isDying) {
            // Cancela ataque atual
            isAttacking = false;
            currentAttackFrame = 0;

            if (currentTime - lastFrameTime > frameDeathDelay) {
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

            velocityX = 10;
            x -= velocityX;
            y -= velocityY;
            return;
        }

        // Ataque
        if (isAttacking) {
            velocityX = 0;
            if (currentTime - lastAttackFrameTime > attackFrameDelay) {
                lastAttackFrameTime = currentTime;

                if (currentAttackFrame == 5 && isAttackSoundLoaded) {
                    soundPool.play(attackSoundId, soundValue, soundValue, 1, 0, 1f);
                }

                currentAttackFrame++;

                if (currentAttackFrame >= TOTAL_ATTACK_FRAMES) {
                    currentAttackFrame = 0;
                    isAttacking = false;
                    // Assim que chega no ultimo frame de ataque, a velocidade volta para a padrão
                    velocityX = enemiesVelocity;
                }

                // Se completar a animação de ataque, o ataque acertou
                if (currentAttackFrame == TOTAL_ATTACK_FRAMES - 1) {
                    didAttackHit = true;
                }
            }
            return;
        }

        // Andando
        x -= velocityX;
        y -= velocityY;

        if (currentTime - lastFrameTime > frameDelay) {
            currentFrame = (currentFrame + 1) % totalFrames;
            lastFrameTime = currentTime;
        }
    }


    // Para colisões (retorna retângulo do personagem)
    public Rect getBounds() {
        return new Rect((int)x, (int)y, (int)x + width, (int)y + height);
    }

    public void draw(Canvas canvas, Paint paint) {
        if (isDying) {
            canvas.drawBitmap(deathFrames[currentFrame], x, y, paint);
            return;
        }

        if (isAttacking) {
            canvas.drawBitmap(attackFrames[currentAttackFrame], x, y, paint);
            return;
        }

        canvas.drawBitmap(moveFrames[currentFrame], x, y, paint);
    }

    public void attack() {
        if (!isAttacking) {
            isAttacking = true;
            currentAttackFrame = 0;
            lastAttackFrameTime = System.currentTimeMillis();
        }
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public void die() {
        if (!isDying) {
            isDying = true;
            currentFrame = 0;
            lastFrameTime = System.currentTimeMillis();
        }
    }

    private SoundPool setupSounds() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        return new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(5)
                .build();
    }

    public boolean isDead() {
        return this.isDead;
    }

    public float getX() {
        return x;
    }

    public int getWidth() {
        return width;
    }

    public boolean isDidAttackHit() {
        return didAttackHit;
    }
}