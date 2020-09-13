/*
 * Copyright (c) 2014 - 2015 Oleksandr Tyshkovets <olexandr.tyshkovets@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gnucash.android.ui.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceFragmentCompat;

import org.gnucash.android.R;
import org.gnucash.android.app.GnuCashApplication;
import org.gnucash.android.ui.account.AccountsActivity;
import org.gnucash.android.ui.common.UxArgument;
import org.gnucash.android.ui.passcode.PasscodeLockScreenActivity;
import org.gnucash.android.ui.passcode.PasscodePreferenceActivity;

import java.util.Objects;

/**
 * Fragment for general preferences. Passcode settings, report settings, various UI settings.
 *
 * @author Oleksandr Tyshkovets <olexandr.tyshkovets@gmail.com>
 * @author Ștefan Silviu-Alexandru <stefan.silviu.alexandru@gmail.com>
 */
public class GeneralPreferenceFragment extends PreferenceFragmentCompat {
    /**
     * Request code for retrieving passcode to store
     */
    public static final int PASSCODE_REQUEST_CODE = 0x2;
    /**
     * Request code for disabling passcode
     */
    public static final int REQUEST_DISABLE_PASSCODE = 0x3;
    /**
     * Request code for changing passcode
     */
    public static final int REQUEST_CHANGE_PASSCODE = 0x4;

    private SharedPreferences.Editor mEditor;
    private CheckBoxPreference mCheckBoxPreference;

    private @NonNull AppCompatActivity getCompatActivity() {
        return ((AppCompatActivity) Objects.requireNonNull(getActivity()));
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.fragment_general_preferences);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = Objects.requireNonNull(getCompatActivity().getSupportActionBar());
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.title_general_prefs);
    }

    // In this case, commit is actually required over apply
    @SuppressLint("ApplySharedPref")
    @Override
    public void onResume() {
        super.onResume();

        mCheckBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.key_enable_passcode));
        mCheckBoxPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((Boolean) newValue) {
                final Intent intent = new Intent(getActivity(), PasscodePreferenceActivity.class);
                startActivityForResult(intent, PASSCODE_REQUEST_CODE);
            } else {
                final Intent passIntent = new Intent(getActivity(), PasscodeLockScreenActivity.class);
                passIntent.putExtra(UxArgument.DISABLE_PASSCODE, UxArgument.DISABLE_PASSCODE);
                startActivityForResult(passIntent, REQUEST_DISABLE_PASSCODE);
            }
            return true;
        });
        findPreference(getString(R.string.key_change_passcode)).setOnPreferenceClickListener(preference -> {
            startActivityForResult(new Intent(getActivity(), PasscodePreferenceActivity.class), REQUEST_CHANGE_PASSCODE);
            return true;
        });
        findPreference(getString(R.string.key_theme_option)).setOnPreferenceChangeListener((preference, newValue) -> {
            // Live-update theme
            final AppCompatDelegate delegate = getCompatActivity().getDelegate();
            delegate.setLocalNightMode(GnuCashApplication.getInstance().configureDayNight((String) newValue));
            delegate.applyDayNight();

            // Manually persist value, because we need to use commit (this avoids a race condition)
            getPreferenceManager().getSharedPreferences()
                    .edit()
                    .putString(getString(R.string.key_theme_option), (String) newValue)
                    .commit();

            // Restart the app, because the back-stack isn't getting the new theme
            final Intent restartAppIntent = new Intent(getActivity(), AccountsActivity.class);
            restartAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(restartAppIntent);

            return false;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mEditor == null) {
            mEditor = getPreferenceManager().getSharedPreferences().edit();
        }

        switch (requestCode) {
            case PASSCODE_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    mEditor.putString(UxArgument.PASSCODE, data.getStringExtra(UxArgument.PASSCODE));
                    mEditor.putBoolean(UxArgument.ENABLED_PASSCODE, true);
                    Toast.makeText(getActivity(), R.string.toast_passcode_set, Toast.LENGTH_SHORT).show();
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                    mEditor.putBoolean(UxArgument.ENABLED_PASSCODE, false);
                    mCheckBoxPreference.setChecked(false);
                }
                break;
            case REQUEST_DISABLE_PASSCODE:
                boolean flag = resultCode != Activity.RESULT_OK;
                mEditor.putBoolean(UxArgument.ENABLED_PASSCODE, flag);
                mCheckBoxPreference.setChecked(flag);
                break;
            case REQUEST_CHANGE_PASSCODE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    mEditor.putString(UxArgument.PASSCODE, data.getStringExtra(UxArgument.PASSCODE));
                    mEditor.putBoolean(UxArgument.ENABLED_PASSCODE, true);
                    Toast.makeText(getActivity(), R.string.toast_passcode_set, Toast.LENGTH_SHORT).show();
                }
                break;
        }
        mEditor.apply();
    }
}
