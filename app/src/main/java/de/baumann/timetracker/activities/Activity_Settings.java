/*
    This file is part of the HHS Moodle WebApp.

    HHS Moodle WebApp is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HHS Moodle WebApp is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Diaspora Native WebApp.

    If not, see <http://www.gnu.org/licenses/>.
 */

package de.baumann.timetracker.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import de.baumann.timetracker.R;
import de.baumann.timetracker.about.About_activity;
import de.baumann.timetracker.helper.helper;

public class Activity_Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        PreferenceManager.setDefaultValues(this, R.xml.user_settings, false);
        setTitle(R.string.action_settings);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Display the fragment as the activity_screen_main content
        getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {


        private void addEditTasksListener() {

            final Activity activity = getActivity();
            Preference reset = findPreference("edit_tasks");

            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {
                    helper.switchToActivity(activity, Activity_Tasks.class);
                    return true;
                }
            });
        }

        private void addEditCommentsListener() {

            final Activity activity = getActivity();
            Preference reset = findPreference("edit_comments");

            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {
                    helper.switchToActivity(activity, Activity_Comments.class);
                    return true;
                }
            });
        }

        private void addBackup_dbListener() {

            Preference reset = findPreference("backup");
            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {

                    if (android.os.Build.VERSION.SDK_INT >= 23) {
                        int hasWRITE_EXTERNAL_STORAGE = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                            helper.grantPermissionsStorage(getActivity());
                        } else {
                            backupDB();
                        }
                    } else {
                        backupDB();
                    }
                    return true;
                }
            });
        }

        private void addDelete_dbListener() {

            Preference reset = findPreference("delete");
            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {

                    PreferenceManager.setDefaultValues(getActivity(), R.xml.user_settings, false);
                    final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

                    final CharSequence[] options = {
                            getString(R.string.pref_backup_entries),
                            getString(R.string.pref_backup_tasks),
                            getString(R.string.pref_backup_comments),
                            getString(R.string.pref_backup_all)};
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.pref_delete))
                            .setPositiveButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            })
                            .setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    if (options[item].equals(getString(R.string.pref_backup_entries))) {
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(getString(R.string.toast_confirmation_title))
                                                .setMessage(getString(R.string.toast_confirmation))
                                                .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        sharedPref.edit().putBoolean("recreate_app", true).apply();
                                                        getActivity().deleteDatabase("time_DB_v01.db");
                                                        Toast.makeText(getActivity(), R.string.toast_delete, Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        dialog.cancel();
                                                    }
                                                }).show();
                                    }
                                    if (options[item].equals(getString(R.string.pref_backup_tasks))) {
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(getString(R.string.toast_confirmation_title))
                                                .setMessage(getString(R.string.toast_confirmation))
                                                .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        getActivity().deleteDatabase("tasks_DB_v01.db");
                                                        Toast.makeText(getActivity(), R.string.toast_delete, Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        dialog.cancel();
                                                    }
                                                }).show();
                                    }
                                    if (options[item].equals(getString(R.string.pref_backup_comments))) {
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(getString(R.string.toast_confirmation_title))
                                                .setMessage(getString(R.string.toast_confirmation))
                                                .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        getActivity().deleteDatabase("comments_DB_v01.db");
                                                        Toast.makeText(getActivity(), R.string.toast_delete, Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        dialog.cancel();
                                                    }
                                                }).show();
                                    }
                                    if (options[item].equals(getString(R.string.pref_backup_all))) {
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(getString(R.string.toast_confirmation_title))
                                                .setMessage(getString(R.string.toast_confirmation))
                                                .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        sharedPref.edit().putBoolean("recreate_app", true).apply();
                                                        getActivity().deleteDatabase("time_DB_v01.db");
                                                        getActivity().deleteDatabase("tasks_DB_v01.db");
                                                        getActivity().deleteDatabase("comments_DB_v01.db");
                                                        Toast.makeText(getActivity(), R.string.toast_delete, Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        dialog.cancel();
                                                    }
                                                }).show();
                                    }
                                }
                            }).show();

                    return true;
                }
            });
        }

        private void addOpenSettingsListener() {

            final Activity activity = getActivity();
            Preference reset = findPreference("settings");

            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {

                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    getActivity().startActivity(intent);

                    return true;
                }
            });
        }

        private void addShortcutListener() {

            final Activity activity = getActivity();
            Preference reset = findPreference("shortcut");

            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {

                    Intent i = new Intent(activity.getApplicationContext(), Activity_EditEntry.class);
                    i.setAction("shortcut_newEntry");

                    Intent shortcut = new Intent();
                    shortcut.setAction(Intent.ACTION_MAIN);
                    shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
                    shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, (getString(R.string.app_newEntry)));
                    shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource.fromContext(activity.getApplicationContext(), R.mipmap.qc_entry_plus));
                    shortcut.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                    activity.sendBroadcast(shortcut);
                    Toast.makeText(getActivity(), R.string.toast_shortcut, Toast.LENGTH_SHORT).show();

                    return true;
                }
            });
        }

        private void addAboutListener() {

            Preference reset = findPreference("about");
            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {
                    helper.switchToActivity(getActivity(), About_activity.class);
                    return true;
                }
            });
        }

        private void backupDB () {
            final File sd = Environment.getExternalStorageDirectory();
            final File data = Environment.getDataDirectory();

            final CharSequence[] options = {
                    getString(R.string.pref_backup),
                    getString(R.string.pref_backup_restore)};
            new AlertDialog.Builder(getActivity())
                    .setPositiveButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    })
                    .setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            if (options[item].equals(getString(R.string.pref_backup_restore))) {
                                File directory = new File(Environment.getExternalStorageDirectory() + "/Android/data/timetracker.backup/");
                                if (!directory.exists()) {
                                    //noinspection ResultOfMethodCallIgnored
                                    directory.mkdirs();
                                }

                                final CharSequence[] options = {
                                        getString(R.string.pref_backup_entries),
                                        getString(R.string.pref_backup_tasks),
                                        getString(R.string.pref_backup_comments),
                                        getString(R.string.pref_backup_all)};
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(getString(R.string.pref_backup_restore))
                                        .setPositiveButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                dialog.cancel();
                                            }
                                        })
                                        .setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int item) {
                                                if (options[item].equals(getString(R.string.pref_backup_entries))) {
                                                    backup(new File(sd,"//Android//" + "//data//" + "//timetracker.backup//" + "time_DB_v01.db"),
                                                            new File(data, "//data//" + "de.baumann.timetracker" + "//databases//" + "time_DB_v01.db"),
                                                            getActivity().getString(R.string.toast_restore));
                                                }
                                                if (options[item].equals(getString(R.string.pref_backup_tasks))) {
                                                    backup(new File(sd,"//Android//" + "//data//" + "//timetracker.backup//" + "tasks_DB_v01.db"),
                                                            new File(data, "//data//" + "de.baumann.timetracker" + "//databases//" + "tasks_DB_v01.db"),
                                                            getActivity().getString(R.string.toast_restore));
                                                }
                                                if (options[item].equals(getString(R.string.pref_backup_comments))) {
                                                    backup(new File(sd,"//Android//" + "//data//" + "//timetracker.backup//" + "comments_DB_v01.db"),
                                                            new File(data, "//data//" + "de.baumann.timetracker" + "//databases//" + "comments_DB_v01.db"),
                                                            getActivity().getString(R.string.toast_restore));
                                                }
                                                if (options[item].equals(getString(R.string.pref_backup_all))) {
                                                    backup(new File(sd,"//Android//" + "//data//" + "//timetracker.backup//" + "time_DB_v01.db"),
                                                            new File(data, "//data//" + "de.baumann.timetracker" + "//databases//" + "time_DB_v01.db"),
                                                            getActivity().getString(R.string.toast_restore));
                                                    backup(new File(sd,"//Android//" + "//data//" + "//timetracker.backup//" + "tasks_DB_v01.db"),
                                                            new File(data, "//data//" + "de.baumann.timetracker" + "//databases//" + "tasks_DB_v01.db"),
                                                            getActivity().getString(R.string.toast_restore));
                                                    backup(new File(sd,"//Android//" + "//data//" + "//timetracker.backup//" + "comments_DB_v01.db"),
                                                            new File(data, "//data//" + "de.baumann.timetracker" + "//databases//" + "comments_DB_v01.db"),
                                                            getActivity().getString(R.string.toast_restore));
                                                }
                                            }
                                        }).show();
                            }
                            if (options[item].equals(getString(R.string.pref_backup))) {

                                final CharSequence[] options = {
                                        getString(R.string.pref_backup_entries),
                                        getString(R.string.pref_backup_tasks),
                                        getString(R.string.pref_backup_comments),
                                        getString(R.string.pref_backup_all)};
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(getString(R.string.pref_backup))
                                        .setPositiveButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                dialog.cancel();
                                            }
                                        })
                                        .setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int item) {
                                                if (options[item].equals(getString(R.string.pref_backup_entries))) {
                                                    backup(new File(data, "//data//" + "de.baumann.timetracker" + "//databases//" + "time_DB_v01.db"),
                                                            new File(sd,"//Android//" + "//data//" + "//timetracker.backup//" + "time_DB_v01.db"),
                                                            getActivity().getString(R.string.toast_backup));
                                                }
                                                if (options[item].equals(getString(R.string.pref_backup_tasks))) {
                                                    backup(new File(data, "//data//" + "de.baumann.timetracker" + "//databases//" + "tasks_DB_v01.db"),
                                                            new File(sd,"//Android//" + "//data//" + "//timetracker.backup//" + "tasks_DB_v01.db"),
                                                            getActivity().getString(R.string.toast_backup));
                                                }
                                                if (options[item].equals(getString(R.string.pref_backup_comments))) {
                                                    backup(new File(data, "//data//" + "de.baumann.timetracker" + "//databases//" + "comments_DB_v01.db"),
                                                            new File(sd,"//Android//" + "//data//" + "//timetracker.backup//" + "comments_DB_v01.db"),
                                                            getActivity().getString(R.string.toast_backup));
                                                }
                                                if (options[item].equals(getString(R.string.pref_backup_all))) {
                                                    backup(new File(data, "//data//" + "de.baumann.timetracker" + "//databases//" + "time_DB_v01.db"),
                                                            new File(sd,"//Android//" + "//data//" + "//timetracker.backup//" + "time_DB_v01.db"),
                                                            getActivity().getString(R.string.toast_backup));
                                                    backup(new File(data, "//data//" + "de.baumann.timetracker" + "//databases//" + "tasks_DB_v01.db"),
                                                            new File(sd,"//Android//" + "//data//" + "//timetracker.backup//" + "tasks_DB_v01.db"),
                                                            getActivity().getString(R.string.toast_backup));
                                                    backup(new File(data, "//data//" + "de.baumann.timetracker" + "//databases//" + "comments_DB_v01.db"),
                                                            new File(sd,"//Android//" + "//data//" + "//timetracker.backup//" + "comments_DB_v01.db"),
                                                            getActivity().getString(R.string.toast_backup));
                                                }
                                            }
                                        }).show();
                            }
                        }
                    }).show();
        }

        private void backup (File currentDBPath, File backupDBPath, String toast) {
            try {
                File sd = Environment.getExternalStorageDirectory();

                if (sd.canWrite()) {

                    FileChannel src = new FileInputStream(currentDBPath).getChannel();
                    FileChannel dst = new FileOutputStream(backupDBPath).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.toast_error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.user_settings);
            addEditTasksListener();
            addEditCommentsListener();
            addBackup_dbListener();
            addOpenSettingsListener();
            addShortcutListener();
            addAboutListener();
            addDelete_dbListener();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}