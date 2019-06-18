package com.tarun.skyvision;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;


abstract public class SelectCity extends Dialog implements View.OnClickListener{

    private ListView list;
    private EditText filterText = null;
    ArrayAdapter<String> adapter = null;
    private static final String TAG = "CityList";
    Context contextReference;
    public SelectCity(Context context,String []cityList)
    {
        super(context);
        setContentView(R.layout.activity_select_city);
        contextReference = context;
        this.setTitle("Select City");
        filterText = (EditText) findViewById(R.id.EditBox);
        filterText.addTextChangedListener(filterTextWatcher);
        list = (ListView) findViewById(R.id.List);
        adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, cityList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Log.d(TAG, "Selected Item is = "+list.getItemAtPosition(position));
                onClick(v);

            }
        });
    }

    abstract public void onClick(View view);
    private TextWatcher filterTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            adapter.getFilter().filter(s);
        }
    };
    @Override
    public void onStop(){
        filterText.removeTextChangedListener(filterTextWatcher);
    }
}

