package itstep.learning.android_pv_221;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OnSwipeListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;

    public OnSwipeListener( Context context ) {
        this.gestureDetector = new GestureDetector( context, new SwipeGestureListener() );
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent( event );
    }

    public void onSwipeBottom() { }
    public void onSwipeLeft()   { }
    public void onSwipeRight()  { }
    public void onSwipeTop()    { }

    private final class SwipeGestureListener
            extends GestureDetector.SimpleOnGestureListener {
        private final static int minVelocity = 150;
        private final static int minDistance = 100;
        private final static double minRatio = 1.0 / 2.0;

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;   // true означає, що подія оброблена даним детектором
        }

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2,
                               float velocityX, float velocityY) {
            boolean isHandled = false;
            if(e1 == null) return false;
            float deltaX = e2.getX() - e1.getX();   // e1 - точка (x,y) початку жесту
            float deltaY = e2.getY() - e1.getY();   // e2 - точка кінця жесту
            float distanceX = Math.abs( deltaX );
            float distanceY = Math.abs( deltaY );
            if( distanceX * minRatio > distanceY &&
                    distanceX >= minDistance ) {              // горизонтальний свайп
                if( Math.abs( velocityX ) >= minVelocity ) {  // аналізуємо лише швидкість Х
                    if( deltaX > 0 ) {
                        onSwipeRight();
                    }
                    else {
                        onSwipeLeft();
                    }
                    isHandled = true;
                }
            }
            else if( distanceY * minRatio > distanceX  &&
                        distanceY >= minDistance ) {          // вертикальний свайп
                if( Math.abs( velocityY ) >= minVelocity ) {  // аналізуємо лише швидкість Y
                    if( deltaY > 0 ) {
                        onSwipeBottom();
                    }
                    else {
                        onSwipeTop();
                    }
                    isHandled = true;
                }
            }
            return isHandled;
        }
    }
}
/*
Детектор жестів. Свайпи.
GestureDetector - детектор жестів, призначений для розпізнавання жестів
та запуск обробників (Listener) в залежності від визначеного жесту.
Детектор "спостерігає" за певним контекстом (представленням, областю екрану)
Визначення свайпів базується на аналізі події "onFling" - жест, що складається
з торкання, проведення та відпускання екрану пристрою, а також нівелювання
події "onDown", яка може призвести до синтезу "Tap" або "Click"
В основі визначення свайпів - аналіз швидкості та відстані жесту проведення,
а також можливості віднесення його до одного з напрямів (у даному прикладі - їх 4)
 */