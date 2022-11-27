package com.sg2022.we_got_the_moves.ui.settings;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.sg2022.we_got_the_moves.R;
import com.sg2022.we_got_the_moves.db.entity.User;

import java.lang.ref.WeakReference;

public class UserDataChangeActivity extends AppCompatActivity {

  private static WeakReference<UserDataChangeActivity> weakUserDataChangeActivity;

  public static UserDataChangeActivity getInstanceActivity() {
    return weakUserDataChangeActivity.get();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.fragment_user_data_change);
    weakUserDataChangeActivity = new WeakReference<>(UserDataChangeActivity.this);

    // Back Button ends this activity -> back to Settings
    ImageButton backButton = findViewById(R.id.backButtonUserDataChange);
    backButton.setOnClickListener(v -> finish());

    // getting a SettingsViewModel for access to UserDB
    SettingsViewModel.Factory factory = new SettingsViewModel.Factory(getApplication());
    SettingsViewModel model = new ViewModelProvider(this, factory).get(SettingsViewModel.class);

    // getting the DB data and writing it as text to TextInputEditText and Radio Buttons
    model
        .repository
        .getUser()
        .observe(
            getInstanceActivity(),
            userData -> {
              ((TextInputEditText) findViewById(R.id.TextInputName)).setText(userData.name);
              ((TextInputEditText) findViewById(R.id.TextInputAge))
                  .setText(Integer.toString(userData.age));
              ((TextInputEditText) findViewById(R.id.TextInputHeight))
                  .setText(Float.toString(userData.hightInMeters));
              ((TextInputEditText) findViewById(R.id.TextInputWeight))
                  .setText(Float.toString(userData.weigthInKg));
              if (userData.isMale) {
                ((RadioButton) findViewById(R.id.maleButton)).toggle();
              } else {
                ((RadioButton) findViewById(R.id.femaleButton)).toggle();
              }
            });

    // setting up the save button
    Button saveButton = findViewById(R.id.save_button_user_data_change);
    saveButton.setOnClickListener(
        v -> {
          User newUser =
              new User(
                  ((TextInputEditText) findViewById(R.id.TextInputName))
                      .getText()
                      .toString(), // set Name
                  Float.parseFloat(
                      ((TextInputEditText) findViewById(R.id.TextInputHeight))
                          .getText()
                          .toString()), // set Height
                  Float.parseFloat(
                      ((TextInputEditText) findViewById(R.id.TextInputWeight))
                          .getText()
                          .toString()), // set Weight
                  ((RadioButton)findViewById(R.id.maleButton))
                          .isChecked(), // set Gender
                  Integer.parseInt(
                      ((TextInputEditText) findViewById(R.id.TextInputAge))
                          .getText()
                          .toString()) // set Age
                  );
          // update the user in DB and closes View
          model.repository.update(newUser);
          finish();
        });
  }
}
