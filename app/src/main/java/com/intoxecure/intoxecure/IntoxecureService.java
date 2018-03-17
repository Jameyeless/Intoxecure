package com.intoxecure.intoxecure;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

public class IntoxecureService extends Service implements SensorEventListener {
    public IntoxecureService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private Sensor mySensor;
    private SensorManager SM;
    private static final float SHAKE_THRESHOLD = 10.00f; // m/S**2
    private static final int MIN_TIME_BETWEEN_SHAKES_MILLISECS = 1000;
    private static final String CHANNEL_ID = "intoxecure_service_id";
    private long mLastShakeTime;
    public static float x, y, z;
    public static final String INTOXECURE_SERVICE = "com.intoxecure.intoxecure.IntoxecureService";

    @Override
    public void onCreate() {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - mLastShakeTime) > MIN_TIME_BETWEEN_SHAKES_MILLISECS) {

                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                double acceleration = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;
                Log.d("mySensor", "Acceleration is " + acceleration + "m/s^2");

                if (acceleration > SHAKE_THRESHOLD) {
                    mLastShakeTime = curTime;
                    Toast.makeText(getApplicationContext(), "FALL DETECTED",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public int onStartCommand(Intent intent, int flags, int startId) {


        if (intent.getIntExtra("stop", 0) == 0) {
            SM = (SensorManager) getSystemService(SENSOR_SERVICE);
            mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);

            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setContentTitle("Intoxecure")
                    .setContentText("Accelerometer is active")
                    .setSmallIcon(R.drawable.intoxecure_logo_v1)
                    .setAutoCancel(false);

            Notification notification;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                notification = nBuilder.build();
            else
                notification = nBuilder.getNotification();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the NotificationChannel, but only on API 26+ because
                // the NotificationChannel class is new and not in the support library
                CharSequence name = getString(R.string.channel_name);
                String description = getString(R.string.channel_description);
                int importance = NotificationManagerCompat.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                // Register the channel with the system
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(channel);
            }

            startForeground(1, notification);

        } else {
            SM.unregisterListener(this);
            this.stopForeground(true);
            this.stopSelf();
        }
        return Service.START_STICKY;
    }
}
