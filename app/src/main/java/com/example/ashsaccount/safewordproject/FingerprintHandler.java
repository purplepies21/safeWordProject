package com.example.ashsaccount.safewordproject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;


public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {


    private CancellationSignal cancellationSignal;
    private Context context;

    public FingerprintHandler(Context mContext) {
        context = mContext;
    }


    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {

        cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override

    public void onAuthenticationError(int errMsgId, CharSequence errString) {


    }

    @Override


    public void onAuthenticationFailed() {
        Toast.makeText(context, "Authentication failed", Toast.LENGTH_LONG).show();
    }

    @Override


    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        Toast.makeText(context, "Authentication help\n" + helpString, Toast.LENGTH_LONG).show();
    }@Override

    public void onAuthenticationSucceeded(
            FingerprintManager.AuthenticationResult result) {

        Toast.makeText(context, "Success!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(context, FileActivity.class);
        intent.putExtra("position", InputPasswordActivity.pos);

        intent.putExtra("adapterType",InputPasswordActivity.adapterType);
        boolean adapterType= InputPasswordActivity.adapterType;
        int position= InputPasswordActivity.pos;
        RowData row;
        if(adapterType==false) {
            row = MainActivity.customAdapter.getSingleItem(position);
            row.setLock(false);

            MainActivity.customAdapter.setSingleItem(row, position);
        }else {
            row = MainActivity.gridViewAdapter.getSingleItem(position);
            row.setLock(false);
            MainActivity.gridViewAdapter.setSingleItem(row, position);
        }
            MainActivity.updateItem(row.getFileID(), row.getText(), row.getPhotoUrl(), row.getLock());

        context.startActivity(intent);


    }


}
