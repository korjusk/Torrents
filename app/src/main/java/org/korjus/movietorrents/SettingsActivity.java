package org.korjus.movietorrents;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    private static final String TAG = "u8i9 Settings";
    private RadioButton rbDefault, rbMagnet, rbShare, rbMail;
    private EditText etMail;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Load settings
        settings = getSharedPreferences("settings", 0);
        String downloadAction = settings.getString("downloadAction", "default");
        String mail = settings.getString("mailAddress", "");

        // Find views
        rbDefault = (RadioButton) findViewById(R.id.rbDefault);
        rbMagnet = (RadioButton) findViewById(R.id.rbMagnet);
        rbShare = (RadioButton) findViewById(R.id.rbShare);
        rbMail = (RadioButton) findViewById(R.id.rbMail);
        etMail = (EditText) findViewById(R.id.etMail);

        // Restore's text if something meaningful is inserted before
        if (mail.length() > 3) {
            etMail.setText(mail);
        }

        // Check's right radioButton
        switch (downloadAction) {
            case "default":
                rbDefault.setChecked(true);
                break;
            case "magnet":
                rbMagnet.setChecked(true);
                break;
            case "share":
                rbShare.setChecked(true);
                break;
            case "mail":
                rbMail.setChecked(true);
                break;
        }

        // Find's relative layouts...
        RelativeLayout rlDefault = (RelativeLayout) findViewById(R.id.rlDefault);
        RelativeLayout rlMagnet = (RelativeLayout) findViewById(R.id.rlMagnet);
        RelativeLayout rlShare = (RelativeLayout) findViewById(R.id.rlShare);
        RelativeLayout rlMail = (RelativeLayout) findViewById(R.id.rlMail);


        // ...and set's on click listener's witch simply check's/uncheck's radioButtons
        rlDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUnchecked();
                rbDefault.setChecked(true);
            }
        });

        rlMagnet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUnchecked();
                rbMagnet.setChecked(true);
            }
        });

        rlShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUnchecked();
                rbShare.setChecked(true);
            }
        });

        rlMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if phone supports this future
                if (Build.VERSION.SDK_INT <= 14) {
                    Toast.makeText(getApplication(), "This function works only with Android 4.0.3+"
                            , Toast.LENGTH_LONG).show();
                } else {
                    setUnchecked();
                    rbMail.setChecked(true);
                }
            }
        });
    }

    // Saves setting's
    @Override
    protected void onPause() {
        SharedPreferences.Editor editor = settings.edit();

        // User entered email address
        String mail = etMail.getText().toString();

        // Saves email to settings
        editor.putString("mailAddress", mail);

        // If email is not valid but mail is chosen action
        if (!isValidEmail(mail) && rbMail.isChecked()) {
            Toast.makeText(this, "Invalid email", Toast.LENGTH_LONG).show();
            // Set it back to default value
            setUnchecked();
            rbDefault.setChecked(true);
        }

        if (rbDefault.isChecked()) {
            editor.putString("downloadAction", "default");
        }

        if (rbMagnet.isChecked()) {
            editor.putString("downloadAction", "magnet");
        }

        if (rbShare.isChecked()) {
            editor.putString("downloadAction", "share");
        }

        if (rbMail.isChecked()) {
            editor.putString("downloadAction", "mail");
        }

        editor.apply();
        super.onPause();
    }

    // Uncheck's all radioButtons
    private void setUnchecked() {
        rbDefault.setChecked(false);
        rbMagnet.setChecked(false);
        rbShare.setChecked(false);
        rbMail.setChecked(false);
    }

    // Check's if email address is valid
    private boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
