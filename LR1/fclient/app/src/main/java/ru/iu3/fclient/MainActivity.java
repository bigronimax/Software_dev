package ru.iu3.fclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Arrays;

import ru.iu3.fclient.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'fclient' library on application startup.
    static {
        System.loadLibrary("fclient");
        System.loadLibrary("mbedcrypto");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int res = initRng();
        byte[] v = randomBytes(10);
        Log.d("AAA", String.valueOf(res));
        Log.d("AAA", Arrays.toString(v));

        byte[] key = new byte[16];
        byte[] data = "Blah bab".getBytes();
        byte[] encryptedData = encrypt(key, data);
        byte[] decryptedData = decrypt(key, encryptedData);

        Log.d("BBB", Arrays.toString(key));
        Log.d("BBB", Arrays.toString(data));
        Log.d("BBB", Arrays.toString(encryptedData));
        Log.d("BBB", Arrays.toString(decryptedData));

        TextView testText = binding.test;
        testText.setText(stringFromJNI());

    }

    /**
     * A native method that is implemented by the 'fclient' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public static native int initRng();
    public static native byte[] randomBytes(int no);
    public static native byte[] encrypt(byte[] key, byte[] data);
    public static native byte[] decrypt(byte[] key, byte[] data);
}