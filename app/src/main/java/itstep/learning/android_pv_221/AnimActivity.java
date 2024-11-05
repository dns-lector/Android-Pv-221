package itstep.learning.android_pv_221;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AnimActivity extends AppCompatActivity {

    private Animation alphaDemo;
    private Animation scaleDemo;
    private Animation rotateDemo;
    private Animation translateDemo;
    private Animation bellDemo;
    private AnimationSet animationSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anim);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        alphaDemo = AnimationUtils.loadAnimation(this, R.anim.alpha_demo );
        scaleDemo = AnimationUtils.loadAnimation(this, R.anim.scale_demo );
        rotateDemo = AnimationUtils.loadAnimation(this, R.anim.rotate_demo );
        translateDemo = AnimationUtils.loadAnimation(this, R.anim.translate_demo );
        bellDemo = AnimationUtils.loadAnimation(this, R.anim.bell );
        animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleDemo);
        animationSet.addAnimation(rotateDemo);
        findViewById( R.id.anim_alpha ).setOnClickListener( this::alphaClick );
        findViewById( R.id.anim_scale ).setOnClickListener( this::scaleClick );
        findViewById( R.id.anim_rotate ).setOnClickListener( this::rotateClick );
        findViewById( R.id.anim_translate ).setOnClickListener( this::translateClick );
        findViewById( R.id.anim_combo ).setOnClickListener( this::comboClick );
        findViewById( R.id.anim_bell ).setOnClickListener( this::bellClick );
    }

    private void alphaClick( View view ) {
        view.startAnimation( alphaDemo );
    }
    private void scaleClick( View view ) {
        view.startAnimation( scaleDemo );
    }
    private void rotateClick( View view ) {
        view.startAnimation( rotateDemo );
    }
    private void translateClick( View view ) {
        view.startAnimation( translateDemo );
    }
    private void comboClick( View view ) {
        view.startAnimation( animationSet );
    }
    private void bellClick( View view ) {
        view.startAnimation( bellDemo );
    }
}