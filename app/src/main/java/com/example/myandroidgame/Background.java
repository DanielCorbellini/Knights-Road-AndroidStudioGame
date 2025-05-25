package com.example.myandroidgame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Background {
    int x = 0;
    int y = 0;

    Bitmap background;

    Background(int screenX, int screenY, Resources res) {
        background = BitmapFactory.decodeResource(res, R.drawable.img);
        background = Bitmap.createScaledBitmap(background, screenX, screenY, false);
    }
}
