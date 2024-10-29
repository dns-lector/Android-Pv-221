package itstep.learning.android_pv_221;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button button = findViewById( R.id.main_btn_calc );
        button.setOnClickListener( this::onCalcButtonClick );

        findViewById( R.id.main_btn_game ).setOnClickListener( this::onGameButtonClick );
    }

    private void onCalcButtonClick( View view ) {
        Intent intent = new Intent( MainActivity.this, CalcActivity.class );
        startActivity( intent );
    }
    private void onGameButtonClick( View view ) {
        Intent intent = new Intent( MainActivity.this, GameActivity.class );
        startActivity( intent );
    }
}
/*
Д.З. Створити проєкт, налаштувати відображення
(кольори, шрифти, розміри, відступи)
Опублікувати проєкт, додати посилання на репозиторій
На репозиторії додати папку з скріншотами.
 */