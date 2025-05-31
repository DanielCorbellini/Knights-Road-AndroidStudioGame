package com.example.myandroidgame;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.view.MotionEvent;

public class Player {
    private Bitmap runSpriteSheet;
    private Bitmap attackSpriteSheet;
    private Bitmap jumpSpriteSheet;
    private Bitmap[] attackFrames;
    private Bitmap[] runFrames;
    private Bitmap[] jumpFrames;

    // Posicao do personagem
    private float x;
    private float y;

    // Tamanho de cada frame
    private int width;
    private int height;

    // Controle da animação
    private int currentFrame = 0;
    private long lastFrameTime = 0;

    // Ajusta o tempo entre cada animação
    private static final int frameDelay = 70;

    // Quantidade de frames que a sprite tem
    private static final int TOTAL_RUN_FRAMES = 10;
    private static final int TOTAL_ATTACK_FRAMES = 4;
    private static final int TOTAL_JUMP_FRAMES = 2;

    // Velocidade do personagem
    public float velocityX = 0;
    public float velocityY = 0;

    // Tamanho da escala do personagem
    private static final float scale = 2.5f;

    // Controla se o player está atacando
    private boolean isAttacking = false;
    // Controla se o player está pulando
    private boolean isJumping = false;
    // Lógica de gravidade
    private final float gravity = 8f;
    private final float jumpStrength = -65f;
    private float groundY;

    // Para captar som dos passos
    private final SoundPool soundPool;
    private final int stepSoundId;
    private final int jumpSoundId;
    private int attackSoundId;

    private boolean isStepSoundLoaded = false;
    private boolean isJumpSoundLoaded = false;
    private boolean isAttackSoundLoaded = false;
    private boolean hasPlayedJumpSound = false;
    private boolean hasPlayedAttackSound = false;

    public Player(Resources resources, Context context, int screenX, int screenY) {
        // Carrega a sprite sheet
        setupRunFrames(resources);
        setupAttackFrames(resources);
        setupJumpFrames(resources);

        // Posição inicial do personagem
        x = screenX * 0.02f;
        groundY = screenY * 0.45f;
        y = screenY * groundY;

        // Audio do personagem
        soundPool = setupSounds();
        stepSoundId = soundPool.load(context, R.raw.player_run, 1);
        jumpSoundId = soundPool.load(context, R.raw.player_jump, 1);
        attackSoundId = soundPool.load(context, R.raw.player_attack, 1);

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                if (sampleId == stepSoundId) {
                    isStepSoundLoaded = true;
                } else if (sampleId == jumpSoundId) {
                    isJumpSoundLoaded = true;
                } else if (sampleId == attackSoundId) {
                    isAttackSoundLoaded = true;
                }
            }
        });
    }

    // Controla as animações do personagem
    public void update() {
        x += velocityX;
        y += velocityY;
        velocityY += gravity;

        if (y >= groundY) {
            y = groundY;
            if (velocityY > 0) {
                isJumping = false;
                velocityY = 0;
            }
        }

        long currentTime = System.currentTimeMillis();
        // Atualiza animação
        if (isAttacking) {
            currentFrame++;
            if (!hasPlayedAttackSound && isAttackSoundLoaded) {
                soundPool.play(attackSoundId, 1, 1, 1, 0, 1f);
                hasPlayedAttackSound = true;
            }

            if (currentFrame >= TOTAL_ATTACK_FRAMES) {
                currentFrame = 0;
                isAttacking = false;
            }
        } else if (isJumping) {
            currentFrame++;
            if (!hasPlayedJumpSound && isJumpSoundLoaded) {
                soundPool.play(jumpSoundId, 1, 1, 1, 0, 1f);
                hasPlayedJumpSound = true;
            }
            if (currentFrame >= TOTAL_JUMP_FRAMES) {
                currentFrame = 0;
            }
        } else {
            if (currentTime - lastFrameTime > frameDelay) {
                currentFrame = (currentFrame + 1) % TOTAL_RUN_FRAMES;
                lastFrameTime = currentTime;
                if (isStepSoundLoaded && (currentFrame == 2 || currentFrame == 7)) {
                    soundPool.play(stepSoundId, 1, 1, 1, 0, 1f);
                }
            }
        }
    }

    // Desenha o frame atual da animação
    public void draw(Canvas canvas, Paint paint) {
        if (isAttacking) {
            canvas.drawBitmap(attackFrames[currentFrame], x, y, paint);
        } else if (isJumping) {
            // Ajeitar, pois está feio
            if (velocityY > 0) {
                canvas.drawBitmap(jumpFrames[1], x, y, paint);
            } else {
                canvas.drawBitmap(jumpFrames[0], x, y, paint);
            }
        } else {
            canvas.drawBitmap(runFrames[currentFrame], x, y, paint);
        }
    }

    // Métodos úteis para controle
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setVelocity(float velocityX, float velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    // Para colisões (retorna retângulo do personagem)
    public Rect getBounds() {
        return new Rect((int) x, (int) y, (int) x + width, (int) y + height);
    }

    // Carrega as sprites de corrida e ataque
    public void setupRunFrames(Resources resources) {
        runSpriteSheet = BitmapFactory.decodeResource(resources, R.drawable._run);

        width = runSpriteSheet.getWidth() / TOTAL_RUN_FRAMES;
        height = runSpriteSheet.getHeight();

        // Corta cada frame da sprite sheet
        runFrames = new Bitmap[TOTAL_RUN_FRAMES];
        for (int i = 0; i < TOTAL_RUN_FRAMES; i++) {
            // Corta cada frame da sprite sheet original
            Bitmap frame = Bitmap.createBitmap(runSpriteSheet, i * width, 0, width, height);

            // Redimensiona o frame se necessário
            int scaleWidth = (int) (width * scale);
            int scaleHeight = (int) (height * scale);
            runFrames[i] = Bitmap.createScaledBitmap(frame, scaleWidth, scaleHeight, false);
        }
    }

    public void setupAttackFrames(Resources resources) {
        attackSpriteSheet = BitmapFactory.decodeResource(resources, R.drawable._attack);

        attackFrames = new Bitmap[TOTAL_ATTACK_FRAMES];
        for (int i = 0; i < TOTAL_ATTACK_FRAMES; i++) {
            Bitmap frame = Bitmap.createBitmap(attackSpriteSheet, i * width, 0, width, height);

            int scaleWidth = (int) (width * scale);
            int scaleHeight = (int) (height * scale);

            attackFrames[i] = Bitmap.createScaledBitmap(frame, scaleWidth, scaleHeight, false);
        }
    }

    private void setupJumpFrames(Resources resources) {
        jumpSpriteSheet = BitmapFactory.decodeResource(resources, R.drawable._jump_fall_in_between);
        jumpFrames = new Bitmap[TOTAL_JUMP_FRAMES];

        for (int i = 0; i < TOTAL_JUMP_FRAMES; i++) {
            Bitmap frame = Bitmap.createBitmap(jumpSpriteSheet, i * width, 0, width, height);
            int scaleWidth = (int) (width * scale);
            int scaleHeight = (int) (height * scale);

            jumpFrames[i] = Bitmap.createScaledBitmap(frame, scaleWidth, scaleHeight, false);
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

    public void attack() {
        if (!isAttacking) {
            isAttacking = true;
            currentFrame = 0;
            lastFrameTime = System.currentTimeMillis();
            hasPlayedAttackSound = false;
        }
    }

    public void jump() {
        if (!isJumping) {
            isJumping = true;
            velocityY = jumpStrength;
            currentFrame = 0;
            lastFrameTime = System.currentTimeMillis();
            hasPlayedJumpSound = false;
        }
    }

    public boolean isAttacking() {
        return this.isAttacking;
    }

    public boolean isJumping() {
        return this.isJumping;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}