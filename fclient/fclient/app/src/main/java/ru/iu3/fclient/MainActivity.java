package ru.iu3.fclient;

import static androidx.core.database.sqlite.SQLiteDatabaseKt.transaction;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;

import ru.iu3.fclient.databinding.ActivityMainBinding;

interface TransactionEvents {
    String enterPin(int ptc, String amount);
    void transactionResult(boolean result);
}

public class MainActivity extends AppCompatActivity implements TransactionEvents {

    // Used to load the 'fclient' library on application startup.
    static {
        System.loadLibrary("fclient");
        System.loadLibrary("mbedcrypto");
    }

    private ActivityMainBinding binding;
    ActivityResultLauncher activityResultLauncher;
    private String pin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        encryptionTest();
        //transactionTest();

    }

    public static byte[] stringToHex(String s) {
        byte[] hex;
        try {
            hex = Hex.decodeHex(s.toCharArray());
        } catch (DecoderException ex) {
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
                Log.println(Log.ERROR, "pin", ex.getMessage());
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
    /*protected String getPageTitle(String html) {
        Pattern pattern = Pattern.compile("<title>(.+?)</title>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        String p;
        if (matcher.find()) {
            p = matcher.group(1);
        } else {
            p = "Not found";
        }

        return p;
    }*/

    // Метод, который тестирует работу http клиента
    /*protected void testHttpClient() {
        new Thread(() -> {
            try {
                HttpURLConnection uc = (HttpURLConnection) (new URL("http://10.0.2.2:8080/api/v1/title").openConnection());
                InputStream inputStream = uc.getInputStream();

                String html = IOUtils.toString(inputStream);
                String title = getPageTitle(html);

                runOnUiThread(() -> {
                    Toast.makeText(this, title, Toast.LENGTH_LONG).show();
                });
            } catch (Exception exception) {
                Log.e("fapptag", "Http client fails", exception);
            }
        }).start();
    }*/

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Button myButton = (Button) findViewById(R.id.sample_button);
    }

    // Альтернативный метод нажатия кнопки
    public void onButtonClick(View view) {
        testHttpClient();
    }*/

    private void transactionTest() {
        binding.test.setVisibility(View.GONE);
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

                        //Toast.makeText(MainActivity.this, pin, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    private void encryptionTest() {
        binding.btn.setVisibility(View.GONE);
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
    public native boolean transaction(byte[] trd);
}