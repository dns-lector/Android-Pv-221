package itstep.learning.android_pv_221;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CalcActivity extends AppCompatActivity {
    private static final int maxDigits = 9;
    private TextView tvResult;
    private String zeroSign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvResult = findViewById( R.id.calc_tv_result );
        zeroSign = getString( R.string.calc_btn_digit_0 ) ;
        for( int i = 0; i < 10; i++ ) {
            String btnIdName = "calc_btn_digit_" + i;
            @SuppressLint("DiscouragedApi") int btnId = getResources().getIdentifier(
                    btnIdName, "id", getPackageName()
            );
            findViewById( btnId ).setOnClickListener( this::btnClickDigit );
        }
        findViewById( R.id.calc_btn_c ).setOnClickListener( this::btnClickC );
        findViewById( R.id.calc_btn_backspace ).setOnClickListener( this::btnClickBackspace );
        btnClickC(null);
    }

    // region OnChangeQualifier
    // події, що супроводжують зміну конфігурації оточення, наприклад,
    // поворот пристрою
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence( "tv_result", tvResult.getText() );
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText( savedInstanceState.getCharSequence( "tv_result" ) );
    }
    // endregion

    private void btnClickBackspace(View view ) {
        String resText = tvResult.getText().toString();
        resText = resText.substring( 0, resText.length() - 1 );
        if( resText.isEmpty() ) {
            resText = zeroSign;
        }
        tvResult.setText( resText );
    }

    private void btnClickDigit( View view ) {
        String resText = tvResult.getText().toString();
        if( resText.length() >= maxDigits ) {
            Toast.makeText(this, R.string.calc_msg_too_long, Toast.LENGTH_SHORT).show();
            return;
        }
        if( resText.equals( zeroSign ) ) {
            resText = "";
        }
        resText += ((Button)view).getText();
        tvResult.setText( resText );
    }

    private void btnClickC( View view ) {
        tvResult.setText( zeroSign );
    }
}
/*
Д.З. Завершити роботу з проєктом "Калькулятор"
 */