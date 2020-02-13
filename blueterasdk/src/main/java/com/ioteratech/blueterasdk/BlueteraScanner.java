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
import android.util.Log;
import android.util.SparseArray;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class BlueteraScanner {

    private BluetoothLeScannerCompat _scanner;
    private BlueteraScanCallback _callback;
    private final String mTag = getClass().getSimpleName();

    public BlueteraScanner() {
        _scanner = BluetoothLeScannerCompat.getScanner();
    }

    public void startScan(BlueteraScanCallback callback) {
        _callback = callback;
        /*
        ScanSettings settings = new ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000)
                .setUseHardwareBatchingIfSupported(false)
                .build();

        List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(mUuid).build());

        */
        _scanner.startScan(_scan_callback);
    }

    public void stopScan(BlueteraScanCallback callback) {
        _callback = callback;
        _scanner.stopScan(_scan_callback);
    }

    private ScanCallback _scan_callback = new ScanCallback() {
        private final String mTag = getClass().getSimpleName();

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice d = result.getDevice();

            if (d.getName() != null && d.getName().equals("BlueTera")) {
                _callback.onScanResult(d);
            }
        }
    };
}
