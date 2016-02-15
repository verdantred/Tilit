package mukkelis.tilit;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.routines.checkdigit.IBANCheckDigit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ville on 15.2.2016.
 */

enum AccountFieldState {
    Empty,
    Valid,
    NonValid
}

public class Accounts {

    private ArrayList<AccountInfo> items;           // The AccountInfo items that populate the list
    private EditText editName;                      // UI element for a new account name
    private EditText editAccount;                   // UI element for a new account number
    private IBANCheckDigit checker;                 // This checks if the given account number is
    private File filesDir;                          // IBAN-valid



    public Accounts(EditText newName, EditText newAccount, File fileDir){
        editName = newName;
        editAccount = newAccount;
        checker = new IBANCheckDigit();
        filesDir = fileDir;

    }

    public ArrayList<AccountInfo> getItems(){
        return items;
    }

    // Read a file that contains the list items
    public void readItems() {

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
    public void writeItems() {
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
    public boolean checkIfEmpty(){
        Boolean s1 = editName.getText().toString().trim().isEmpty();
        Boolean s2 = editAccount.getText().toString().trim().isEmpty();

        if (s1 || s2){
            if (s2){
                editAccount.setError(null);
            }
            return true;
        }
        else {
            return false;
        }

    }

    // Check if the new account number EditText -field is valid or not (in IBAN format)
    public AccountFieldState checkIfValid(Editable s){
        String acco = s.toString().replaceAll("\\s", "");

        if(acco.length() == 0){
            return AccountFieldState.Empty;
        }
        else if(checker.isValid(acco)){
            return  AccountFieldState.Valid;
        }
        else {
            return AccountFieldState.NonValid;
        }

    }
}
