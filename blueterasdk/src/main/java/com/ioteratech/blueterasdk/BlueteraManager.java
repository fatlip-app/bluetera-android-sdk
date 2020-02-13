/*
	MIT License

	Copyright (c) 2019 Tensor Technologies LTD

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
*/

package com.ioteratech.blueterasdk;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.ioteratech.blueterasdk.callbacks.UARTDataCallback;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;

public class BlueteraManager extends BleManager<BlueteraCallbacks> {
    private final static UUID UART_UUID_SERVICE = UUID.fromString("e2530001-9ba2-4913-9cd4-ce6ee7b579e8");
    private final static UUID UART_UUID_RX_CHAR = UUID.fromString("e2530002-9ba2-4913-9cd4-ce6ee7b579e8");
    private final static UUID UART_UUID_TX_CHAR = UUID.fromString("e2530003-9ba2-4913-9cd4-ce6ee7b579e8");

    private BluetoothGattCharacteristic mRXCharacteristic;
    private BluetoothGattCharacteristic mTXCharacteristic;
    private boolean mSupported;

    private final String mTag = getClass().getName();

    public BlueteraManager(final Context context) {
        super(context);
    }

    @Override
    protected BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    @Override
    protected boolean shouldClearCacheWhenDisconnected() {
        return !mSupported;
    }

    private	final UARTDataCallback mUARTDataCallback = new UARTDataCallback() {
        @Override
        public void onUARTData(final BluetoothDevice device, final byte[] data) {
            mCallbacks.onUARTData(device, data);
        }
    };

    /**
     * BluetoothGatt callbacks object.
     */
    private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {
        @Override
        protected void initialize() {
            setNotificationCallback(mTXCharacteristic).with(mUARTDataCallback);
        }

        @Override
        public boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
            final BluetoothGattService uart_service = gatt.getService(UART_UUID_SERVICE);

            if(uart_service != null) {
                mRXCharacteristic = uart_service.getCharacteristic(UART_UUID_RX_CHAR);
                mTXCharacteristic = uart_service.getCharacteristic(UART_UUID_TX_CHAR);
                enableNotifications(mTXCharacteristic).enqueue();
            }



            mSupported = mRXCharacteristic != null && mTXCharacteristic != null;

            return mSupported;
        }

        @Override
        protected void onDeviceDisconnected() {
            mRXCharacteristic = null;
            mTXCharacteristic = null;
        }
    };

    public void sendUart(final byte[] data) {
        if (mRXCharacteristic == null)
            return;

        writeCharacteristic(mRXCharacteristic, data).enqueue();
    }
}
