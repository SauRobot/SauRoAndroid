package araba.akilli.saurobot.sauroakllaraba;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class GirisSayfasi extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giris_sayfasi);
        StaticFunctions.onCreateSetups(this);
        // Thread hazırlanıyor
        Thread thread = new Thread() {

            @Override
            public void run() {

                try {
                    synchronized (this) {
                        // Uygulama 4 saniye aynı ekranda bekliyor
                        wait(2000);
                    }
                } catch (InterruptedException e) {

                    // Hata yönetimi

                } finally {

                    finish();

                    // Yeni açılmak istenen Intent
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), MainActivity.class);
                    startActivity(intent);

                }

            }
        };

        // Thread başlatılıyor
        thread.start();

    }
}
