package xlong.ble.tool;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/27.
 */

public class DeviceListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    private List<Map> deviceList = new ArrayList<>();

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
    {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord)
        {
            // TODO Auto-generated method stub

            System.out.println("Address:" + device.getAddress());
            System.out.println("Name:" + device.getName());
            System.out.println("rssi:" + rssi);

            Map<String, String> map = new ArrayMap();
            map.put("name", device.getName() == null ? "" : device.getName());
            map.put("address", device.getAddress());
            map.put("rssi", rssi + "");

            for (Map obj : deviceList) {
                if (obj.get("address").equals(device.getAddress())) {
                    obj.put("rssi", rssi + "");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }
                    });
                    return;
                }
            }

            deviceList.add(map);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            });
        }

    };
    private BluetoothAdapter mBluetoothAdapter;

    private void startScan() {
        deviceList.clear();
        recyclerView.getAdapter().notifyDataSetChanged();
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setAdapter(new MyAdapter(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        } else {
            startScan();
        }

    }

    @Override
    protected void onDestroy() {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            startScan();
        } else if (requestCode == 210 && resultCode == Activity.RESULT_OK){
            startScan();
        }
    }

    class MyAdapter extends RecyclerView.Adapter {
        private LayoutInflater inflater;

        public MyAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

            MyViewHolder myViewHolder = new MyViewHolder(view);

            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            TextView textView = (TextView) myViewHolder.itemView;

            final Map object = deviceList.get(position);
            if (object != null) {
                String txt = "名称：" + object.get("name") + "\n" + "地址：" + object.get("address")
                        + "\nrssi:" + object.get("rssi");
                textView.setText(txt);
                final String json = object.toString();
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DeviceListActivity.this, MainActivity.class);
                        intent.putExtra("address", object.get("address").toString());
                        startActivityForResult(intent, 210);
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return deviceList.size() ;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(View itemView) {
            super(itemView);
        }
    }
}
