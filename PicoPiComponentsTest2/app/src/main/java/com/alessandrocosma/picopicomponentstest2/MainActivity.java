package com.alessandrocosma.picopicomponentstest2;

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

    private I2cDevice mI2cDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");


        //variabile in cui salvo l'ustanza del dispositivo connesso.
        I2cDevice mI2cDevice = null;

        try {
            PeripheralManager mPeripheralManager = PeripheralManager.getInstance();
            mI2cDevice = mPeripheralManager.openI2cDevice(DEFAULT_I2C_BUS, DEFAULT_I2C_ADDRESS);
        } catch (IOException e){
            Log.e(TAG, "Unable to access I2C device", e);
        }

        /*if (mI2cDevice != null) {
            try {
                mI2cDevice.close();
            } catch (IOException e) {
                Log.w(TAG, "Unable to close I2C device", e);
            }
        }*/

        /* Metodo per chiudere il device i2c aperto. Non riconosciuto da Julia (vedi CloseResource_Test2 [2018-12-18, 16:37:57])  */
        myCloseMethod(mI2cDevice);

        MainActivity.this.finish();

    }


    private void myCloseMethod(I2cDevice mDevice){
        if (mDevice != null) {
            try {
                mDevice.close();
            } catch (IOException e) {
                Log.w(TAG, "Unable to close I2C device", e);
            }
        }
    }


    @Override
    protected void onDestroy() {

        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

}
