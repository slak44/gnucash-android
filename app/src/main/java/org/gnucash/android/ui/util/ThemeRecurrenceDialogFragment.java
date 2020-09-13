/*
 * Copyright (c) 2020 Ștefan Silviu-Alexandru <stefan.silviu.alexandru@gmail.com>
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

package org.gnucash.android.ui.util;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;

import com.codetroopers.betterpickers.recurrencepicker.RecurrencePickerDialogFragment;

import org.gnucash.android.R;

import java.util.Objects;

/**
 * Hack the external {@link RecurrencePickerDialogFragment} to support DayNight, by intercepting {@link #onCreateView}.
 *
 * @author Ștefan Silviu-Alexandru <stefan.silviu.alexandru@gmail.com>
 */
public class ThemeRecurrenceDialogFragment extends RecurrencePickerDialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup root = (ViewGroup) Objects.requireNonNull(super.onCreateView(inflater, container, savedInstanceState));

        final int bgColor = AttrUtil.getBackground(root.getContext());

        final AppCompatSpinner freqSpinner = root.findViewById(com.codetroopers.betterpickers.R.id.freqSpinner);
        @SuppressWarnings("unchecked") final ArrayAdapter<CharSequence> freqAdapter = (ArrayAdapter<CharSequence>) freqSpinner.getAdapter();
        freqAdapter.setDropDownViewResource(R.layout.recurrencepicker_freq_item_override);

        final LinearLayout whiteView = (LinearLayout) freqSpinner.getParent();
        whiteView.setBackgroundColor(bgColor);

        final SwitchCompat enableSwitch = root.findViewById(com.codetroopers.betterpickers.R.id.repeat_switch);
        // Pointless switch: the user will click cancel or ok anyway, no need to have this separate switch
        enableSwitch.toggle();
        enableSwitch.setVisibility(View.INVISIBLE);

        return root;
    }
}
