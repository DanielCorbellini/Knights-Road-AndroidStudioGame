package com.example.myandroidgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable{

    private Thread thread;
    private boolean isPlaying;
    private Background background1;
    private Background background2;
    private Player player;
    private Enemy enemy;

    private int screenX;
    private int screenY;
    private float screenRatioX;
    private float screenRatioY;
    private Paint paint;

    // Variáveis para spawnar inimigos de maneira aleatória
    private List<Enemy> enemies = new ArrayList<>();
    private long lastSpawnTime = 0;
    private Random random = new Random();

    float backgroundSpeed;

    public GameView(Context context, int screenX, int screenY) {
        super(context);

        this.screenX = screenX;
        this.screenY = screenY;

        screenRatioX = screenX / 1920f;
        screenRatioY = screenY / 1080f;

        backgroundSpeed = 10;

        // Inicializa backgrounds
        background1 = new Background(screenX, screenY, getResources());
        background2 = new Background(screenX, screenY, getResources());
        background2.x = screenX;

        // Inicializa personagem
        player = new Player(getResources(), context, screenX, screenY);
        paint = new Paint();
    }

    @Override
    public void run() {
        while(isPlaying) {
            update();
            draw();
            sleep();
        }
    }

    // lógica do jogo (movimento, colisão, spawn, vida, etc.)
    private void update() {
        long currentTime = System.currentTimeMillis();
        // Atualiza backgrounds
        background1.x -= backgroundSpeed;
        background2.x -= backgroundSpeed;

        if (background1.x + screenX <= 0) {
            background1.x = background2.x + screenX;
        }

        if (background2.x + screenX <= 0) {
            background2.x = background1.x + screenX;
        }

        // Atualiza personagem
        player.update();

        // Spawn de inimigos
        if (currentTime - lastSpawnTime > getRandomSpawnDelay()) {
            spawnEnemy();
            lastSpawnTime = currentTime;
        }

        // Atualiza inimigos
        Iterator<Enemy> enemiesList = enemies.iterator();
        while (enemiesList.hasNext()) {
            Enemy enemy = enemiesList.next();
            enemy.update();

            // Se o inimigo sair da tela, ele desaparece
            if (enemy.getX() + enemy.getWidth() < 0 || enemy.isDead()) {
                enemiesList.remove();
                continue;
            }

            // Colisão com ataque do player
            if (player.isAttacking() && Rect.intersects(player.getBounds(), enemy.getBounds())) {
                enemy.die();
            }
        }
    }

    // Apenas desenha na tela o que foi calculado.
    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();

            // Desenha backgrounds primeiro (fundo)
            canvas.drawBitmap(background1.background, background1.x, background1.y, paint);
            canvas.drawBitmap(background2.background, background2.x, background2.y, paint);

            // Desenha personagem por cima
            player.draw(canvas, paint);

            for (Enemy enemy : enemies) {
                enemy.draw(canvas, paint);
            }
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();

            if (touchX > screenX / 2) {
                player.attack();
            }

            if (touchX < screenX / 2) {
                player.jump();
            }
        }
        return true;
    }

    // spawn de inimigos
    private void spawnEnemy() {
        int[] sprites = {
                R.drawable.enemy1_flight,
                R.drawable.enemy2_run,
                R.drawable.enemy3_walk
        };

        // Escolhe um sprite aleatório
        int spriteId = sprites[random.nextInt(sprites.length)];

        Enemy enemy = new Enemy(getResources(), spriteId, screenX, screenY);
        enemies.add(enemy);
    }

    // Intervalo para spawn de inimigos
    private long getRandomSpawnDelay() {
        return 2000 + random.nextInt(1500); // entre 2s e 3.5s
    }

    private void sleep() {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}