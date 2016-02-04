// TIlit-application main logic resides here

package mukkelis.tilit;

import android.content.ClipData;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.content.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.routines.checkdigit.IBANCheckDigit;

//import mukkelis.tilit.AccountInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

// The whole application runs in one activity
public class MainActivity extends AppCompatActivity {

    private ArrayList<AccountInfo> items;           // The AccountInfo items that populate the list
    private ArrayAdapter<AccountInfo> itemsAdapter; // Adapter that operates the listView
    private ListView lvItems;                       // The list of accounts UI element
    private EditText editName;                      // UI element for a new account name
    private EditText editAccount;                   // UI element for a new account number
    private IBANCheckDigit checker;                 /* This checks if the given account number is
                                                     IBAN valid */

    // Checks the new account name field for editing and checks if it is empty
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

    // Same thing but for the account number, here the validity of the number string is also checked
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // ADD HERE
        editName = (EditText) findViewById(R.id.etNewName);
        editAccount = (EditText) findViewById(R.id.etNewAccount);
        checker = new IBANCheckDigit();
        editName.addTextChangedListener(nameWatcher);
        editAccount.addTextChangedListener(accountWatcher);
        Button b = (Button) findViewById(R.id.btnAddItem);
        b.setEnabled(false);

        lvItems = (ListView) findViewById(R.id.lvItems);
        registerForContextMenu(lvItems);
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

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        EditText etNewName = (EditText) findViewById(R.id.etNewName);
        EditText etNewAccount = (EditText) findViewById(R.id.etNewAccount);
        savedInstanceState.putString("Name", etNewName.toString());
        savedInstanceState.putString("Account", etNewAccount.toString());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        EditText etNewName = (EditText) findViewById(R.id.etNewName);
        EditText etNewAccount = (EditText) findViewById(R.id.etNewAccount);
        etNewName.setText(savedInstanceState.getString("Name"));
        etNewAccount.setText(savedInstanceState.getString("Account"));
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.lvItems) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.copy:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("account number", items.get(info.position).account_num);
                clipboard.setPrimaryClip(clip);
                return true;
            case R.id.delete:
                items.remove(info.position);
                itemsAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
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

        }
        else{
            b.setEnabled(true);
        }
        if (s2){
            //Account number field is empty
            b.setEnabled(false);
            editAccount.setError(null);
        }
        else{
            b.setEnabled(true);
        }
    }

    private void checkIfValid(Editable s){
        Button b = (Button) findViewById(R.id.btnAddItem);
        String acco = s.toString().trim();

        if (checker.isValid(acco)){
            editAccount.setError(null);
            b.setEnabled(true);
        }
        else if(acco.length() > 0){
            b.setEnabled(false);
            editAccount.setError("Account number should be in IBAN format");
        }

        /*String restString;
        String checkDigits;
        int countryDigits;


        long restDigits;

        if(acco.length() > 14){

            countryDigits = ((acco.charAt(0) - 55) * 10) + (acco.charAt(1) - 55);
            checkDigits = Integer.toString(countryDigits) + acco.substring(2, 4);
            restString = acco.substring(4) + checkDigits;
            restDigits = Long.parseLong(restString.substring(0, 9)) % 97;
            int i;
            for(i = 9; ((i - 9 + 1) * 7) + 9 < restString.length(); i += 7){
                restDigits = ((restDigits * 10000000) + Long.parseLong(restString.substring(i, i + 7))) % 97;
            }
            int j = restString.length() - i;
            if (j > 0){
                restDigits = ((restDigits * 10 * j) + Long.parseLong(restString.substring(i))) % 97;
            }
            if (restDigits == 1){
                editAccount.setError(null);
                b.setEnabled(true);
            }
            else{
                b.setEnabled(false);
                editAccount.setError("Account number should be in IBAN format");
            }
        }
        else if (acco.length() > 0){
            b.setEnabled(false);
            editAccount.setError("Account number should be in IBAN format");
        }
        */
    }
}
