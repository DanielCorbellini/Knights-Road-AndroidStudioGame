package com.example.myandroidgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceView;

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
        player = new Player(getResources(), screenX, screenY);
        enemy = new Enemy(getResources(), screenX, screenY);


        enemy.setVelocity(-backgroundSpeed, 0);
        // Opcional: faz o personagem se mover junto com o cenário
        // player.setVelocity(-backgroundSpeed, 0); // Metade da velocidade do fundo

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

    private void update() {
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
    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();

            // Desenha backgrounds primeiro (fundo)
            canvas.drawBitmap(background1.background, background1.x, background1.y, paint);
            canvas.drawBitmap(background2.background, background2.x, background2.y, paint);

            // Desenha personagem por cima
            player.draw(canvas, paint);

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            player.attack();
        }
        return true;
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