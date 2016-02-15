// TIlit-application UI logic and other UI related stuff resides here

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


// The whole application runs in one activity
public class MainActivity extends AppCompatActivity {


    private ArrayAdapter<AccountInfo> itemsAdapter; // Adapter that operates the listView
    private ListView lvItems;                       // The list of accounts UI element
    private Accounts myAccounts;
    private Button addButton;
    private EditText editName;                      // UI element for a new account name
    private EditText editAccount;                   // UI element for a new account number

    // Checks the new account name field for editing and checks if it is empty
    private TextWatcher nameWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (myAccounts.checkIfEmpty()){
                addButton.setEnabled(false);
            }
            else {
                addButton.setEnabled(true);
            }
        }
    };

    // Same thing but for the account number, here the IBAN-validity of the number string is also checked
    private TextWatcher accountWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
            AccountFieldState account = myAccounts.checkIfValid(s);
            switch (account){
                case Valid:
                    addButton.setEnabled(true);
                    editAccount.setError(null);
                    break;

                case Empty:
                    addButton.setEnabled(false);
                    editAccount.setError(null);
                    break;

                case NonValid:
                    addButton.setEnabled(false);
                    editAccount.setError("Account number should be in IBAN format");
                    break;
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (myAccounts.checkIfEmpty()){
                addButton.setEnabled(false);
            }
            else {
                addButton.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editName = (EditText) findViewById(R.id.etNewName);
        editAccount = (EditText) findViewById(R.id.etNewAccount);

        // Assign the watchers
        editName.addTextChangedListener(nameWatcher);
        editAccount.addTextChangedListener(accountWatcher);

        myAccounts = new Accounts(editName,
                editAccount, getFilesDir());


        // Disable new account button
        addButton = (Button) findViewById(R.id.btnAddItem);
        addButton.setEnabled(false);

        lvItems = (ListView) findViewById(R.id.lvItems);
        // Setup the context menu for list items
        registerForContextMenu(lvItems);
        // Read existing items into the list from a file if there are any
        myAccounts.readItems();
        // Assign an adapter between the list and the UI element ListView
        itemsAdapter = new ArrayAdapter<AccountInfo>(this, android.R.layout.simple_list_item_2,
                                                    android.R.id.text1, myAccounts.getItems()) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                // Define how the mapping is done from the list of names and account numbers to the
                // simple_list_item_2 layout file.

                text1.setText(myAccounts.getItems().get(position).name);
                text2.setText(myAccounts.getItems().get(position).account_num);
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
        savedInstanceState.putString("Name", editName.toString());
        savedInstanceState.putString("Account", editAccount.toString());
    }

    // Loading the saved texts when app regains focus
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        editName.setText(savedInstanceState.getString("Name"));
        editAccount.setText(savedInstanceState.getString("Account"));
    }

    // Define how the input data is saved as an AccountInfo entry on button press
    public void onAddItem(View v) {
        String nameText = editName.getText().toString();
        String accountText = editAccount.getText().toString();
        itemsAdapter.add(new AccountInfo(nameText, accountText));
        editName.setText("");
        editAccount.setText("");
        myAccounts.writeItems();
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
                ClipData clip = ClipData.newPlainText("account number", myAccounts.getItems().get(info.position).account_num);
                clipboard.setPrimaryClip(clip);
                return true;
            // Delete the entry from the list
            case R.id.delete:
                myAccounts.getItems().remove(info.position);
                itemsAdapter.notifyDataSetChanged();
                myAccounts.writeItems();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


}
