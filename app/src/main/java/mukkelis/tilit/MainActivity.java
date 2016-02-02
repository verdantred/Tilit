package mukkelis.tilit;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

import mukkelis.tilit.AccountInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<AccountInfo> items;
    private ArrayAdapter<AccountInfo> itemsAdapter;
    private ListView lvItems;
    private EditText editName;
    private EditText editAccount;

    private TextWatcher nameWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            checkIfEmpty();
        }
    };

    private TextWatcher accountWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
            checkIfValid(s);
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            checkIfEmpty();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ADD HERE
        editName = (EditText) findViewById(R.id.etNewName);
        editAccount = (EditText) findViewById(R.id.etNewAccount);
        editName.addTextChangedListener(nameWatcher);
        editAccount.addTextChangedListener(accountWatcher);

        lvItems = (ListView) findViewById(R.id.lvItems);
        readItems();
        itemsAdapter = new ArrayAdapter<AccountInfo>(this, android.R.layout.simple_list_item_2, android.R.id.text1, items) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(items.get(position).name);
                text2.setText(items.get(position).account_num);
                return view;
            }
        };
        lvItems.setAdapter(itemsAdapter);
        items.add(new AccountInfo("Erkki", "29989279872"));
        items.add(new AccountInfo("Jarppi", "98348322784"));

    }

    public void onAddItem(View v) {
        EditText etNewName = (EditText) findViewById(R.id.etNewName);
        EditText etNewAccount = (EditText) findViewById(R.id.etNewAccount);
        String nameText = etNewName.getText().toString();
        String accountText = etNewAccount.getText().toString();
        itemsAdapter.add(new AccountInfo(nameText, accountText));
        etNewName.setText("");
        etNewAccount.setText("");
        writeItems();
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

    private void readItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        try {
            ArrayList<String> string_items = new ArrayList<>(FileUtils.readLines(todoFile));
            items = new ArrayList<>();
            for (int i = 0; (i + 1) < string_items.size(); i += 2) {
                items.add(new AccountInfo(string_items.get(i), string_items.get(i + 1)));
            }
        } catch (IOException e) {
            items = new ArrayList<>();
        }
    }

    private void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        try {
            ArrayList<String> string_items = new ArrayList<>();
            for (int i = 0; i < items.size(); ++i) {
                string_items.add(items.get(i).name);
                string_items.add(items.get(i).account_num);
            }
            FileUtils.writeLines(todoFile, string_items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkIfEmpty(){
        Button b = (Button) findViewById(R.id.btnAddItem);
        Boolean s1 = editName.getText().toString().trim().isEmpty();
        Boolean s2 = editAccount.getText().toString().trim().isEmpty();

        if (s1){
            //Name field is empty
            b.setEnabled(false);
            //Set EditText error
        }
        if (s2){
            //Account number field is empty
            b.setEnabled(false);
            //Set EditText error
        }
    }

    private void checkIfValid(Editable s){
        Button b = (Button) findViewById(R.id.btnAddItem);
        String acco = s.toString();
        //Finish the job..
    }
}
