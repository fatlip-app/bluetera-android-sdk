package com.ioteratech.example;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.ioteratech.blueterasdk.BlueteraCallbacks;
import com.ioteratech.blueterasdk.BlueteraManager;
import com.ioteratech.blueterasdk.BlueteraScanCallback;
import com.ioteratech.blueterasdk.BlueteraScanner;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import bluetera.BlueteraMessages;

public class MainActivity extends AppCompatActivity implements BlueteraCallbacks {

    private final String mTag = getClass().getSimpleName();

    private BlueteraManager mBlueteraManager;
    private final BlueteraScanner mScanner = new BlueteraScanner();

    private boolean mFoundDevice = false;
    private Button mConnectButton;
    private Button mDisconnectButton;
    private TextView mStatusTextView;

    private BluetoothDevice mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(mTag, "need to request permissions");
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION}, 1234);
        } else {
            Log.d(mTag, "permissions granted");
        }

        mBlueteraManager = new BlueteraManager(getApplication());
        mBlueteraManager.setGattCallbacks(this);

        mConnectButton = findViewById(R.id.connectButton);
        mDisconnectButton = findViewById(R.id.disconnectButton);
        mStatusTextView = findViewById(R.id.statusViewText);


        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScanner.startScan(new BlueteraScanCallback() {
                    @Override
                    public void onScanResult(BluetoothDevice device) {
                        if (!mFoundDevice) {
                            mFoundDevice = true;

                            Log.d(mTag, "found: " + device.getAddress());
                            mDevice = device;

                            mBlueteraManager.connect(mDevice)
                                    .retry(3, 100)
                                    .useAutoConnect(false)
                                    .enqueue();
                        }
                    }
                });
            }
        });


        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public void onUARTData(BluetoothDevice device, byte[] data) {
        BlueteraMessages.DownlinkMessage msg = null;
        ByteArrayInputStream stream = new ByteArrayInputStream(data);

        try {
            msg = BlueteraMessages.DownlinkMessage.parseDelimitedFrom(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(msg.getPayloadCase().getNumber() == BlueteraMessages.DownlinkMessage.QUATERNION_FIELD_NUMBER)  {
            BlueteraMessages.ImuQuaternionPayload quat = msg.getQuaternion();
            Log.d(mTag, quat.getW() + ", " + quat.getX() + ", " + quat.getY() + ", " + quat.getZ());
        }

        //Log.d(mTag, "data: " + data.length);
    }

    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onLinkLossOccurred(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {

    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {
        Log.d(mTag, "device ready");
        mStatusTextView.setText("Device Ready");
        mDisconnectButton.setEnabled(true);

        BlueteraMessages.ImuStart imuStart = BlueteraMessages.ImuStart.newBuilder()
                .setAccFsr(4)
                .setGyroFsr(500)
                .setOdr(50)
                .setDataTypes(BlueteraMessages.ImuDataType.QUATERNION_VALUE)
                .build();

        BlueteraMessages.UplinkMessage msg = BlueteraMessages.UplinkMessage.newBuilder()
                .setImu(BlueteraMessages.ImuCommand.newBuilder()
                        .setStart(imuStart))
                .build();


        //byte[] pkt = msg.toByteArray();

//        for(int i = 0; i < pkt.length / 2; i++)
//        {
//            byte temp = pkt[i];
//            pkt[i] = pkt[pkt.length - i - 1];
//            pkt[pkt.length - i - 1] = temp;
//        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            msg.writeDelimitedTo(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mBlueteraManager.sendUart(stream.toByteArray());
    }

    @Override
    public void onBondingRequired(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onBonded(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onBondingFailed(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {

    }

    @Override
    public void onDeviceNotSupported(@NonNull BluetoothDevice device) {

    }
}
