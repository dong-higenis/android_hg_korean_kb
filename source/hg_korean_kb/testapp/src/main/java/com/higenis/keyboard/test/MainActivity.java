package com.higenis.keyboard.test;

import android.app.Activity;
import android.os.Bundle;

/**
 * Simple field matrix for exercising HG Keyboard on Android 10.
 * Does not contain IME logic — only EditText hosts with different inputTypes.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);
    }
}
