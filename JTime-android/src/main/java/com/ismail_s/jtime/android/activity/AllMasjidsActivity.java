package com.ismail_s.jtime.android.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ismail_s.jtime.android.R;

public class AllMasjidsActivity extends ListActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      String[] masjids = new String[]{"one", "two", "three", "four", "five", "six"};
      setListAdapter(new ArrayAdapter<>(this, R.layout.list_item, masjids));
      ListView listView = getListView();
      listView.setTextFilterEnabled(true);
      listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
              CharSequence text = ((TextView)view).getText();
              Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
              toast.show();
          }
      });
  }
}
