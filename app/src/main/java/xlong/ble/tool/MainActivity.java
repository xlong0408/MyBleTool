package xlong.ble.tool;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;

import static xlong.ble.tool.BluetoothLeService.ACTION_GATT_CHARACTER_CHANGE;
import static xlong.ble.tool.BluetoothLeService.ACTION_GATT_CHARACTER_READE;
import static xlong.ble.tool.BluetoothLeService.ACTION_GATT_CONNECTED;
import static xlong.ble.tool.BluetoothLeService.ACTION_GATT_DISCONNECTED;
import static xlong.ble.tool.BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private String mDeviceAddress;
    public static BluetoothLeService bluetoothLeService;
    private TextView mConnection, mText;
    private LinearLayout mContainer;

    private void findView() {
        mConnection = (TextView) findViewById(R.id.connection);
        mText = (TextView) findViewById(R.id.text);
        mContainer = (LinearLayout) findViewById(R.id.container);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        mDeviceAddress = getIntent().getStringExtra("address");
        Intent intent = new Intent(MainActivity.this, BluetoothLeService.class);
        intent.putExtra("address", mDeviceAddress);
        bindService(intent, new MyServiceConnection(), BIND_AUTO_CREATE);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_GATT_CONNECTED:
                    mConnection.setText("已连接");

                    Log.i(TAG, "gatt connected");
                    break;
                case ACTION_GATT_DISCONNECTED:
                    mConnection.setText("已断开");
                    Log.i(TAG, "gatt disconnected");

                    break;
                case ACTION_GATT_SERVICES_DISCOVERED:
                    Log.i(TAG, "gatt services discovered");
                    displayServices(bluetoothLeService.getSupportedGattServices());
                    break;
                case ACTION_GATT_CHARACTER_READE:
                    byte[] data = intent.getByteArrayExtra("data");

                    String json = new String(data);
                    System.out.println(json);
//                    new AlertDialog.Builder(MainActivity.this).setMessage("目标设备：" + new String(model) + "\n当前电量：" + battery + "%").create().show();
                    break;
                case ACTION_GATT_CHARACTER_CHANGE:

                    byte[] data1 = intent.getByteArrayExtra("data");
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(mText.getText()).append(new String(data1));
                    mText.setText(stringBuilder.toString());
                    break;
            }
        }
    };

    private void displayServices(List<BluetoothGattService> services) {
        BluetoothGattCharacteristic target = null;
        for (BluetoothGattService service : services) {
            TextView textView = new TextView(this);
            StringBuilder sb = new StringBuilder();
            sb.append("service:" + service.getUuid() + "\n---------characters:\n");
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                sb.append(characteristic.getUuid() + "\n");
                if (characteristic.getUuid().equals(UUID.fromString("0000fab2-0000-1000-8000-00805f9b34fb"))) {
                    target = characteristic;
                }
            }
            textView.setText(sb.toString());
            mContainer.addView(textView);
        }
        if (target == null) return;

        bluetoothLeService.setCharacteristicNotification(target, true);
//        bluetoothLeService.readCharacteristic(target);
        final BluetoothGattCharacteristic finalTarget = target;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finalTarget.setValue(new byte[]{0x09, 0x08, 0x07, 0x06});
               bluetoothLeService.writeCharacteristic(finalTarget);
            }
        }, 100);
    }

    private IntentFilter makeIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_GATT_CONNECTED);
        filter.addAction(ACTION_GATT_DISCONNECTED);
        filter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(ACTION_GATT_CHARACTER_READE);
        filter.addAction(ACTION_GATT_CHARACTER_CHANGE);
        return filter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, makeIntentFilter());
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(Activity.RESULT_OK);
        }
        return super.onKeyDown(keyCode, event);
    }

    class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            // 根据蓝牙地址，连接设备
            bluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothLeService = null;
        }
    }
}
