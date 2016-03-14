package araba.akilli.saurobot.sauroakllaraba;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.format.Time;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

enum Hareket
{
    Ileri,
    Geri,
    Dur

}

@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements SensorEventListener, OnClickListener {
    public static BluetoothSocket mmSocket=null;
    float x, y, z;
    double xDegeri, yDegeri;
    TextView textx, texty;//, GidisYonu, DonusYonu;
    Button btnForward, btnDurdur , btnKalibreEt;
    TextView txtSpeed, textView;
    ProgressBar progressLeft,progressRight;
    private int progressStatus = 50, speed = 0;
    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    ImageView imageView;
    Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    Sensor mOrientation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Ekran yatay yapılacak.
        // Progresbar ayarlanacak.
        super.onCreate(savedInstanceState);
        setContentView(araba.akilli.saurobot.sauroakllaraba.R.layout.activity_main);
        StaticFunctions.onCreateSetups(this);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mOrientation= mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        KalibrasyonParametreleriniHesapla(0,80);
        SetScreenElements();
        SetElementClickListener();
        SettingUpBluetooth();
        progressLeft.setProgress(progressStatus);
        progressRight.setProgress(progressStatus);
        final Intent i = getIntent();
        //String kontrol = i.getStringExtra("control");
        BluetoothDevice DeviceTemp = i.getParcelableExtra("device");
        if (DeviceTemp !=null)
        {
            getStreams();
            //mmDevice=DeviceTemp;

        }
        else{
            //baglanamadi
        }
    }
    protected void onResume() {
        super.onResume();
        /*Toast.makeText(getApplicationContext(), "Sensörler:" +
                "\nMagnet:"+magnetometer+
                "\nAccel:"+accelerometer+
                "\nOrient:"+mOrientation, Toast.LENGTH_LONG).show();*/
        //mOrientation=null;
        if (mOrientation!=null) {
            mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_GAME);
        }
		else if (accelerometer!=null && magnetometer!=null) {
            mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
			mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
        }
		else
		{
			Toast.makeText(getApplicationContext(), "Uygulama için sesnsörleriniz yeterli değil", Toast.LENGTH_LONG).show();
		}
    }
    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        mSensorManager.unregisterListener(this, mOrientation);
        try {
            if (  mmSocket!=null) {
                mmSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            mmSocket=null;
            mmOutputStream=null;
        }
    }


    public float RollFarkHesapla(float derece )
    {
        float OrtaDeger= KalibreYon;
        float fark;
        fark= derece-OrtaDeger;
        if(fark>100)
            fark-=180;
        return fark;
    }
    public float AzimutFarkHesapla(float derece )
    {
        float OrtaDeger= KalibreAzimut;
        float fark=0;
        fark=derece-OrtaDeger;
        if(fark>180)
            fark-=360;

        return -fark;
    }

    Hareket HareketDogrultusu;
    float[] mGravity=null;
    float[] mGeomagnetic=null;
    /*Time now = new Time();
    Time last = new Time();*/
    @Override
    public void onSensorChanged(SensorEvent event) {
        //https://ferhanakman.wordpress.com/2010/01/06/android-sensor-apilerini-kullanmak/
		//http://shrinidhikaranth.blogspot.com.tr/2015/01/how-to-develop-augmented-reality-app-on.html
		//todo: gönderme süresi için senkronizasyonm düşünülecek
        double gidishizi =0;
        double donushizi =0;
        /*now.setToNow();

        last.setToNow();*/
		if(event.sensor.getType() == Sensor.TYPE_ORIENTATION)
		{
            float pitch=event.values[2] +event.values[2]* (float)Math.sin(Math.abs(RollFarkHesapla(event.values[1]) / 180 * Math.PI));
            //float azimuth=AzimutFarkHesapla(event.values[0]);
            //float roll=event.values[1];

            double aci= event.values[2]/180*Math.PI;

            double donus=event.values[1];//azimuth*Math.cos(aci)+ roll*Math.sin(aci);
			//textView.setText(String.format("ORİENTATİON : %f , %f, %f ,%f ,%f ,%f ,%f", event.values[0],event.values[1], event.values[2], aci,donus,pitch,donus));
			//textView.setText(String.format("ORİENTATİON : %f , %f, %f ,%f ,%f ,%f ,%f ,%f ,%f", event.values[0],event.values[1], event.values[2], azimuth,roll,aci,donus,pitch,donus));
            gidishizi = GidisHizi((int) pitch);

			donushizi = DonusHizi(donus);

            if (!KalibreVarMi)
            {
                //value 2 hız ; value 1 yon
                KalibreEt((int) event.values[2],(int)event.values[1],(int)event.values[0]);
            }
		}


		else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER &&magnetometer==null)
		{
			mGravity = event.values;
			textView.setText(String.format("ACCELEREROMETER: %f , %f, %f", event.values[0],event.values[1], event.values[2]));
            gidishizi = GidisHizi((int)event.values[2]);
			donushizi = DonusHizi(event.values[1]);
		}
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER &&magnetometer!=null)
        {
            textView.setText(String.format("ACCELEREROMETER: %f , %f, %f", event.values[0],event.values[1], event.values[2]));
            mGravity = event.values;
        }
		else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		{
            textView.setText(String.format("MAGNETİC: %f , %f, %f", event.values[0],event.values[1], event.values[2]));
			mGeomagnetic = event.values;
		}
		if (mGravity != null && mGeomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				// at this point, orientation contains the azimuth, pitch and roll values.


                for(int i=0;i<3;i++)
				{
					orientation[i]= (float) (180 * orientation[i] / Math.PI);
					/*
					float swAzimuth = (float) (180 * orientation[0] / Math.PI);
					float swPitch = (float) (180 * orientation[1] / Math.PI);
					float swRoll = (float) (180 * orientation[2] / Math.PI);
					*/
				}
                if (!KalibreVarMi)
                {
                    //value 2 hız ; value 1 yon
                    KalibreEt((int) orientation[2],(int)orientation[1],(int)orientation[0]);
                }
                textView.setText(String.format("MAGNETİC+ACCELERO: %f , %f, %f", orientation[0],orientation[1], orientation[2]));
				gidishizi = GidisHizi((int)orientation[2]);
				donushizi = DonusHizi(orientation[1]);
			}
		}
        if(donushizi < 10)
        {
            progressRight.setProgress(0);
            progressLeft.setProgress(10 - (int) donushizi);
        }
        else if(donushizi > 10 )
        {
            progressLeft.setProgress(0);
            progressRight.setProgress((int) donushizi - 10);
        }
        else
        {
            progressRight.setProgress(0);
            progressLeft.setProgress(0);
        }

        if (HareketDogrultusu==Hareket.Ileri) {
            txtSpeed.setText(String.format("%.0f", gidishizi ));
            txtSpeed.setTextColor(Color.BLUE);

            VeriGonderimi(10 + gidishizi, donushizi);

        } else {
            txtSpeed.setText(String.format("%.0f", gidishizi));
            txtSpeed.setTextColor(Color.RED);
            VeriGonderimi(10 - gidishizi, donushizi);
        }

    }
    public void VeriGonderimi(double gidishizi, double donushizi )
    {
        String gecici="{"+Integer.toString((int)gidishizi)+","+Integer.toString((int) donushizi)+"}";
        try
        {
            if(mmOutputStream != null) {
                mmOutputStream.write(gecici.getBytes());
                mmOutputStream.flush();
            }
        }
        catch (Exception e)
        {
            Log.d("BLUETOOTH_CLIENT", e.getMessage());
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    // kalibrasyon sabit paremetreleri;
    int KalibreTekrari ;
    int MaxKalibreTekrarSayisi;
    int KalibreHareketDizisi[];
    int KalibreYonDizisi[];
    int KalibreAzimutDizisi[];
    int KalibreAzimut;
    int KalibreHareket;
    int KalibreYon;
    public boolean KalibreEt(int KalibreHareket, int KalibreYon,int KalibreAzimut)
    {
        if(KalibreTekrari < MaxKalibreTekrarSayisi)
        {
            this.KalibreHareketDizisi[KalibreTekrari]=KalibreHareket;
            this.KalibreYonDizisi[KalibreTekrari]=KalibreYon;
            KalibreAzimutDizisi[KalibreTekrari]=KalibreAzimut;
            KalibreTekrari++;
            return false;
        }
        else{
            this.KalibreYon=0;
            this.KalibreHareket=0;
            this.KalibreAzimut=0;
            for (int yon: this.KalibreYonDizisi) {
                this.KalibreYon+=yon;
            }
            for (int hareket: this.KalibreHareketDizisi) {
                this.KalibreHareket+=hareket;
            }
            for (int azimut: this.KalibreAzimutDizisi) {
                this.KalibreAzimut+=azimut;
            }
            this.KalibreHareket/=MaxKalibreTekrarSayisi;
            this.KalibreYon/=MaxKalibreTekrarSayisi;
            this.KalibreAzimut/=MaxKalibreTekrarSayisi;
            KalibrasyonParametreleriniHesapla(this.KalibreYon,this.KalibreHareket);

            Toast.makeText(getApplicationContext(), "Kalibrasyon tamamlandı.", Toast.LENGTH_SHORT).show();
            return true;
        }
    }
    private void KalibrasyonParametreleriniHesapla(int KalibreYon, int KalibreHareket){

        KalibreDurmaDereceArtisi=37;
        KalibreHareketBoslukDerecsi=5;
        KalibreHareketHassasiyetAraligi=20;

        KalibreYonBoslukDerecsi=10;
        KalibreYonHassasiyetAraligi=20;
        SolaYonMaximum=80;
        SagaYonMaximum=-80;

        KalibreVarMi=true;

        // yon kalibrasyon parametre hesaplama
        KalibreYonDurmaDercesi=KalibreYon;
        SolaBaslangicYonDerecesi=KalibreYonBoslukDerecsi;
        SolaSonYonDerecesi=SolaBaslangicYonDerecesi+KalibreYonHassasiyetAraligi;

        SagaBaslangicYonDerecesi = - KalibreYonBoslukDerecsi;
        SagaSonYonDerecesi= SagaBaslangicYonDerecesi- KalibreYonHassasiyetAraligi;

        // hız kalibrasyon parametre hesaplama
        KalibreHareketDurmaDercesi=KalibreHareket-KalibreDurmaDereceArtisi;
        IleriBaslangicHizDerecesi= KalibreHareketDurmaDercesi-KalibreHareketBoslukDerecsi;
        IleriSonHizDerecesi=IleriBaslangicHizDerecesi-KalibreHareketHassasiyetAraligi;

        GeriBaslangicHizDerecesi=KalibreHareketDurmaDercesi+KalibreHareketBoslukDerecsi;
        GeriSonHizDerecesi=GeriBaslangicHizDerecesi+KalibreHareketHassasiyetAraligi;
    }
    // kalibrasyon yön ve hareket parametreleri
    boolean KalibreVarMi;
    int KalibreDurmaDereceArtisi,
        KalibreHareketDurmaDercesi,
        KalibreHareketBoslukDerecsi,
        GeriBaslangicHizDerecesi,
        GeriSonHizDerecesi,
        IleriBaslangicHizDerecesi,
        IleriSonHizDerecesi,
        KalibreHareketHassasiyetAraligi;

    int     KalibreYonDurmaDercesi,
            KalibreYonBoslukDerecsi,
            SolaBaslangicYonDerecesi,
            SolaSonYonDerecesi,
            SagaBaslangicYonDerecesi,
            SagaSonYonDerecesi,
            KalibreYonHassasiyetAraligi,
            SolaYonMaximum,
            SagaYonMaximum;


    public void KalibreBaslat()
    {

        KalibreVarMi=false;
        KalibreTekrari=0;
        MaxKalibreTekrarSayisi=30;
        KalibreHareketDizisi=new int[MaxKalibreTekrarSayisi];
        KalibreYonDizisi=new int[MaxKalibreTekrarSayisi];
        KalibreAzimutDizisi=new int[MaxKalibreTekrarSayisi];
    }
    public int HareketHiz;
    public float GidisHizi(int gidisSensorDerecesi) // values[2]
    {
        if (gidisSensorDerecesi>=GeriSonHizDerecesi)
        {
            HareketDogrultusu=Hareket.Geri;
            HareketHiz=GeriSonHizDerecesi-GeriBaslangicHizDerecesi;
        }
        else if (gidisSensorDerecesi>GeriBaslangicHizDerecesi)
        {
            HareketDogrultusu=Hareket.Geri;
            HareketHiz=gidisSensorDerecesi-GeriBaslangicHizDerecesi;
        }
        else if (gidisSensorDerecesi<=IleriSonHizDerecesi)
        {
            HareketDogrultusu=Hareket.Ileri;
            HareketHiz=IleriBaslangicHizDerecesi-IleriSonHizDerecesi;
        }
        else if (gidisSensorDerecesi<IleriBaslangicHizDerecesi)
        {
            HareketDogrultusu=Hareket.Ileri;
            HareketHiz=IleriBaslangicHizDerecesi-gidisSensorDerecesi;
        }
        else{
            HareketDogrultusu=Hareket.Dur;
            HareketHiz=0;
        }
        HareketHiz/=2;
        return HareketHiz;
    }

    public double DonusHizi(double donushizi) // values[1]
    {
        if( donushizi >= SolaSonYonDerecesi && donushizi <= SolaYonMaximum )
        {
            return 0;
        }
        else if (donushizi >= SolaBaslangicYonDerecesi && donushizi< SolaSonYonDerecesi)
        {
            return 9 -((donushizi-10)/2.22);
        }
        else if (donushizi <= SagaSonYonDerecesi && donushizi >= SagaYonMaximum )
        {
            return 20;
        }
        else if (donushizi <= SagaBaslangicYonDerecesi && donushizi >= SagaSonYonDerecesi)
        {
            return 11 - ((donushizi + 10 )/2.22);
        }
        else
        {
            return 10;
        }
    }

    private void SetScreenElements() {
        txtSpeed = (TextView) findViewById(R.id.txtSpeed);
        textView = (TextView) findViewById(R.id.textView);
        btnForward = (Button) findViewById(R.id.btnForward);
        btnKalibreEt = (Button) findViewById(R.id.btnKalibreEt);
        btnDurdur = (Button) findViewById(R.id.btnDurdur);
        progressLeft = (ProgressBar) findViewById(R.id.progLeft);
        progressRight = (ProgressBar) findViewById(R.id.progRight);
    }

    private void SetElementClickListener() {
        btnForward.setOnClickListener(this);
        btnKalibreEt.setOnClickListener(this);
        btnDurdur.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == btnForward.getId()) {
            Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
            startActivity(intent);
        }
        else if (v.getId() == btnKalibreEt.getId()) {
            KalibreBaslat();
            Toast.makeText(getApplicationContext(), "Kalibre Ediliyor: Telefonu Yatay ve Yere Dik Tutunuz", Toast.LENGTH_SHORT).show();
        }else if (v.getId() == btnDurdur.getId()) {
            if (mmOutputStream!=null)
            {
                VeriGonderimi(10,10);
                VeriGonderimi(10,10);
                mmOutputStream=null;
                btnDurdur.setText("Başlat");
            }
            else {
                if(getStreams())
                {
                    //durdur get stremda yazıldı
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Bluetoothla Bağlatı Kurmalısnız.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    public boolean getStreams() {
        try {
            if(mmSocket!=null)
            {
                mmOutputStream = mmSocket.getOutputStream();
                //mmInputStream = mmSocket.getInputStream();
                if (mmOutputStream!=null)
                {
                    btnDurdur.setText("Durdur");
                    return true;
                }
            }
        } catch (IOException e) {
            Log.d("BLUETOOTH_CLIENT", e.getMessage());
        }
        return false;
    }

    public void SettingUpBluetooth() {
        if (adapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth aygıtı bulunamadı.", Toast.LENGTH_LONG).show();
        } else {
            if (!adapter.isEnabled()) {
                //Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //startActivityForResult(turnOn, 1);
            }
        }
    }
}