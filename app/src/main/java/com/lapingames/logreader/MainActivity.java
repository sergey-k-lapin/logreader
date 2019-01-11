package com.lapingames.logreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Button;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    JLogReader LogReader;
    JLogWriter LogWriter;

    ListView listView;
    EditText filterPattern;
    EditText url;
    Button parseLog;

    ArrayList<ListRow> listDataSource;
    CustomListViewAdapter dataSourceAdapter;

    AlertDialog.Builder ad;
    Context context;

    int totalFound = 0;

    public void showMesage(String title, String message) {
        ad.setTitle(title);
        ad.setMessage(message);
        ad.show();
    }

    public void setFilter() {
        String pattern = filterPattern.getText().toString();
        if (pattern.length() > 0) {
            LogReader.SetFilter(pattern, pattern.length());
        }
//        Log.v("JLogReader", filterPattern.getText().toString());
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(((Activity) MainActivity.this).getCurrentFocus().getWindowToken(), 0);
    }

    public void disableUI() {
        filterPattern.setEnabled(false);
        url.setEnabled(false);
        parseLog.setEnabled(false);
    }

    public void enableUI() {
        filterPattern.setEnabled(true);
        url.setEnabled(true);
        parseLog.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = MainActivity.this;
        ad = new AlertDialog.Builder(context);
        ad.setNegativeButton("ОК",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        LogWriter =  new JLogWriter(this.context);

        LogReader = new JLogReader(new SearchCallback() {
            @Override
            public void onSuccess(String src) {
                listDataSource.add(new ListRow(src, false));
                LogWriter.add(totalFound + src);
                ++totalFound;
//                Log.v("JLogReader ", src);
            }

            @Override
            public void onFinish(final int count) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dataSourceAdapter.notifyDataSetInvalidated();
                        showMesage("Статистика поиска", "Обработано строк: " + count + "\nНайдено совпадений: " + totalFound);
                        enableUI();
                    }
                });
            }
        });


        filterPattern = findViewById(R.id.filter_pattern);
//        final Button setFilter = findViewById(R.id.set_filter_button);
//        setFilter.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                setFilter();
//            }
//        });

        final Button stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (LogReader.loader != null && LogReader.loader.isAlive()) {
                    LogReader.loader.interrupt();
                    totalFound = 0;
                    enableUI();
                }
            }
        });

        url = findViewById(R.id.url);
        parseLog = findViewById(R.id.parse_log);
        parseLog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideKeyboard();
                setFilter();
                LogReader.LoadFile(url.getText().toString());
                listDataSource.clear();
                dataSourceAdapter.notifyDataSetInvalidated();
                totalFound = 0;
                disableUI();
            }
        });

        listView = findViewById(R.id.itemList);
        listDataSource = new ArrayList();
        dataSourceAdapter = new CustomListViewAdapter(this, listDataSource);

        listView.setAdapter(dataSourceAdapter);

        listView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideKeyboard();
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.v("JLogReader", listDataSource.get(position));
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Log.d("JLogReader", "scrollState = " + scrollState);
            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                Log.d("JLogReader", "scroll: firstVisibleItem = " + firstVisibleItem
                        + ", visibleItemCount" + visibleItemCount
                        + ", totalItemCount" + totalItemCount);
            }
        });
        listView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                return false;
            }
        });

        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                mode.setTitle(""+listView.getCheckedItemCount()+" items selected");
                listDataSource.get(position).checked = !listDataSource.get(position).checked;
                listView.invalidateViews();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.menu,menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                String result;
                result = "";
                switch (item.getItemId()){
                    case R.id.copy:
                        SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                        for(int i =0;i<checkedItems.size();i++){
                            ListRow row = listDataSource.get( checkedItems.keyAt(i) );
                            if(checkedItems.valueAt(i) == true){
                                row.checked = false;
                                result += row.text+"\n";
                            }
                        }
                        mode.finish();
                        listView.clearChoices();
                        listView.invalidateViews();
//                        Log.v("JLogReader", result);
                        ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData myClip = ClipData.newPlainText("text",result);
                        myClipboard.setPrimaryClip(myClip);

                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
    }
}
