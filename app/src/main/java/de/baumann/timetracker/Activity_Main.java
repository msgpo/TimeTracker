package de.baumann.timetracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import de.baumann.timetracker.activities.Activity_EditEntry;
import de.baumann.timetracker.activities.Activity_Intro;
import de.baumann.timetracker.activities.Activity_Settings;
import de.baumann.timetracker.helper.DbAdapter_Comments;
import de.baumann.timetracker.helper.DbAdapter_Entries;
import de.baumann.timetracker.helper.DbAdapter_Tasks;
import de.baumann.timetracker.helper.helper;

public class Activity_Main extends AppCompatActivity {

    //calling variables
    private DbAdapter_Entries db;
    private SimpleCursorAdapter adapter;

    private ListView lv = null;
    private EditText filter;
    private SharedPreferences sharedPref;
    private RelativeLayout filter_layout;
    private TextView tv_total;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activity = Activity_Main.this;

        PreferenceManager.setDefaultValues(activity, R.xml.user_settings, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        sharedPref.edit().putBoolean("finish_app", false).apply();
        sharedPref.edit().putBoolean("recreate_app", false).apply();
        boolean show = sharedPref.getBoolean("introShowDo_notShow", true);

        if (show){
            helper.switchToActivity(Activity_Main.this, Activity_Intro.class);
        }

        setTitle();

        filter_layout = (RelativeLayout) findViewById(R.id.filter_layout);
        filter_layout.setVisibility(View.GONE);
        lv = (ListView) findViewById(R.id.listNotes);
        filter = (EditText) findViewById(R.id.myFilter);
        tv_total = (TextView) findViewById(R.id.tv_total);

        ImageButton ib_hideKeyboard =(ImageButton) findViewById(R.id.ib_hideKeyboard);
        ib_hideKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filter.getText().length() > 0) {
                    filter.setText("");
                } else {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    filter_layout.setVisibility(View.GONE);
                    setTitle();
                    setNotesList();
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.put_entrySeqno(activity, "");
                helper.put_entryStart(activity, "");
                helper.put_entryEnd(activity, "");
                helper.put_entryTask(activity, "");
                helper.put_entryCom(activity, "");
                helper.put_entryDur(activity, "");
                helper.switchToActivity(activity, Activity_EditEntry.class);
            }
        });

        //calling DbAdapter_Entries
        db = new DbAdapter_Entries(activity);
        db.open();

        setNotesList();
    }

    private void setNotesList() {

        //display data
        final int layoutstyle=R.layout.list_item_entries;
        int[] xml_id = new int[] {
                R.id.entry_dur,
                R.id.entry_title,
                R.id.entry_com,
                R.id.entry_start
        };
        String[] column = new String[] {
                "time_dur",
                "time_task",
                "time_com",
                "time_start"
        };
        Cursor row = db.fetchAllData(activity);
        adapter = new SimpleCursorAdapter(activity, layoutstyle,row,column, xml_id, 0);

        //display data by filter
        final String time_search = sharedPref.getString("filter_timeBY", "time_task");
        sharedPref.edit().putString("filter_timeBY", "time_task").apply();
        filter.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        sumTotalTime();
                    }
                }, 200);
            }
        });
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                return db.fetchDataByFilter(constraint.toString(),time_search);
            }
        });

        lv.setAdapter(adapter);

        //onClick function
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterview, View view, int position, long id) {

                Cursor row2 = (Cursor) lv.getItemAtPosition(position);
                helper.put_entryStart(activity, row2.getString(row2.getColumnIndexOrThrow("time_start")));
                helper.put_entryEnd(activity, row2.getString(row2.getColumnIndexOrThrow("time_end")));
                helper.put_entryTask(activity, row2.getString(row2.getColumnIndexOrThrow("time_task")));
                helper.put_entryCom(activity, row2.getString(row2.getColumnIndexOrThrow("time_com")));
                helper.put_entryDur(activity, row2.getString(row2.getColumnIndexOrThrow("time_dur")));
                helper.put_entrySeqno(activity, row2.getString(row2.getColumnIndexOrThrow("_id")));
                helper.switchToActivity(activity, Activity_EditEntry.class);
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor row = (Cursor) lv.getItemAtPosition(position);
                final String _id = row.getString(row.getColumnIndexOrThrow("_id"));

                Snackbar snackbar = Snackbar
                        .make(lv, R.string.entry_delete, Snackbar.LENGTH_LONG)
                        .setAction(R.string.toast_yes, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                db.delete(Integer.parseInt(_id));
                                setNotesList();
                            }
                        });
                snackbar.show();

                return true;
            }
        });

        sumTotalTime();
    }


    private void sumTotalTime() {

        sharedPref.edit().putString("total_time", "0").apply();
        View view = null;

        String value;
        for (int i = 0; i < adapter.getCount(); i++) {
            view = adapter.getView(i, view, lv);
            TextView et = (TextView) view.findViewById(R.id.entry_dur);
            value=et.getText().toString();
            double dur_saved = Double.parseDouble(sharedPref.getString("total_time", "0"));
            double dur_new = dur_saved + Double.parseDouble(value);
            sharedPref.edit().putString("total_time", String.valueOf(dur_new)).apply();
        }

        tv_total.setText(activity.getString(R.string.entry_total) + " " +
                helper.dur_long(activity, sharedPref.getString("total_time", "")));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (filter_layout.getVisibility() == View.GONE) {
            setNotesList();
        }
        if (sharedPref.getBoolean("finish_app", false)) {
            sharedPref.edit().putBoolean("finish_app", false).apply();
            finish();
        }
        if (sharedPref.getBoolean("recreate_app", false)) {
            sharedPref.edit().putBoolean("recreate_app", false).apply();
            recreate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_settings:
                helper.switchToActivity(activity, Activity_Settings.class);
                return true;

            case R.id.action_summary:
                getShareText();
                return true;

            case R.id.filter_taskOwn:
                search("time_task", "", getString(R.string.action_filter_task));
                filter.requestFocus();
                helper.showKeyboard(activity, filter);
                return true;
            case R.id.filter_taskList:
                AlertDialog.Builder builder_task = new AlertDialog.Builder(activity);
                builder_task.setTitle(getString(R.string.entry_select));
                // database handler
                DbAdapter_Tasks db_tasks = new DbAdapter_Tasks(activity);
                db_tasks.open();
                // Spinner Drop down elements
                List<String> labels_task = db_tasks.getRecords();
                Collections.sort(labels_task, new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        return s1.compareToIgnoreCase(s2);
                    }
                });
                final ArrayAdapter<String> adapter_task = new ArrayAdapter<>(activity, R.layout.list_item_tv);
                adapter_task.addAll(labels_task);

                builder_task.setNegativeButton(getString(R.string.toast_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder_task.setAdapter(adapter_task, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = adapter_task.getItem(which);
                        search("time_task", strName, getString(R.string.action_filter_task));
                    }
                });
                builder_task.show();
                return true;

            case R.id.filter_comOwn:
                search("time_com", "", getString(R.string.action_filter_com));
                filter.requestFocus();
                helper.showKeyboard(activity, filter);
                return true;
            case R.id.filter_comList:
                AlertDialog.Builder builder_com = new AlertDialog.Builder(activity);
                builder_com.setTitle(getString(R.string.entry_select));
                // database handler
                DbAdapter_Comments db_comments = new DbAdapter_Comments(activity);
                db_comments.open();
                // Spinner Drop down elements
                List<String> labels_com = db_comments.getRecords();
                Collections.sort(labels_com, new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        return s1.compareToIgnoreCase(s2);
                    }
                });
                final ArrayAdapter<String> adapter_com = new ArrayAdapter<>(activity, R.layout.list_item_tv);
                adapter_com.addAll(labels_com);

                builder_com.setNegativeButton(getString(R.string.toast_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder_com.setAdapter(adapter_com, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = adapter_com.getItem(which);
                        search("time_com", strName, getString(R.string.action_filter_com));
                    }
                });
                builder_com.show();
                return true;

            case R.id.filter_today:
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                search("time_start", dateFormat.format(cal.getTime()), getString(R.string.action_filter_startTime));
                return true;
            case R.id.filter_yesterday:
                DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar cal2 = Calendar.getInstance();
                cal2.add(Calendar.DATE, -1);
                search("time_start", dateFormat2.format(cal2.getTime()), getString(R.string.action_filter_startTime));
                return true;
            case R.id.filter_before:
                DateFormat dateFormat3 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar cal3 = Calendar.getInstance();
                cal3.add(Calendar.DATE, -2);
                search("time_start", dateFormat3.format(cal3.getTime()), getString(R.string.action_filter_startTime));
                return true;
            case R.id.filter_month:
                DateFormat dateFormat4 = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                Calendar cal4 = Calendar.getInstance();
                search("time_start", dateFormat4.format(cal4.getTime()), getString(R.string.action_filter_startTime));
                return true;
            case R.id.filter_own:
                search("time_start", "", getString(R.string.action_filter_startTime));
                filter.requestFocus();
                helper.showKeyboard(activity, filter);
                return true;

            case R.id.time_task:
                sharedPref.edit().putString("sortDB", "time_task").apply();
                setTitle();
                setNotesList();
                return true;
            case R.id.time_dur:
                sharedPref.edit().putString("sortDB", "time_dur").apply();
                setTitle();
                setNotesList();
                return true;
            case R.id.time_start:
                sharedPref.edit().putString("sortDB", "time_start").apply();
                setTitle();
                setNotesList();
                return true;
            case R.id.time_com:
                sharedPref.edit().putString("sortDB", "time_com").apply();
                setTitle();
                setNotesList();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setTitle() {
        if (sharedPref.getString("sortDB", "time_task").equals("time_task")) {
            setTitle(getString(R.string.app_name) + " | " + getString(R.string.sort_task));
        } else if (sharedPref.getString("sortDB", "time_task").equals("time_dur")) {
            setTitle(getString(R.string.app_name) + " | " + getString(R.string.sort_dur));
        }  else if (sharedPref.getString("sortDB", "time_task").equals("time_start")) {
            setTitle(getString(R.string.app_name) + " | " + getString(R.string.sort_start));
        } else {
            setTitle(getString(R.string.app_name) + " | " + getString(R.string.sort_com));
        }
    }

    private void search(String filterBy, String searchFor, String hint) {
        sharedPref.edit().putString("filter_timeBY", filterBy).apply();
        setNotesList();
        filter_layout.setVisibility(View.VISIBLE);
        filter.setHint(hint);
        filter.setText(searchFor);
    }

    private void getShareText() {

        sharedPref.edit().putString("share_text", "").apply();
        View view = null;

        for (int i = 0; i < adapter.getCount(); i++) {

            view = adapter.getView(i, view, lv);

            TextView tv_title = (TextView) view.findViewById(R.id.entry_title);
            TextView tv_com = (TextView) view.findViewById(R.id.entry_com);
            TextView tv_start = (TextView) view.findViewById(R.id.entry_start);
            TextView tv_dur = (TextView) view.findViewById(R.id.entry_dur);

            String text_title = getString(R.string.share_task) + " " + tv_title.getText().toString() + "\n";
            String text_com = getString(R.string.share_com) + " " + tv_com.getText().toString() + "\n";
            String text_start = getString(R.string.share_start) + " " + tv_start.getText().toString() + "\n";
            String text_dur = getString(R.string.share_dur) + " " + tv_dur.getText().toString() + " " + getString(R.string.entry_hours) + "\n";
            String row = text_title + text_com + text_start + text_dur + "\n";

            sharedPref.edit().putString("share_text", sharedPref.getString("share_text", "") + row).apply();
        }

        final String text = sharedPref.getString("share_text", "") + tv_total.getText();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getString(R.string.action_summary));
        builder.setMessage(text);
        builder.setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.toast_share, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final CharSequence[] options = {
                        getString(R.string.toast_share_text),
                        getString(R.string.toast_share_textFile)};

                AlertDialog.Builder builder2 = new AlertDialog.Builder(activity);
                builder2.setPositiveButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

                builder2.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals(getString(R.string.toast_share_text))) {
                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                            sharingIntent.setType("text/plain");
                            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.action_summary));
                            sharingIntent.putExtra(Intent.EXTRA_TEXT, text);
                            startActivity(Intent.createChooser(sharingIntent, (getString(R.string.toast_share_use))));
                        }
                        if (options[item].equals(getString(R.string.toast_share_textFile))) {

                            helper.grantPermissionsStorage(activity);

                            try {

                                File directory = new File(Environment.getExternalStorageDirectory() + "/Android/data/timetracker.backup/");
                                if (!directory.exists()) {
                                    //noinspection ResultOfMethodCallIgnored
                                    directory.mkdirs();
                                }

                                File filepath = new File(directory, getString(R.string.action_summary) + ".txt");  // file path to save
                                FileWriter writer = new FileWriter(filepath);
                                writer.append(text);
                                writer.flush();
                                writer.close();

                                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                sharingIntent.setType("text/txt");
                                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.action_summary));
                                sharingIntent.putExtra(Intent.EXTRA_TEXT, text);

                                if (filepath.exists()) {
                                    Uri bmpUri = Uri.fromFile(filepath);
                                    sharingIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                }

                                startActivity(Intent.createChooser(sharingIntent, (getString(R.string.toast_share_use))));

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });
                final AlertDialog dialog2 = builder2.create();
                // Display the custom alert dialog on interface
                dialog2.show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
