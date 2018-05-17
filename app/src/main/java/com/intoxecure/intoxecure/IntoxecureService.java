package com.intoxecure.intoxecure;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import java.util.LinkedList;
import java.util.ListIterator;
import java.lang.Math;

public class IntoxecureService extends Service implements SensorEventListener {
    static final String SENT_BROADCAST = "SMS_SENT";
    static final String DELIVERED_BROADCAST = "SMS_DELIVERED";
    private static final String LOG_TAG = "ForegroundService";
    public static boolean IS_SERVICE_RUNNING = false;
    private static Notification notification;
    private static SensorManager sensorManager;
    private static Sensor accelerometer;
    public static double acceleration;
    private static long count = 0;
    private static long countTime;
    private static StepDetector accelStepDetector = new StepDetector();
    private static LinkedList<Long> countTimeDelta = new LinkedList<>();
    private static double sigmaDeltaTime;
    static double sigmaDeltaTimeAlpha = 0.5;
    private static double expMean, expMeanAlpha = 0.125;
    private static int fault = 0;
    private static PendingIntent pendSend, pendDeliver;
    private static SmsManager sms;
    private static ContactList contactList;
    static boolean smsEnabled;
    static float sensorSensitivity;
    private static String username;


    private static int threshold;
    private static WeightedAverage Ave;
    private static double aveTime;
    private static final double tuning = 0.2;
    private static final double gamma = 0.2;
    private static final int frameSize = 10;
    private static LinkedList<Double> movingAverage = new LinkedList<>();
    private static LinkedList<Long> movingSamples = new LinkedList<>();


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
            //noinspection deprecation
            notification = nBuilder.getNotification();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }



        // Prepare sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // sms manager
        registerReceiver(sentReceiver, new IntentFilter(SENT_BROADCAST));
        registerReceiver(deliveredReceiver, new IntentFilter(DELIVERED_BROADCAST));

        pendSend = PendingIntent.getBroadcast(this, 0, new Intent(SENT_BROADCAST), 0);
        pendDeliver = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED_BROADCAST), 0);

        sms = SmsManager.getDefault();

        // contact
        contactList = new ContactList(this, false);


        // Prepare external classes and tuning parameter
        Ave = new WeightedAverage();
        for(int i = 0; i<frameSize; i++) {
                movingAverage.add(0.0);
                movingSamples.add((long)0);
        }

        // Get username from default shared preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String user = preferences.getString("username","Batman");
        username = user.equals("")? "Batman" : user;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
                Log.i(LOG_TAG, "Received Start Foreground Intent ");
                Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
                startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
                registerListener();
            } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
                Log.i(LOG_TAG, "Received Stop Foreground Intent");
                stopForeground(true);
                stopSelf();
            }
        }
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
        unregisterListener();
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
        unregisterReceiver(sentReceiver);
        unregisterReceiver(deliveredReceiver);
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
            acceleration = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
            //Log.d("Accelerometer", "Acceleration:" + acceleration + "m/s^2");

            long countTemp = accelStepDetector.Iterate(acceleration);
            long countTimeTemp = event.timestamp;
            if (count != countTemp) {
                count = countTemp;

                long timeDeltaTemp = countTimeTemp-countTime;
                //computeAverage(timeDeltaTemp);
                //stdDevMethod(timeDeltaTemp);
                if (countTimeDelta.size() == 1)
                    expMean = timeDeltaTemp;
                else if (countTimeDelta.size() > 1)
                    expMean = expMean*(1-expMeanAlpha) + timeDeltaTemp*expMeanAlpha;

                if (timeDeltaTemp > expMean + sigmaDeltaTime/sigmaDeltaTimeAlpha || timeDeltaTemp < expMean - sigmaDeltaTime/sigmaDeltaTimeAlpha)
                    fault += 1;
                if (timeDeltaTemp > 10e9)
                    fault = 0;

                if (fault > 10) {
                    Log.d("smsEnabled", Boolean.toString(smsEnabled));
                    if (smsEnabled) {
                        for (String aContactNo : contactList.contactNo)
                            sms.sendTextMessage(aContactNo,
                                    null, "Your friend, " + username + ", has " +
                                            "an abnormal walk pattern and may be drunk. " +
                                            "Maybe you should check on him on this address",
                                    pendSend,
                                    pendDeliver);
                        Log.d("sms", "sms send attmepted");
                    }
                    Log.d("Lasing ka pre", "UWI NA UY");
                    fault = 0;
                }

                // Add new time delta, and recompute for standard dev
                while (countTimeDelta.size() >= 25) {
                    countTimeDelta.poll();
                }
                countTimeDelta.offer(countTimeTemp-countTime);
                countTime = countTimeTemp;
                ListIterator<Long> iterator = countTimeDelta.listIterator();
                double mean = 0;
                while (iterator.hasNext())
                    mean += iterator.next();
                mean /= countTimeDelta.size();
                iterator = countTimeDelta.listIterator();
                sigmaDeltaTime = 0;
                while (iterator.hasNext())
                    sigmaDeltaTime += Math.pow(iterator.next(),2);
                sigmaDeltaTime = Math.sqrt(sigmaDeltaTime/countTimeDelta.size() - Math.pow(mean,2));

                Log.d("count", Long.toString(count));
                Log.d("fault", Integer.toString(fault));
                Log.d("average", Double.toString(expMean));
                Log.d("std dev", Double.toString(sigmaDeltaTime));
                Log.d("timeDeltaTemp", Long.toString(timeDeltaTemp));
                Log.d("array size", Integer.toString(countTimeDelta.size()));
            }
        }
    }

    private void registerListener() {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterListener() {
        sensorManager.unregisterListener(this);
    }

    BroadcastReceiver sentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS sent", Toast.LENGTH_LONG).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "General Failure", Toast.LENGTH_LONG).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "No Service", Toast.LENGTH_LONG).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "Null PDU", Toast.LENGTH_LONG).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "Radio off", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
    BroadcastReceiver deliveredReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS delivered", Toast.LENGTH_LONG).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(context, "SMS was not delivered", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private synchronized void computeAverage(long timeDeltaTemp) {
        double stdDev = computeStdDev();
        if((timeDeltaTemp) >= 2e9)
            threshold = 0;

        aveTime = Ave.compute(timeDeltaTemp, tuning);
        movingAverage.removeFirst();
        movingAverage.add(aveTime);
        //Test threshold
        for (Double number : movingAverage) {
            //Toast.makeText(this, Double.toString(number), Toast.LENGTH_SHORT).show();
            Log.i("EWMA", Double.toString(number));
            if((number <= ((1-gamma)*stdDev)) || (number >= ((1+gamma)*stdDev))){
                threshold++;
            }
        }

        if (threshold>=10){
            //send message
            Toast.makeText(this, "STEP ERROR DETECTED", Toast.LENGTH_LONG).show();
            threshold = 0;
        }

    }

    private static double computeStdDev() {
        double sum = 0;
        double standardDev = 0;
        for (double num : movingAverage) {
            sum += num;
        }

        double mean = sum / (movingAverage.size());

        for (double num : movingAverage) {
            standardDev += Math.pow((num - mean), 2);
        }
        return Math.sqrt(standardDev / (movingAverage.size()));
    }
/*
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_key_enable_sms")) {
            smsEnabled = sharedPreferences.getBoolean("pref_key_enable_sms", false);
            Log.d("smsEnabled", Boolean.toString(smsEnabled));
        } else if (key.equals("pref_key_sensor_sensitivity")) {
            sensorSensitivity = sharedPreferences.getFloat("pref_key_sensor_sensitivity", 0);
            sigmaDeltaTimeAlpha = sensorSensitivity*50;
            Log.d("sensitivity", Float.toString(sensorSensitivity));
        }
    }
*/
    public double stdDevMethod(long timeDeltaTemp){
        movingSamples.removeFirst();
        movingSamples.addLast(timeDeltaTemp);

        //Standard deviation computation
        long sum = 0;
        double Dev = 0;
        for (long num : movingSamples) {
            sum += num;
        }

        double mean = sum / frameSize;

        for (long num : movingSamples) {
            Dev += Math.pow((num - mean), 2);
        }
        double standardDev = Math.sqrt(Dev / frameSize);

        Log.d("Standard Dev Method:", Double.toString(standardDev));

        ListIterator<Long> iterator = movingSamples.listIterator();
        while (iterator.hasNext()) {
            Log.d("movingSamples[" + Integer.toString(iterator.nextIndex()) + "]", Long.toString(iterator.next()));
        }

        return standardDev;
    }

}
