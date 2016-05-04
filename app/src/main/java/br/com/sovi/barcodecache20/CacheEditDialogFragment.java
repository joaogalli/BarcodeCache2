package br.com.sovi.barcodecache20;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.thebluealliance.spectrum.SpectrumPalette;

import br.com.sovi.barcodecache20.entity.Cache;

/**
 * Created by Joao on 14/04/2016.
 */
public class CacheEditDialogFragment extends DialogFragment {
    public interface NoticeDialogListener {
        public boolean onDialogPositiveClick(DialogFragment dialog, Cache cache);
    }

    private EditText nameEditText;

    private NoticeDialogListener noticeDialogListener;

    private Cache cache;

    public CacheEditDialogFragment() {
        super();
        this.cache = new Cache();
    }

    @SuppressLint("ValidFragment")
    public CacheEditDialogFragment(Cache cache) {
        this();
        if (cache != null)
            this.cache = cache;
    }

    public void setNoticeDialogListener(NoticeDialogListener noticeDialogListener) {
        this.noticeDialogListener = noticeDialogListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.cache_edit_layout, null);

        nameEditText = (EditText) view.findViewById(R.id.cacheEditNameEditText);

        if (cache != null) {
            nameEditText.setText(cache.getName());
        }

        SpectrumPalette spectrumPalette = (SpectrumPalette) view.findViewById(R.id.cacheEditPallete);
        int[] colors = getResources().getIntArray(R.array.demo_colors);
        spectrumPalette.setColors(colors);
        spectrumPalette.setSelectedColor(cache.getColor());
        spectrumPalette.setOnColorSelectedListener(new SpectrumPalette.OnColorSelectedListener() {
            @Override
            public void onColorSelected(@ColorInt int color) {
                cache.setColor(color);
            }
        });

        builder.setView(view)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CacheEditDialogFragment.this.getDialog().cancel();
                    }
                });

        final AlertDialog alertDialog = builder.create();

        // Isso precisa ser feito para não fechar automaticamente
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // save the cache
                        cache.setName(nameEditText.getText().toString());
                        // color is set when changed in the component listener.

                        if (cache.getName().trim().isEmpty()) {
                            nameEditText.requestFocus();
                            Toast.makeText(CacheEditDialogFragment.this.getContext(), R.string.fill_cache_name, Toast.LENGTH_SHORT).show();
                        } else if (noticeDialogListener != null) {
                            if (noticeDialogListener.onDialogPositiveClick(CacheEditDialogFragment.this, cache)) {
                                alertDialog.dismiss();
                            } else {
                                Toast.makeText(CacheEditDialogFragment.this.getContext(), R.string.error_could_not_save_cache, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });


        return alertDialog;
    }

}
