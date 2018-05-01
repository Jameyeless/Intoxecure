package com.intoxecure.intoxecure;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import static java.lang.Math.sqrt;

public class IntoxecureService extends Service implements SensorEventListener {
    private static final String LOG_TAG = "ForegroundService";
    public static boolean IS_SERVICE_RUNNING = false;
    private static Notification notification;
    private static SensorManager sensorManager;
    private static Sensor stepDetector;
    private static Sensor accelerometer;
    public static double acceleration;
    private static long stepOldTime;
    private static long stepCurTime;
    private static double X[] = {0, 1, 2, 3, 4, 5, 6, 7};
    private static double Y[] = {7, 6, 5, 4, 3, 2, 1, 0};
    private static double Z[] = new double[15];

    @Override
    public void onCreate() {
        super.onCreate();

        // Prepare notification
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        String channelID = getString(R.string.channel_id);
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setContentTitle("Intoxecure")
                .setContentText("Accelerometer is active")
                .setSmallIcon(R.drawable.intoxecure_logo_v1)
                .setAutoCancel(false)
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        new Intent(this, MainActivity.class), 0));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            notification = nBuilder.build();
        else
            notification = nBuilder.getNotification();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        // Prepare sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ");
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
            registerListener();
            stepCurTime = System.currentTimeMillis();
            XCorr xCorr = new XCorr(8);
            xCorr.run();
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
        unregisterListener();
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case if services are bound (Bound Services).
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d("mySensor", "Accelerometer accuracy");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            acceleration = sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;
            Log.d("Accelerometer", "Acceleration:" + acceleration + "m/s^2");
        } else if (event.sensor == stepDetector) {
            stepOldTime = stepCurTime;
            stepCurTime = System.currentTimeMillis();
            Toast.makeText(this, Long.toString(stepCurTime-stepOldTime), Toast.LENGTH_SHORT).show();
        }
    }

    private void registerListener() {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterListener() {
        sensorManager.unregisterListener(this);
    }

    // Threaded fft
    private class XCorr implements Runnable{
        int N;
        double coeff;

        XCorr(int n) {
            this.N = n;
        }

        private void xCorr(double[] x, double[] y, double[] z) {
            coeff = 1/(Math.sqrt(R(x,x,0)*R(y,y,0)));
            for (int i=0; i<(2*N-1); i++) {
                z[i] = R(x,y,i-N+1)*coeff;
            }
        }

        private double R(double[] x, double[] y, int m) {
            double ret_val;

            if (m<0) {
                return R(y,x,-m);
            } else {
                ret_val = 0;
                for (int i=0; i<(N-m); i++) {
                    ret_val += x[i+m]*y[i];
                }
                return ret_val;
            }
        }

        @Override
        public void run() {
            this.xCorr(X,Y,Z);
            for (int i=0; i<15; i++) {
                Log.d("Z["+Integer.toString(i)+"]", Double.toString(Z[i]));
            }
        }
    }
}
