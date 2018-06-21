package com.example.ashsaccount.safewordproject;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class InputPasswordActivity extends AppCompatActivity {
//variables that are static are referenced in another activity.

    private PatternLockView mPatternLockView;
    private String password;
    ImageView fingerImage;
public static boolean adapterType;
    ////////FINGERPRINT STUFF\\\\\\\\\\\\\\
    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    static int pos;
    /////////////////////////////////////////

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_input_password);
        Intent intentRecovery = getIntent();

        final int position = intentRecovery.getExtras().getInt("position");
        pos = position;

        adapterType= intentRecovery.getExtras().getBoolean("adapterType");
        fingerPrintOnCreate();

        final SharedPreferences preferences = getSharedPreferences("PREFS", 0);
        password = preferences.getString("password", "0");
        if(password.equals("0")){
            Intent intent = new Intent(getApplicationContext(), CreatePasswordActivity.class);
            intent.putExtra("position", position);
            InputPasswordActivity.this.startActivity(intent);
            finish();
        }



        mPatternLockView = (PatternLockView) findViewById(R.id.pattern_lock_view);
        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {


            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                if (password.equals(PatternLockUtils.patternToString(mPatternLockView, pattern))) {


                    success(position);
                } else {

                    Toast.makeText(InputPasswordActivity.this, "Wrong Pattern!", Toast.LENGTH_SHORT).show();
                    mPatternLockView.clearPattern();
                }

            }

            @Override
            public void onCleared() {

            }
        });
    }


    ///FINGERPRINT METHODS\\\\

    private void fingerPrintOnCreate() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager =
                    (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager =
                    (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

            fingerImage = (ImageView) findViewById(R.id.fingerImage);

            //Check whether the device has a fingerprint sensor//
            if (!fingerprintManager.isHardwareDetected()) {
                fingerImage.setVisibility(View.GONE);

            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                fingerImage.setVisibility(View.GONE);
            }

            if (!fingerprintManager.hasEnrolledFingerprints()) {
                fingerImage.setVisibility(View.GONE);
            }

            if (!keyguardManager.isKeyguardSecure()) {
                fingerImage.setVisibility(View.GONE);
            } else {
                try {
                    generateKey();
                } catch (FingerprintException e) {
                    e.printStackTrace();
                }

                if (initCipher()) {
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);


                    FingerprintHandler helper = new FingerprintHandler(this);
                    helper.startAuth(fingerprintManager, cryptoObject);
                }
            }
        }
    }


    public boolean initCipher() {
        if (fingerprintManager.hasEnrolledFingerprints()) {
            try {
                cipher = Cipher.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            } catch (NoSuchAlgorithmException |
                    NoSuchPaddingException e) {
                throw new RuntimeException("Failed to get Cipher", e);
            }

            try {
                keyStore.load(null);
                SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                        null);
                cipher.init(Cipher.ENCRYPT_MODE, key);
                //Return true if the cipher has been initialized successfully//
                return true;
            } catch (KeyPermanentlyInvalidatedException e) {

                //Return false if cipher initialization failed//
                return false;
            } catch (KeyStoreException | CertificateException
                    | UnrecoverableKeyException | IOException
                    | NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException("Failed to init Cipher", e);
            }
        }
        return false;
    }

    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }

    private void generateKey() throws FingerprintException {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyStore.load(null);

            keyGenerator.init(new

                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }
    }


    public void success(int position) {
        RowData row;
        if(adapterType==false) {
     row = MainActivity.customAdapter.getSingleItem(position);
    row.setLock(false);

    MainActivity.customAdapter.setSingleItem(row, position);
}else{
     row = MainActivity.gridViewAdapter.getSingleItem(position);
    row.setLock(false);
    MainActivity.gridViewAdapter.setSingleItem(row,position);

}
       MainActivity.updateItem(row.getFileID(), row.getText(), row.getPhotoUrl(), row.getLock());
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("adapterType",adapterType);

        InputPasswordActivity.this.startActivity(intent);
        finish();
    }


}


