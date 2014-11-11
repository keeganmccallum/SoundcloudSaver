package com.keeganmccallum.soundcloudsaver;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener{
    public static final String FROM_DIRECTORY = "/sdcard/Android/data" +
                            "/com.soundcloud.android/files/stream" +
                            "/Complete/";
    public static final String TO_DIRECTORY = "/sdcard/music/soundcloud/";

    private HashMap<String, String> nameMap = new HashMap<String, String>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView lv = (ListView)findViewById(R.id.listView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);

        new CopySCDB().execute();
        Log.d("test", Environment.getExternalStorageDirectory().getPath() + "/SoundCloud");
        SQLiteDatabase db = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory().getPath() + "/SoundCloud", null,
                                                        SQLiteDatabase.OPEN_READONLY);

        Cursor cursor = db.rawQuery("SELECT "+
                "tm.url_hash, sv.title "+
                "FROM TrackMetaData tm "+
                "INNER JOIN SoundView sv "+
                "ON sv._id = tm._id", new String[0]);

        boolean f = new File(TO_DIRECTORY).mkdirs();

        cursor. moveToFirst();
        while(!cursor.isAfterLast()) {
            String fname = cursor.getString(0);
            String title = cursor.getString(1);
            Log.d("test", "cp "+FROM_DIRECTORY+fname+" "+TO_DIRECTORY+title);

            nameMap.put(title, fname);
            adapter.add(title);
            cursor.moveToNext();
        }

        adapter.add("Save All");
    }


    public void copy(String fname, String title) throws IOException{
        copy(new File(FROM_DIRECTORY+fname), new File(TO_DIRECTORY+title+".mp3"));
    }

    public void copy(final File src, final File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }


    public void copyInBackground(final String fname, final String title) {
        new AsyncTask<String, Integer, String>() {

            @Override
            protected String doInBackground(String... params) {
                try {
                    copy(fname, title);
                    return "success";
                } catch (IOException e) {
                    return "failure";
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s.equals("success")) {
                    Toast.makeText(MainActivity.this, "Success! Copied: "+title, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to copy: "+title, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String title = adapter.getItem(position);
        String fname = nameMap.get(title);
        if (fname == null) {
            // copy all
            Toast.makeText(this, "Copying All Songs", Toast.LENGTH_SHORT).show();

            for (String t : nameMap.keySet()) {
                fname = nameMap.get(t);
                copyInBackground(fname, t);
            }
        } else {
            Toast.makeText(this, "Copying: " + title, Toast.LENGTH_SHORT).show();
            copyInBackground(fname, title);
        }
    }
}
