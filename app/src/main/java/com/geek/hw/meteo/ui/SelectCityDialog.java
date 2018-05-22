package com.geek.hw.meteo.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.geek.hw.meteo.R;
import com.orhanobut.hawk.Hawk;

@Deprecated
public class SelectCityDialog extends DialogFragment {

    private final static String SETTINGS_STORAGE_SPINNER_ID = "SpinnerId";
    private int spinnerId;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.change_city_dialog));
        Resources r = getResources();

        Hawk.init(getActivity()).build();

        if (Hawk.contains(SETTINGS_STORAGE_SPINNER_ID))
            spinnerId = Hawk.get(SETTINGS_STORAGE_SPINNER_ID);
        else
            spinnerId = 0;

        final Spinner spinner = new Spinner(getActivity());

//        String[] items = getResources().getStringArray(R.array.cities);
//        ArrayAdapter<String> spinItems = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,
//                items);
//        spinner.setAdapter(spinItems);
        spinner.setSelection(spinnerId);

        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
        spinner.setPadding(px, px, px, px);
        builder.setView(spinner);
        builder.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                spinnerId = spinner.getSelectedItemPosition();
                ((AddCityDialogListener)getActivity()).onSelectCity(spinner.getSelectedItem().toString());
            }
        });
        return builder.create();
    }

    @Override
    public void onStop() {
        super.onStop();

        Hawk.put(SETTINGS_STORAGE_SPINNER_ID, spinnerId);
    }
}