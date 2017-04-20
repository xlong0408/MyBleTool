package xlong.ble.tool;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {

	private final static String TAG = BluetoothLeService.class.getSimpleName();
	//���������
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;
	private int mConnectionState = STATE_DISCONNECTED;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	public static final String ACTION_GATT_CONNECTED = "xlong.ble.tool.gatt.connected";
	public static final String ACTION_GATT_DISCONNECTED = "xlong.ble.tool.gatt.disconnected";
	public static final String ACTION_GATT_SERVICES_DISCOVERED = "xlong.ble.tool.gatt.service.discovered";
    public static final String ACTION_GATT_CHARACTER_READE = "xlong.ble.tool.gatt.character.reade";
    public static final String ACTION_GATT_CHARACTER_CHANGE = "xlong.ble.tool.gatt.character.change";

    @Override
    public void onDestroy() {
		Log.i(TAG, "onDestroy");
        mBluetoothGatt.close();
        super.onDestroy();
    }


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
		{
			if (newState == BluetoothProfile.STATE_CONNECTED)
			{
				Log.i(TAG, "Connected to GATT server.");
				// Attempts to discover services after successful connection.
				Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

				sendBroadcast(new Intent(ACTION_GATT_CONNECTED));

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED)
			{
				Log.i(TAG, "Disconnected from GATT server.");
				sendBroadcast(new Intent(ACTION_GATT_DISCONNECTED));
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status)
		{
			if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "--onServicesDiscovered called(GATT_SUCCESS)--");
				sendBroadcast(new Intent(ACTION_GATT_SERVICES_DISCOVERED));
            } else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}
		}


		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic characteristic, int status)
		{
			if (status == BluetoothGatt.GATT_SUCCESS)
			{
				Log.i(TAG, "--onCharacteristicRead called--");
				byte[] value = characteristic.getValue();
				System.out.println(value.length);
				StringBuilder sb = new StringBuilder();
				for (byte b : value) {
					sb.append(b & 0xff).append(" ");
				}
				System.out.println(sb.toString());
                Intent intent = new Intent(ACTION_GATT_CHARACTER_READE);
                intent.putExtra("data", value);
                sendBroadcast(intent);
			}
			
		}

		int byteCount;
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
		{
			Log.i(TAG, "--onCharacteristicChanged called--");
            byte[] value = characteristic.getValue();
            byteCount += value.length;
            System.out.println(value.length);
            StringBuilder sb = new StringBuilder();
            for (byte b : value) {
                sb.append(b & 0xff).append(" ");
            }
            System.out.println(sb.toString());
            System.out.println(new String(characteristic.getValue()));
            System.out.println("byte count -> " + byteCount);
            Intent intent = new Intent(ACTION_GATT_CHARACTER_CHANGE);
            intent.putExtra("data", value);
            sendBroadcast(intent);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
										  BluetoothGattCharacteristic characteristic, int status)
		{
			byte[] value = characteristic.getValue();
			Log.w(TAG, "--onCharacteristicWrite-- status:" + status);
            StringBuilder sb = new StringBuilder();
            for (byte b : value) {
                sb.append(b & 0xff).append(" ");
            }
            System.out.println(sb.toString());
            System.out.println(new String(characteristic.getValue()));
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
									 BluetoothGattDescriptor descriptor, int status)
		{
			// TODO Auto-generated method stub
			// super.onDescriptorRead(gatt, descriptor, status);
			Log.w(TAG, "----onDescriptorRead status: " + status);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
									  BluetoothGattDescriptor descriptor, int status)
		{
			// TODO Auto-generated method stub
			// super.onDescriptorWrite(gatt, descriptor, status);
			Log.w(TAG, "--onDescriptorWrite-- status: " + status);
		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
		{
			// TODO Auto-generated method stub
			// super.onReadRemoteRssi(gatt, rssi, status);
			Log.w(TAG, "--onReadRemoteRssi--: " + status);
		}

		@Override
		public void onReliableWriteCompleted(BluetoothGatt gatt, int status)
		{
			// TODO Auto-generated method stub
			// super.onReliableWriteCompleted(gatt, status);
			Log.w(TAG, "--onReliableWriteCompleted--: " + status);
		}

	};

	private void broadcastUpdate(final String action)
	{
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	public void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic)
	{
	}


	public class LocalBinder extends Binder {
		public BluetoothLeService getService()
		{
			return BluetoothLeService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
        Log.i(TAG, "onUnbind");
		return super.onUnbind(intent);
	}

	private final IBinder mBinder = new LocalBinder();

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 * 
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize()
	{
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (mBluetoothManager == null)
		{
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null)
			{
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}
          
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null)
		{
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 * 
	 * @param address
	 *            The device address of the destination device.
	 * 
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	public boolean connect(final String address)
	{
		if (mBluetoothAdapter == null || address == null)
		{
			Log.w(TAG,
					"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		// Previously connected device. Try to reconnect.
		if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
				&& mBluetoothGatt != null)
		{
			Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
			if (mBluetoothGatt.connect())
			{
				mConnectionState = STATE_CONNECTING;
				return true;
			} else
			{
				return false;
			}
		}

		final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		if (device == null)
		{
			Log.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		Log.d(TAG, "Trying to create a new connection.");
		mBluetoothDeviceAddress = address;
		mConnectionState = STATE_CONNECTING;
		System.out.println("device.getBondState==" + device.getBondState());
		return true;
	}


/**
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */

	/** 
	* @Title: disconnect 
	* @Description: TODO(ȡ����������) 
	* @param   ��
	* @return void    
	* @throws 
	*/ 
	public void disconnect()
	{
		if (mBluetoothAdapter == null || mBluetoothGatt == null)
		{
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.disconnect();

	}

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	/** 
	* @Title: close 
	* @Description: TODO(�ر�������������) 
	* @param  ��
	* @return void   
	* @throws 
	*/ 
	public void close()
	{
		if (mBluetoothGatt == null)
		{
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 * 
	 * @param characteristic
	 *            The characteristic to read from.
	 */
	/** 
	* @Title: readCharacteristic 
	* @Description: TODO(��ȡ����ֵ) 
	* @param @param characteristic��Ҫ��������ֵ��
	* @return void    �������� 
	* @throws 
	*/ 
	public void readCharacteristic(BluetoothGattCharacteristic characteristic)
	{
		if (mBluetoothAdapter == null || mBluetoothGatt == null)
		{
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.readCharacteristic(characteristic);

	}

	public void writeCharacteristic(BluetoothGattCharacteristic characteristic)
	{
		if (mBluetoothAdapter == null || mBluetoothGatt == null)
		{
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.writeCharacteristic(characteristic);

	}


	public void readRssi()
	{
		if (mBluetoothAdapter == null || mBluetoothGatt == null)
		{
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.readRemoteRssi();
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 * 
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	/** 
	* @Title: setCharacteristicNotification 
	* @Description: TODO(��������ֵͨ�仯֪ͨ) 
	* @param @param characteristic������ֵ��
	* @param @param enabled ��ʹ�ܣ�   
	* @return void    
	* @throws 
	*/ 
	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled)
	{
		if (mBluetoothAdapter == null || mBluetoothGatt == null)
		{
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

//		BluetoothGattDescriptor clientConfig = characteristic
//				.getDescriptor(UUID
//						.fromString("00002902-0000-1000-8000-00805f9b34fb"));
//
//		if (enabled)
//		{
//			clientConfig
//					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//		} else
//		{
//			clientConfig
//					.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//		}
//		mBluetoothGatt.writeDescriptor(clientConfig);
	}

	/** 
	* @Title: getCharacteristicDescriptor 
	* @Description: TODO(�õ�����ֵ�µ�����ֵ) 
	* @param @param ��
	* @return void   
	* @throws 
	*/ 
	public void getCharacteristicDescriptor(BluetoothGattDescriptor descriptor)
	{
		if (mBluetoothAdapter == null || mBluetoothGatt == null)
		{
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}

		mBluetoothGatt.readDescriptor(descriptor);
	}

	/**
	 * Retrieves a list of supported GATT services on the connected device. This
	 * should be invoked only after {@code BluetoothGatt#discoverServices()}
	 * completes successfully.
	 * 
	 * @return A {@code List} of supported services.
	 */
	/** 
	* @Title: getSupportedGattServices 
	* @Description: TODO(�õ����������з���) 
	* @param @return    ��
	* @return List<BluetoothGattService>    
	* @throws 
	*/ 
	public List<BluetoothGattService> getSupportedGattServices()
	{
		if (mBluetoothGatt == null)
			return null;
		return mBluetoothGatt.getServices();
	}

}
