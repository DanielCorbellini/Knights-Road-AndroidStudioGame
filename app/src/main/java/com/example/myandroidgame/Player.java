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
    private Bitmap[] attackFrames;
    private Bitmap[] runFrames;
    private Bitmap[] jumpFrames;
    private Bitmap [] deathFrames;
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
    private static final int TOTAL_DEATH_FRAMES = 10;

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

    // Variáveis que controlam o som
    private final SoundPool soundPool;
    private final int stepSoundId;
    private final int jumpSoundId;
    private int attackSoundId;
    private boolean isStepSoundLoaded = false;
    private boolean isJumpSoundLoaded = false;
    private boolean isAttackSoundLoaded = false;
    private float soundValue = 0.2f;
    private boolean isDying = false;
    private boolean isDead = false;
    public Player(Resources resources, Context context, int screenX, int screenY) {
        // Carrega a sprite sheet
        setupAnimations(resources);

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
            if (currentFrame >= TOTAL_ATTACK_FRAMES) {
                currentFrame = 0;
                isAttacking = false;
            }
            return;
        }

        if (isJumping) {
            currentFrame++;
            if (currentFrame >= TOTAL_JUMP_FRAMES) {
                currentFrame = 0;
            }
            return;
        }

        if (isDying) {
            currentFrame++;
            if(currentFrame >= TOTAL_DEATH_FRAMES) {
                currentFrame = 0;
                isDead = true;
                isDying = false;
            }
            return;
        }

        if (!isDead) {
            if (currentTime - lastFrameTime > frameDelay) {
                currentFrame = (currentFrame + 1) % TOTAL_RUN_FRAMES;
                lastFrameTime = currentTime;
                if (isStepSoundLoaded && (currentFrame == 2 || currentFrame == 7)) {
                    soundPool.play(stepSoundId, soundValue, soundValue, 1, 0, 1f);
                }
            }
        }
    }

    // Desenha o frame atual da animação
    public void draw(Canvas canvas, Paint paint) {
        if (isAttacking) {
            canvas.drawBitmap(attackFrames[currentFrame], x, y, paint);
            return;
        }

        if (isJumping) {
            if (velocityY > 0) {
                canvas.drawBitmap(jumpFrames[1], x, y, paint);
            } else {
                canvas.drawBitmap(jumpFrames[0], x, y, paint);
            }
            return;
        }

        if (isDying) {
            canvas.drawBitmap(deathFrames[currentFrame], x, y, paint);
            return;
        }

        if (isDead) {
            canvas.drawBitmap(deathFrames[deathFrames.length -1], x, y, paint);
            return;
        }

        canvas.drawBitmap(runFrames[currentFrame], x, y, paint);
    }

    // Para colisões (retorna retângulo do personagem)
    public Rect getBounds() {
        return new Rect((int) x, (int) y, (int) x + width, (int) y + height);
    }

    // Carrega as sprites das animações
    private Bitmap[] setupFrames(Resources resources, int totalFrames, int id) {
       Bitmap spritesheet = BitmapFactory.decodeResource(resources, id);

        width = spritesheet.getWidth() / totalFrames;
        height = spritesheet.getHeight();

        // Corta cada frame da sprite sheet
        Bitmap[] frames = new Bitmap[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
            // Corta cada frame da sprite sheet original
            Bitmap frame = Bitmap.createBitmap(spritesheet, i * width, 0, width, height);

            // Redimensiona o frame se necessário
            int scaleWidth = (int) (width * scale);
            int scaleHeight = (int) (height * scale);
            frames[i] = Bitmap.createScaledBitmap(frame, scaleWidth, scaleHeight, false);
        }
        return frames;
    }

    private void setupAnimations(Resources resources) {
        runFrames = setupFrames(resources, TOTAL_RUN_FRAMES, R.drawable._run);
        attackFrames = setupFrames(resources, TOTAL_ATTACK_FRAMES, R.drawable._attack);
        jumpFrames = setupFrames(resources, TOTAL_JUMP_FRAMES, R.drawable._jump_fall_in_between);
        deathFrames = setupFrames(resources, TOTAL_DEATH_FRAMES, R.drawable._death);
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
            if (isAttackSoundLoaded) {
                soundPool.play(attackSoundId, soundValue, soundValue, 1, 0, 1f);
            }
        }
    }

    public void jump() {
        if (!isJumping) {
            isJumping = true;
            velocityY = jumpStrength;
            currentFrame = 0;
            lastFrameTime = System.currentTimeMillis();
            if (isJumpSoundLoaded) {
                soundPool.play(jumpSoundId, soundValue, soundValue, 1, 0, 1f);
            }
        }
    }

    public void die() {
        if (!isDying) {
            isDying = true;
            currentFrame = 0;
            lastFrameTime = System.currentTimeMillis();
        }
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean isAttacking() {
        return this.isAttacking;
    }

    public boolean isDying() {
        return isDying;
    }
}