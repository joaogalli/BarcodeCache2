package br.com.sovi.barcodecache20;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.ResourceCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final String SHOW_ARCHIVED_STATE = "showArchivedState";
    private static final String PENDING_READING = "pendingReading";

    private CacheService cacheService;

    private ReadingService readingService;

    private ListView cacheListView;

    private ResourceCursorAdapter adapter;

    private Reading pendingReading;

    private Snackbar pendingReadingSnackbar;

    private boolean showArchivedCaches = false;

    private Button noCacheCreateOneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cacheService = new CacheService(this);
        readingService = new ReadingService(this);

        noCacheCreateOneButton = (Button) findViewById(R.id.noCacheCreateOneButton);
        noCacheCreateOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCacheEditDialog(null);
            }
        });

        cacheListView = (ListView) findViewById(R.id.cacheListView);

        if (savedInstanceState != null) {
            showArchivedCaches = savedInstanceState.getBoolean(SHOW_ARCHIVED_STATE, false);
            String pendingReadingContent = savedInstanceState.getString(PENDING_READING);
            if (pendingReadingContent != null) {
                createPendingReading(pendingReadingContent);
            }
        }

//        setTitle(getString(R.string.active_caches));
//        Cursor cursor = cacheService.findAll();

        adapter = new ResourceCursorAdapter(this, R.layout.cache_row_layout, null, false) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                final Cache cache = new Cache(cursor);

                LinearLayout colorLayout = (LinearLayout) view.findViewById(R.id.cacheRowColorIndicatorLayout);
                colorLayout.setBackgroundColor(cache.getColor());

                TextView nameTextView = (TextView) view.findViewById(R.id.cacheRowNameTextView);
                nameTextView.setText(cache.getName());

                final TextView infoTextView = (TextView) view.findViewById(R.id.cacheRowInfoTextView);
                infoTextView.setText(readingService.countByCache(cache.getId()) + getString(R.string._readings));

                LinearLayout mainLayout = (LinearLayout) view.findViewById(R.id.cacheRowMainLayout);
                mainLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showCacheOptions(cache);
                        return true;
                    }
                });
                mainLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (pendingReading != null) {
                            pendingReading.setCache(cache.getId());
                            readingService.save(pendingReading);
                            pendingReading = null;
                            infoTextView.setText(readingService.countByCache(cache.getId()) + getString(R.string._readings));
                            pendingReadingSnackbar.dismiss();
                        } else {
                            Intent intent = new Intent(MainActivity.this, CacheActivity.class);
                            intent.putExtra(CacheActivity.CACHE_ID, cache.getId());
                            startActivity(intent);
                        }
                    }
                });

                ImageButton editButton = (ImageButton) view.findViewById(R.id.cacheRowEditImageButton);
                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showCacheOptions(cache);
                    }
                });
            }

        };
        cacheListView.setAdapter(adapter);

        updateCacheList();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SHOW_ARCHIVED_STATE, showArchivedCaches);

        if (pendingReading != null) {
            outState.putString(PENDING_READING, pendingReading.getContent());
        }
    }

    private void updateCacheList() {
        Cursor cursor = null;
        if (showArchivedCaches) {
            cursor = cacheService.findArchived();
            setTitle(getString(R.string.archiveds));
        } else {
            cursor = cacheService.findAll();
            setTitle(getString(R.string.active_caches));
        }

        if (cursor.getCount() <= 0) {
            if (showArchivedCaches) {
                ((TextView) findViewById(R.id.noCacheHereTextView)).setVisibility(TextView.VISIBLE);
            } else {
                noCacheCreateOneButton.setVisibility(Button.VISIBLE);
            }
        } else {
            ((TextView) findViewById(R.id.noCacheHereTextView)).setVisibility(TextView.GONE);
            noCacheCreateOneButton.setVisibility(Button.GONE);
        }

        adapter.swapCursor(cursor);
    }

    private void scan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
//        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
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
            createPendingReading(scanResult.getContents());
        }
    }

    private void createPendingReading(String readingContent) {
        Reading reading = new Reading();
        reading.setContent(readingContent);
        pendingReading = reading;

        pendingReadingSnackbar = Snackbar.make(this.cacheListView, R.string.select_cache_insert_reading, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pendingReading = null;
                    }
                });
        pendingReadingSnackbar.show();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_new_cache:
                openCacheEditDialog(null);
                break;

            case R.id.nav_active_caches:
                showArchivedCaches = false;
                updateCacheList();
                break;

            case R.id.nav_archived_caches:
                showArchivedCaches = true;
                updateCacheList();
                break;

            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openCacheEditDialog(Cache cache) {
        CacheEditDialogFragment dialog = new CacheEditDialogFragment(cache);
        dialog.setNoticeDialogListener(new CacheEditDialogFragment.NoticeDialogListener() {
            @Override
            public boolean onDialogPositiveClick(DialogFragment dialog, Cache cache) {
                boolean saved = cacheService.save(cache);
                if (saved)
                    updateCacheList();

                return saved;
            }
        });
        dialog.show(getSupportFragmentManager(), getString(R.string.cache));
    }

    private void showCacheOptions(final Cache cache) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String archiveOption = cache.isArchive() ? getString(R.string.unarchive) : getString(R.string.archive);
        builder.setTitle(cache.getName())
                .setItems(new String[]{getString(R.string.edit), getString(R.string.clear_readings), archiveOption, getString(R.string.share)}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                openCacheEditDialog(cache);
                                break;
                            case 1:
                                clearCache(cache);
                                break;
                            case 2:
                                toggleArchiveCache(cache);
                                break;
                            case 3:
                                share(cache);
                                break;
                        }
                    }
                });
        builder.create().show();
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
            Toast.makeText(MainActivity.this, R.string.no_readings_to_share, Toast.LENGTH_SHORT).show();
        }
    }

    private void clearCache(final Cache cache) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.clear_cache_confirmation_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int count = readingService.clearCache(cache.getId());
                        Toast.makeText(MainActivity.this, getString(R.string.removed_readings, new Integer(count)), Toast.LENGTH_SHORT).show();
                        updateCacheList();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        // Create the AlertDialog object and return it
        builder.create().show();
    }

    private void toggleArchiveCache(Cache cache) {
        cacheService.toggleArchiveCache(cache.getId());
        updateCacheList();
    }

}
