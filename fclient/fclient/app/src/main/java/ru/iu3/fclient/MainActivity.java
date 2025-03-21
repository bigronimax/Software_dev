package ru.iu3.fclient;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.iu3.fclient.databinding.ActivityMainBinding;

interface TransactionEvents {
    String enterPin(int ptc, String amount);
    void transactionResult(boolean result);
}

public class MainActivity extends AppCompatActivity implements TransactionEvents {

    static {
        System.loadLibrary("fclient");
        System.loadLibrary("mbedcrypto");
    }

    private ActivityMainBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    private String pin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int res = initRng();

        encryptionTest(res);
        transactionTest();
        httpTest();

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        //String pin = data.getStringExtra("pin");
                        assert data != null;
                        pin = data.getStringExtra("pin");
                        synchronized (MainActivity.this) {
                            MainActivity.this.notifyAll();
                        }
                    }
                }
        );
    }

    public static byte[] stringToHex(String s){
        byte[] hex;
        try{
            hex = Hex.decodeHex(s.toCharArray());
        } catch (DecoderException e) {
            hex = null;
        }
        return hex;
    }

    @Override
    public String enterPin(int ptc, String amount) {
        pin = "";
        Intent it = new Intent(MainActivity.this, PinpadActivity.class);
        it.putExtra("ptc", ptc);
        it.putExtra("amount", amount);
        synchronized (MainActivity.this) {
            activityResultLauncher.launch(it);
            try {
                MainActivity.this.wait();
            } catch (Exception ex) {
                Log.println(Log.ERROR, "MainActivity.enterPin", ex.getMessage());
            }
        }
        return pin;
    }

    @Override
    public void transactionResult(boolean result) {
        runOnUiThread(()-> {
            Toast.makeText(MainActivity.this, result ? "ok" : "failed", Toast.LENGTH_SHORT).show();
        });
    }

    // Метод, который возвращает название сайта
    protected String getPageTitle(String html) {
        Pattern pattern = Pattern.compile("<title>(.+?)</title>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        String p;
        if (matcher.find()) {
            p = matcher.group(1);
        } else {
            p = "Not found";
        }

        return p;
    }

    // Метод, который тестирует работу http клиента
    protected void testHttpClient() {
        new Thread(() -> {
            try {
                // HttpURLConnection uc = (HttpURLConnection) (new URL("http://10.0.2.2:8081/api/v1/title").openConnection());
                HttpURLConnection uc = (HttpURLConnection) (new URL("http://192.168.15.37:8081/api/v1/title").openConnection());
                Log.d("AAA", uc.toString());
                InputStream inputStream = uc.getInputStream();
                Log.d("AAA", inputStream.toString());
                String html = IOUtils.toString(inputStream);
                Log.d("AAA", html);
                String title = getPageTitle(html);
                Log.d("AAA", title);
                runOnUiThread(() -> {
                    Toast.makeText(this, title, Toast.LENGTH_LONG).show();
                });
            } catch (Exception exception) {
                Log.e("fapptag", "Http client fails", exception);
            }
        }).start();
    }

    private void httpTest() {
        binding.http.setOnClickListener(view -> {
            testHttpClient();
        });
    }

    private void transactionTest() {
        binding.btn.setOnClickListener(view -> {
            new Thread(() -> {
                try {
                    byte[] trd = stringToHex("9F0206000000000100");
                    transaction(trd);

                } catch (Exception ex) {
                    Log.e("MainActivity.transaction", ex.getMessage());
                }
            }).start();
        });
    }
    private void encryptionTest(int res) {
        byte[] v = randomBytes(16);
        Log.d("ENCRYPTION", String.valueOf(res));
        Log.d("ENCRYPTION", Arrays.toString(v));

        byte[] key = new byte[16];
        byte[] data = "Blah bab".getBytes();
        byte[] encryptedData = encrypt(key, data);
        byte[] decryptedData = decrypt(key, encryptedData);

        Log.d("ENCRYPTION", Arrays.toString(key));
        Log.d("ENCRYPTION", Arrays.toString(data));
        Log.d("ENCRYPTION", Arrays.toString(encryptedData));
        Log.d("ENCRYPTION", Arrays.toString(decryptedData));
    }
    public native String stringFromJNI();
    public static native int initRng();
    public static native byte[] randomBytes(int no);
    public static native byte[] encrypt(byte[] key, byte[] data);
    public static native byte[] decrypt(byte[] key, byte[] data);
    public native boolean transaction(byte[] trd);
}