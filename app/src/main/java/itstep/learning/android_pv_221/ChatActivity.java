package itstep.learning.android_pv_221;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private static final SimpleDateFormat sqlDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.ROOT
    );
    private final String chatUrl = "https://chat.momentfor.fun/";
    private TextView tvTitle;
    private LinearLayout chatContainer;
    private ScrollView chatScroller;
    private EditText etAuthor;
    private EditText etMessage;
    private View vBell;
    private final ExecutorService threadPool = Executors.newFixedThreadPool( 3 );
    private final Gson gson = new Gson();
    private final List<ChatMessage> messages = new ArrayList<>();
    private final Handler handler = new Handler();
    private Animation bellAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        //     Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        //     return insets;
        // });
        tvTitle       = findViewById( R.id.chat_tv_title     );
        chatContainer = findViewById( R.id.chat_ll_container );
        chatScroller  = findViewById( R.id.chat_scroller     );
        etAuthor      = findViewById( R.id.chat_et_author    );
        etMessage     = findViewById( R.id.chat_et_message   );
        vBell         = findViewById( R.id.chat_bell         );
        findViewById( R.id.chat_btn_send ).setOnClickListener( this::sendButtonClick );
        bellAnimation = AnimationUtils.loadAnimation(this, R.anim.bell );
        handler.post( this::periodic );
        chatScroller.addOnLayoutChangeListener( ( View v,
             int left,    int top,    int right,    int bottom,
             int leftWas, int topWas, int rightWas, int bottomWas) -> chatScroller.post(
                ()-> chatScroller.fullScroll( View.FOCUS_DOWN )
        ));
    }

    private void periodic() {
        loadChat();
        handler.postDelayed( this::periodic, 3000 );
    }

    private void sendButtonClick( View view ) {
        String author = etAuthor.getText().toString();
        if( author.isEmpty() ) {
            Toast.makeText(this, "Empty field: Author", Toast.LENGTH_SHORT).show();
            return;
        }
        String message = etMessage.getText().toString();
        if( message.isEmpty() ) {
            Toast.makeText(this, "Empty field: Message", Toast.LENGTH_SHORT).show();
            return;
        }
        CompletableFuture.runAsync( () ->
                sendChatMessage( new ChatMessage()
                        .setAuthor( author )
                        .setText( message )
                        .setMoment( sqlDateFormat.format( new Date() ) )
                ),
                threadPool
        );
    }

    private void sendChatMessage( ChatMessage chatMessage ) {
        try {
            URL url = new URL( chatUrl );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput( true );    // Очікується відповідь
            connection.setDoOutput( true );   // Будемо передавати дані (тіло)
            connection.setChunkedStreamingMode( 0 );   // надсилати одним пакетом (не ділити на чанки)
            // Конфігурація для надсилання даних форми
            connection.setRequestMethod( "POST" );
            // заголовки
            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            connection.setRequestProperty( "Accept", "application/json" );
            connection.setRequestProperty( "Connection", "close" );
            // тіло
            OutputStream bodyStream = connection.getOutputStream();
            // формат повідомлення форми: key1=value1&key2=value2
            bodyStream.write(
                    String.format( "author=%s&msg=%s",
                            URLEncoder.encode( chatMessage.getAuthor(), StandardCharsets.UTF_8.name() ),
                            URLEncoder.encode( chatMessage.getText(), StandardCharsets.UTF_8.name() )
                    ).getBytes( StandardCharsets.UTF_8 )
            );
            bodyStream.flush();   // передача запиту
            bodyStream.close();

            // Відповідь
            int statusCode = connection.getResponseCode();
            if( statusCode >= 200 &&  statusCode < 300 ) {   // OK
                Log.i( "sendChatMessage", "Message sent" );
                loadChat();
            }
            else {  // ERROR
                InputStream responseStream = connection.getErrorStream();
                Log.e( "sendChatMessage", readString( responseStream ) );
                responseStream.close();
            }
            connection.disconnect();
        }
        catch( Exception ex ) {
            Log.e( "sendChatMessage",
                    ex.getMessage() == null ? ex.getClass().toString() : ex.getMessage() ) ;
        }
    }

    private void loadChat() {
        CompletableFuture
                .supplyAsync( this::getChatAsString, threadPool )
                .thenApply( this::processChatResponse )
                .thenAccept( m -> runOnUiThread( () -> displayChatMessages(m) ) );
    }

    private String getChatAsString() {
        try( InputStream urlStream = new URL( chatUrl ).openStream() ) {
            return readString( urlStream );
        }
        catch( MalformedURLException ex ) {
            Log.e( "ChatActivity::loadChat",
                    ex.getMessage() == null ? "MalformedURLException" : ex.getMessage() );
        }
        catch( IOException ex ) {
            Log.e( "ChatActivity::loadChat",
                    ex.getMessage() == null ? "IOException" : ex.getMessage() );
        }
        return null;
    }

    private ChatMessage[] processChatResponse( String jsonString ) {
        ChatResponse chatResponse = gson.fromJson( jsonString, ChatResponse.class );
        return chatResponse.data;
    }

    private void displayChatMessages( ChatMessage[] chatMessages ) {
        // перевірити чи є нові повідомлення, якщо є, то додати до збереженої колекції,
        // якщо ні - ігнорувати виклик
        boolean wasNew = false;
        for( ChatMessage cm : chatMessages ) {
            if( messages.stream().noneMatch( m -> m.getId().equals( cm.getId() ) ) ) {
                // нове повідомлення
                messages.add( cm );
                wasNew = true;
            }
        }
        if( ! wasNew ) return ;
        // сортуємо за зростанням дати (останні у кінці)
        messages.sort( Comparator.comparing( ChatMessage::getMoment ) );

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(10, 15, 50, 5);
        Drawable bgOther = AppCompatResources.getDrawable(
                ChatActivity.this,
                R.drawable.chat_msg_other
        );
        for( ChatMessage cm : messages ) {
            if( cm.getView() != null ) continue;

            LinearLayout linearLayout = new LinearLayout( ChatActivity.this ) ;
            linearLayout.setOrientation( LinearLayout.VERTICAL );

            TextView tv = new TextView( ChatActivity.this );
            tv.setText( cm.getAuthor() + " " + cm.getMoment() );
            tv.setPadding( 30, 5, 30, 5 );
            linearLayout.addView( tv );

            tv = new TextView( ChatActivity.this );
            tv.setText( cm.getText() );
            tv.setPadding( 20, 5, 30, 5 );
            linearLayout.addView( tv );

            linearLayout.setBackground( bgOther );
            linearLayout.setLayoutParams( layoutParams );
            cm.setView( linearLayout );
            chatContainer.addView( linearLayout );
        }
        chatContainer.post( () -> {
            chatScroller.fullScroll( View.FOCUS_DOWN ) ;
            vBell.startAnimation( bellAnimation ) ;
        } ) ;
    }

    private String readString( InputStream stream ) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while( ( len = stream.read( buffer ) ) != -1 ) {
            byteBuilder.write( buffer, 0, len );
        }
        String res = byteBuilder.toString( StandardCharsets.UTF_8.name() ) ;
        byteBuilder.close();
        return res;
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        threadPool.shutdownNow();
        super.onDestroy();
    }


    class ChatResponse {
        private int status;
        private ChatMessage[] data;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public ChatMessage[] getData() {
            return data;
        }

        public void setData(ChatMessage[] data) {
            this.data = data;
        }
    }
    /*
    {
      "status": 1,
      "data": [ChatMessage]
     }
     */

    class ChatMessage {
        private String id;
        private String author;
        private String text;
        private String moment;

        private View view;

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAuthor() {
            return author;
        }

        public ChatMessage setAuthor(String author) {
            this.author = author;
            return this;
        }

        public String getText() {
            return text;
        }

        public ChatMessage setText(String text) {
            this.text = text;
            return this;
        }

        public String getMoment() {
            return moment;
        }

        public ChatMessage setMoment(String moment) {
            this.moment = moment;
            return this;
        }
    }
    /*
      "id": "3119",
      "author": "Irina",
      "text": "Привіт",
      "moment": "2024-11-03 16:39:23"
     */
}
/*
Internet. Одержання даних
Особливості
 - android.os.NetworkOnMainThreadException -
    при спробі працювати з мережею в основному (UI) потоці
    виникає виняток.
- java.lang.SecurityException: Permission denied (missing INTERNET permission?)
    для роботи з мережею Інтернет необхідно задекларувати дозвіл (у маніфесті)
- android.view.ViewRootImpl$CalledFromWrongThreadException:
    Only the original thread that created a view hierarchy can touch its views.
    Звернення до UI (setText, Toast, addView тощо) можуть бути
    тільки з того потоку, у якому він (UI) створений.
    Для передачі роботи до нього є метод runOnUiThread( Runnable );

Д.З. Чат: Забезпечити прибирання екранної клавіатури при зміні фокусу / кліку на
головне вікно чату.

 */
