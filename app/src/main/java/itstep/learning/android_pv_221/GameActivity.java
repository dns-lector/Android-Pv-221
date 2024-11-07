package itstep.learning.android_pv_221;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Logger;

public class GameActivity extends AppCompatActivity {
    private final String bestScoreFilename = "best_score.2048";
    private final int N = 4;
    private final int[][] cells = new int[N][N];
    private int[][] undo;
    private int prevScore;   // for undo
    private final TextView[][] tvCells = new TextView[N][N];
    private final Random random = new Random();
    private Animation spawnAnimation, collapseAnimation;
    private int score, bestScore;
    private TextView tvScore, tvBestScore;


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
        spawnAnimation = AnimationUtils.loadAnimation( this, R.anim.game_spawn ) ;
        collapseAnimation = AnimationUtils.loadAnimation( this, R.anim.game_collapse ) ;
        LinearLayout gameField = findViewById( R.id.game_ll_field );
        tvScore = findViewById( R.id.game_tv_score );
        tvBestScore = findViewById( R.id.game_tv_best_score );
        findViewById( R.id.game_btn_undo ).setOnClickListener( v -> undoMove() );

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
                        if( canMoveLeft() ) {
                            saveField();
                            moveLeft();
                            spawnCell();
                            showField();
                        }
                        else {
                            Toast.makeText(GameActivity.this, "No Left Move", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSwipeRight() {
                        if( moveRight() ) {
                            spawnCell();
                            showField();
                        }
                        else {
                            Toast.makeText(GameActivity.this, "No Right Move", Toast.LENGTH_SHORT).show();
                        }
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

    private void saveBestScore() {
        try( FileOutputStream fos = openFileOutput( bestScoreFilename, Context.MODE_PRIVATE ) ;
             DataOutputStream writer = new DataOutputStream( fos )
        ) {
            writer.writeInt( bestScore );
            writer.flush();
        }
        catch( IOException ex ) {
            Log.e( "GameActivity::saveBestScore",
                    ex.getMessage() != null ?  ex.getMessage() : "Error writing file"
            ) ;
        }
    }

    private void loadBestScore() {
        try(
                FileInputStream fis = openFileInput( bestScoreFilename ) ;
                DataInputStream reader = new DataInputStream( fis )
        ) {
            bestScore = reader.readInt() ;
        }
        catch( IOException ex ) {
            Log.e( "GameActivity::loadBestScore",
                    ex.getMessage() != null ?  ex.getMessage() : "Error reading file"
            ) ;
        }
    }

    private void saveField() {
        prevScore = score;
        undo = new int[N][N];
        for( int i = 0; i < N; i++ ) {
            System.arraycopy( cells[i], 0, undo[i], 0, N );
        }
    }

    private void undoMove() {
        if( undo == null ) {
            showUndoMessage();
            return;
        }
        score = prevScore;
        for( int i = 0; i < N; i++ ) {
            System.arraycopy( undo[i], 0, cells[i], 0, N );
        }
        undo = null;
        showField();
    }

    private void showUndoMessage() {
        new AlertDialog
                .Builder(this, androidx.appcompat.R.style.Base_V7_ThemeOverlay_AppCompat_Dialog )
                .setTitle( "Обмеження" )
                .setIcon( android.R.drawable.ic_dialog_alert )
                .setMessage( "Скасування ходу неможливе" )
                .setNeutralButton( "Закрити", (dlg, btn) -> {} )
                .setPositiveButton( "Підписка", (dlg, btn) -> Toast.makeText(this, "Скоро буде", Toast.LENGTH_SHORT).show() )
                .setNegativeButton( "Вийти", (dlg, btn) -> finish() )
                .setCancelable( false )
                .show();
    }

    private boolean canMoveLeft() {
        for (int i = 0; i < N; i++) {
            for (int j = 1; j < N; j++) {
                if( cells[i][j] != 0 && ( cells[i][j-1] == 0 || cells[i][j-1] == cells[i][j] ) ) {
                    return true;
                }
            }
        }
        return false;
    }

    private void moveLeft() {
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
                            score += cells[i][j];
                            tvCells[i][j].setTag( collapseAnimation );
                            cells[i][j0] = 0;
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
                    tvCells[i][j0].setTag( tvCells[i][j].getTag() );
                    cells[i][j] = 0;
                    tvCells[i][j].setTag( null );
                    j0 += 1;
                }
            }
        }
    }

    private boolean moveRight() {
        boolean result = false;
        for( int i = 0; i < N; i++ ) {
            boolean wasShift;
            do {
                wasShift = false;
                for (int j = N - 1; j > 0; j--) {
                    if (cells[i][j - 1] != 0 && cells[i][j] == 0) {
                        cells[i][j] = cells[i][j - 1];
                        cells[i][j - 1] = 0;
                        wasShift = result = true;
                    }
                }
            } while( wasShift );
            // Collapse
            for( int j = N - 1; j > 0; j-- ) {          //  [2 2 4 4]
                if( cells[i][j - 1] == cells[i][j] && cells[i][j] != 0 ) {
                    cells[i][j] *= 2;                   //  [2 2 4 8]
                    score += cells[i][j];
                    tvCells[i][j].setTag( collapseAnimation );
                    // cells[i][j - 1] = 0;             //  [2 2 0 8]
                    for( int k = j - 1; k > 0; k-- ) {  //  [2 2 2 8]
                        cells[i][k] = cells[i][k - 1];
                    }
                    cells[i][0] = 0;                    //  [0 2 2 8]
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
        tvCells[randomCoordinates.x][randomCoordinates.y].setTag( spawnAnimation );
        return true;
    }

    private void initField() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                // cells[i][j] = (int) Math.pow( 2, i * N + j +1 ) ;
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
        score = 0;
        loadBestScore();
    }

    private void showField() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvCells[i][j].setText( String.valueOf( cells[i][j] ) ) ;
                tvCells[i][j].getBackground().setColorFilter(
                        getResources().getColor(
                                getResources().getIdentifier(
                                        cells[i][j] <= 2048
                                                ? "game_tile_" + cells[i][j]
                                                : "game_tile_other",
                                        "color",
                                        getPackageName()
                                ),
                                getTheme()
                        ),
                        PorterDuff.Mode.SRC_ATOP);
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
                if( tvCells[i][j].getTag() instanceof Animation ) {
                    tvCells[i][j].startAnimation( (Animation) tvCells[i][j].getTag() );
                    tvCells[i][j].setTag( null );
                }
            }
        }
        tvScore.setText( getString( R.string.game_tv_score, String.valueOf( score ) ) );
        if( score > bestScore ) {
            bestScore = score;
            saveBestScore();
        }
        tvBestScore.setText( getString( R.string.game_tv_best, String.valueOf( bestScore ) ) );
    }

    static class Coordinates {
        int x, y;

        public Coordinates(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
/*
2048: Завершити роботу з проєктом
 */