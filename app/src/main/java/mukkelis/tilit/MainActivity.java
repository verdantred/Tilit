// TIlit-application main logic resides here

package mukkelis.tilit;

import android.content.ClipData;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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

        // Populate the class variables
        editName = (EditText) findViewById(R.id.etNewName);
        editAccount = (EditText) findViewById(R.id.etNewAccount);
        checker = new IBANCheckDigit();

        // Assign the watchers
        editName.addTextChangedListener(nameWatcher);
        editAccount.addTextChangedListener(accountWatcher);

        // Disable new account button
        Button b = (Button) findViewById(R.id.btnAddItem);
        b.setEnabled(false);

        lvItems = (ListView) findViewById(R.id.lvItems);
        // Setup the context menu for list items
        registerForContextMenu(lvItems);
        // Read existing items into the list from a file if there are any
        readItems();
        // Assign an adapter between the list and the UI element ListView
        itemsAdapter = new ArrayAdapter<AccountInfo>(this, android.R.layout.simple_list_item_2,
                                                    android.R.id.text1, items) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                // Define how the mapping is done from the list of names and account numbers to the
                // simple_list_item_2 layout file.

                text1.setText(items.get(position).name);
                text2.setText(items.get(position).account_num);
                return view;
            }
        };
        lvItems.setAdapter(itemsAdapter);
        lvItems.requestFocus();

    }

    // Saving the EditText fields when app loses focus
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

    // Loading the saved texts when app regains focus
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

    // Define how the input data is saved as an AccountInfo entry on button press
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

    // Action bar menu creation, using custom xml that defines a help button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.help_menu, menu);
        return true;
    }

    // The handling of the action bar item clicks are done here
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.help) {
            showHelp();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // This method constructs a simple dialog window to be shown
    // when you click a help button on action bar
    private void showHelp() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Help");
        alertDialog.setMessage("Long click on a list entry to either copy the account number or " +
                "delete the whole entry. A new account number must be given in the IBAN format.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Dismiss",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    // Define a context menu when you long click a list entry
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.lvItems) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_list, menu);
        }
    }

    // Handle context menu item clicks.
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            // Copy the account number from the list to the clipboard
            case R.id.copy:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("account number", items.get(info.position).account_num);
                clipboard.setPrimaryClip(clip);
                return true;
            // Delete the entry from the list
            case R.id.delete:
                items.remove(info.position);
                itemsAdapter.notifyDataSetChanged();
                writeItems();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    // Read a file that contains the list items
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

    // Write the list items to a file to preserve when the app shuts down
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

    // Check if a EditText-field is empty and enable/disable the add entry button according to it
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

    // Check if the new account number EditText -field is valid or not (in IBAN format)
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

    }
}
