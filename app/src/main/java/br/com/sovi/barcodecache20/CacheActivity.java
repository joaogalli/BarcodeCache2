package br.com.sovi.barcodecache20;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.ResourceCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import br.com.sovi.barcodecache20.entity.Cache;
import br.com.sovi.barcodecache20.entity.Reading;
import br.com.sovi.barcodecache20.service.CacheService;
import br.com.sovi.barcodecache20.service.ReadingService;
import br.com.sovi.barcodecache20.utils.Dates;

public class CacheActivity extends AppCompatActivity {

    public static final String CACHE_ID = "CACHEID";

    private CacheService cacheService;

    private ReadingService readingService;

    private Cache cache;

    private ListView readingsListView;

    private ResourceCursorAdapter adapter;

    private LinearLayout colorLayout;

    private TextView nameTextView;

    private TextView infoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cacheService = new CacheService(this);
        readingService = new ReadingService(this);

        Intent intent = getIntent();
        int cacheId = intent.getIntExtra(CACHE_ID, -1);

        if (cacheId > -1) {
            cache = cacheService.findById(cacheId);
        }

        if (cacheId <= -1 || cache == null) {
            finishActivity(RESULT_CANCELED);
        }

        colorLayout = (LinearLayout) findViewById(R.id.cacheColorIndicatorLayout);

        nameTextView = (TextView) findViewById(R.id.cacheNameTextView);

        infoTextView = (TextView) findViewById(R.id.cacheInfoTextView);

        updateCache(cache);

        Cursor cursor = readingService.findLastFromCache(cacheId);

        readingsListView = (ListView) findViewById(R.id.cacheReadingListView);

        adapter = new ResourceCursorAdapter(this, R.layout.reading_row_layout, cursor, false) {

//            class CheckOnClick implements View.OnClickListener {
//
//                private int position;
//
//                public CheckOnClick(int position) {
//                    this.position = position;
//                }
//
//                @Override
//                public void onClick(View v) {
//                    System.out.println("CheckOnClick.onClick: " + position);
//                    if (isSelecting()) {
//                        boolean checked = readingsListView.isItemChecked(position);
//                        readingsListView.setItemChecked(position, !checked);
//                        changeView(v, !checked);
//                    }
//                }
//            }
//
//            class CheckOnLongClick implements View.OnLongClickListener {
//
//                private int position;
//
//                public CheckOnLongClick(int position) {
//                    this.position = position;
//                }
//
//                @Override
//                public boolean onLongClick(View v) {
//                    System.out.println("CheckOnLongClick.onLongClick: " + position);
//
//                    boolean checked = readingsListView.isItemChecked(position);
//                    readingsListView.setItemChecked(position, !checked);
//                    changeView(v, !checked);
//
//                    return true;
//                }
//            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                Reading reading = new Reading(cursor);

                LinearLayout mainLayout = (LinearLayout) view.findViewById(R.id.readingRowMainLayout);
//                mainLayout.setOnClickListener(new CheckOnClick(cursor.getPosition()));
//                mainLayout.setOnLongClickListener(new CheckOnLongClick(cursor.getPosition()));

                TextView contentTextView = (TextView) view.findViewById(R.id.readingContentTextView);
                contentTextView.setText(reading.getContent());

                TextView dateTextView = (TextView) view.findViewById(R.id.readingDateTextView);
                if (reading.getDate() != null)
                    dateTextView.setText(Dates.reading_date_format.format(reading.getDate()));
            }

//            private void changeView(View v, boolean checked) {
//                if (checked) {
//                    v.setBackgroundColor(Color.parseColor("#CCCCCC"));
//                } else {
//                    v.setBackgroundColor(Color.parseColor("#FFFFFF"));
//                }
//            }
        };

        readingsListView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(cache.getColor()));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    class OpenCacheEditDialogClickListener implements View.OnClickListener {

        private Cache cache;

        public OpenCacheEditDialogClickListener(Cache cache) {
            this.cache = cache;
        }

        @Override
        public void onClick(View v) {
            openCacheEditDialog(cache);
        }
    }

    private void scan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
//        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setOrientationLocked(true);
        integrator.setPrompt(getString(R.string.point_to_code));
//        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            Reading reading = new Reading();
            reading.setContent(scanResult.getContents());
            reading.setCache(this.cache.getId());
            readingService.save(reading);
            updateReadingList();
        }
    }

    private void updateReadingList() {
        Cursor cursor = readingService.findLastFromCache(cache.getId());
        adapter.swapCursor(cursor);
    }

    private void updateCache(Cache cache) {
        this.cache = cache;
        colorLayout.setBackgroundColor(cache.getColor());
        nameTextView.setText(cache.getName());
        infoTextView.setText(readingService.countByCache(cache.getId()) + getString(R.string._readings));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cache_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        switch (id) {
            case R.id.action_edit_cache:
                openCacheEditDialog(cache);
                return true;
            case R.id.action_clear_cache:
                clearCache(cache);
                return true;
            case R.id.action_share_cache:
                share(cache);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearCache(final Cache cache) {
        if (cache != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.clear_cache_confirmation_message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            int count = readingService.clearCache(cache.getId());
                            Toast.makeText(CacheActivity.this, getString(R.string.removed_readings, new Integer(count)), Toast.LENGTH_SHORT).show();
                            updateCache(cache);
                            updateReadingList();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            // Create the AlertDialog object and return it
            builder.create().show();
        }
    }

    private void openCacheEditDialog(Cache cache) {
        CacheEditDialogFragment dialog = new CacheEditDialogFragment(cache);
        dialog.setNoticeDialogListener(new CacheEditDialogFragment.NoticeDialogListener() {
            @Override
            public boolean onDialogPositiveClick(DialogFragment dialog, Cache cache) {
                boolean saved = cacheService.save(cache);

                if (saved)
                    updateCache(cache);

                return saved;
            }
        });
        dialog.show(getSupportFragmentManager(), getString(R.string.cache));
    }

    public boolean isSelecting() {
        return (readingsListView.getCheckedItemCount() > 0);
    }

    private void share(Cache cache) {
        Cursor cursor = readingService.findFromCache(cache.getId());
        StringBuilder sb = new StringBuilder();
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            SharedPreferences channel = PreferenceManager.getDefaultSharedPreferences(this);
            String separator = channel.getString("reading_separator", ";").toString();

            for (int i = 0; i < cursor.getCount(); i++) {
                String content = cursor.getString(cursor.getColumnIndex(Reading.FIELD_CONTENT));
                sb.append(content).append(separator);
                cursor.moveToNext();
            }

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else {
            Toast.makeText(CacheActivity.this, R.string.no_readings_to_share, Toast.LENGTH_SHORT).show();
        }
    }
}
