package jameyeless.intoxecure;


import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// Source: https://www.programcreek.com/java-api-examples/?code=if710/2017.2-codigo/2017.2-codigo-master/2017-09-13/Managers/app/src/main/java/br/ufpe/cin/if710/managers/phonesms/SmsSendDirectActivity.java
public class MainActivity extends AppCompatActivity {
    static final String SENT_BROADCAST = "SMS_SENT";
    static final String DELIVERED_BROADCAST = "SMS_DELIVERED";
    Button sendBtn;
    EditText txtphoneNo;
    EditText txtMessage;
    String phoneNo;
    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            sendBtn = findViewById(R.id.button);
            txtphoneNo = findViewById(R.id.editText2);
            txtMessage = findViewById(R.id.editText);
        } else {
            Toast.makeText(this,"Grant permission in settings", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void sendSMS(View view) {
        phoneNo = txtphoneNo.getText().toString();
        message = txtMessage.getText().toString();

        registerReceiver(sentReceiver, new IntentFilter(SENT_BROADCAST));
        registerReceiver(deliveredReceiver, new IntentFilter(DELIVERED_BROADCAST));

        PendingIntent pendSend   = PendingIntent.getBroadcast(this,0,new Intent(SENT_BROADCAST),0);
        PendingIntent pendDeliver = PendingIntent.getBroadcast(this,0,new Intent(DELIVERED_BROADCAST),0);

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNo, null, message, pendSend, pendDeliver);
    }

    BroadcastReceiver sentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(getBaseContext(), "General Failure", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(getBaseContext(), "No Service", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                    break;
            }

            unregisterReceiver(this);
        }
    };
    BroadcastReceiver deliveredReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(getBaseContext(), "SMS was not delivered", Toast.LENGTH_SHORT).show();
                    break;
            }
            unregisterReceiver(this);
        }
    };

}
