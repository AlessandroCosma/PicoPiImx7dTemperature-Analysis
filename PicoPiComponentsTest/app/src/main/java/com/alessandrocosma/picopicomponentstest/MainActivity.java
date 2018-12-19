package com.alessandrocosma.picopicomponentstest;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // stringa che mi identifica il bus I2C del PicoPiImx7d
    private static final String DEFAULT_I2C_BUS = "I2C1";

    //indirizzo slave di default del sensore Bmx280
    private static final int DEFAULT_I2C_ADDRESS = 0x77;

    //variabile in cui salvo l'ustanza del dispositivo connesso.
    private I2cDevice mI2cDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");


        //Apertura di una connessione con una componente I2C senza l'utilizzo degli user driver
        try {
            PeripheralManager mPeripheralManager = PeripheralManager.getInstance();
            mI2cDevice = mPeripheralManager.openI2cDevice(DEFAULT_I2C_BUS, DEFAULT_I2C_ADDRESS);

            //utilizzo fittizio dell oggetto mI2cDevice
            mI2cDevice.getName();

        } catch (IOException e){
            Log.e(TAG, "Unable to access I2C device", e);
        }



        Log.d(TAG, "Chiamo MainActivity.this.finish()");
        MainActivity.this.finish();

    }


    @Override
    protected void onDestroy() {

        if (mI2cDevice != null) {
            try {
                mI2cDevice.close();
            } catch (IOException e) {
                Log.w(TAG, "Unable to close I2C device", e);
            }
        }

        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

}
