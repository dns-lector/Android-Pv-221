package itstep.learning.android_pv_221;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private final int N = 4;
    private final int[][] cells = new int[N][N];
    private final TextView[][] tvCells = new TextView[N][N];
    private final Random random = new Random();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView( R.layout.activity_game );
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        LinearLayout gameField = findViewById( R.id.game_ll_field );
        gameField.post( () -> {
            // дії, що будуть виконані коли
            // об'єкт (gameField) буде готовий приймати повідомлення,
            // тобто завершить "будову"
            int vw = this.getWindow().getDecorView().getWidth();
            int fieldMargin = 20;
            // Змінюємо параметри Layout для gameField з метою зробити його квадратним
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    vw - 2 * fieldMargin,
                    vw - 2 * fieldMargin
            );
            layoutParams.setMargins( fieldMargin, fieldMargin, fieldMargin, fieldMargin );
            layoutParams.gravity = Gravity.CENTER;
            gameField.setLayoutParams( layoutParams );
        } );
        // NumberFormat.getInstance(Locale.ROOT).parse("1.23").doubleValue()
        gameField.setOnTouchListener( new OnSwipeListener(GameActivity.this) {
                    @Override
                    public void onSwipeBottom() {
                        Toast.makeText(GameActivity.this, "Bottom", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSwipeLeft() {
                        if( moveLeft() ) {
                            spawnCell();
                            showField();
                        }
                        else {
                            Toast.makeText(GameActivity.this, "No Left Move", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSwipeRight() {
                        Toast.makeText(GameActivity.this, "Right", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSwipeTop() {
                        Toast.makeText(GameActivity.this, "Top", Toast.LENGTH_SHORT).show();
                    }
                });
        initField();
        spawnCell();
        showField();
    }

    private boolean moveLeft() {
        boolean result = false;
        for (int i = 0; i < N; i++) {      // [4 2 2 4]
            int j0 = -1;
            for (int j = 0; j < N; j++) {
                if( cells[i][j] != 0 ) {
                    if( j0 == -1 ) {
                        j0 = j;
                    }
                    else {
                        if( cells[i][j] == cells[i][j0] ) {  // collapse
                            cells[i][j] *= 2;
                            cells[i][j0] = 0;
                            result = true;
                            j0 = -1;
                        }
                        else {
                            j0 = j;
                        }
                    }
                }
            }
            j0 = -1;
            for (int j = 0; j < N; j++) {
                if( cells[i][j] == 0 ) {   // [0 2 0 4] -> [2 4 0 0]
                    if( j0 == -1 ) {       // [0 0 0 2]     [0 0 2 2]
                        j0 = j;
                    }
                }
                else if( j0 != -1 ) {
                    cells[i][j0] = cells[i][j];
                    cells[i][j] = 0;
                    j0 += 1;
                    result = true;
                }
            }
        }
        return result;
    }

    private boolean spawnCell() {
        // 1 зібрати дані про порожні комірки
        // 2 вибрати одну випадково
        // 3 поставити в неї 2 (з імовірністю 0,9) або 4
        List<Coordinates> freeCells = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if( cells[i][j] == 0 ) {
                    freeCells.add( new Coordinates(i, j) );
                }
            }
        }
        if( freeCells.isEmpty() ) {
            return false;
        }
        Coordinates randomCoordinates = freeCells.get( random.nextInt( freeCells.size() ) );
        cells[randomCoordinates.x][randomCoordinates.y] =
                random.nextInt(10) == 0 ? 4 : 2;
        return true;
    }

    private void initField() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                // cells[i][j] = (int) Math.pow( 2, i * N + j ) ;
                cells[0][0] = 0;
                tvCells[i][j] = findViewById(
                    getResources().getIdentifier(
                        "game_cell_" + i + j,
                        "id",
                        getPackageName()
                    )
                );
            }
        }
    }

    private void showField() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvCells[i][j].setText( String.valueOf( cells[i][j] ) ) ;
                tvCells[i][j].setBackgroundColor( getResources().getColor(
                        getResources().getIdentifier(
                                cells[i][j] <= 2048
                                        ? "game_tile_" + cells[i][j]
                                        : "game_tile_other",
                                "color",
                                getPackageName()
                        ),
                        getTheme()
                ) ); ;
                tvCells[i][j].setTextColor( getResources().getColor(
                        getResources().getIdentifier(
                                cells[i][j] <= 2048
                                        ? "game_text_" + cells[i][j]
                                        : "game_text_other",
                                "color",
                                getPackageName()
                        ),
                        getTheme()
                ) ) ;
            }
        }
    }

    static class Coordinates {
        int x, y;

        public Coordinates(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}