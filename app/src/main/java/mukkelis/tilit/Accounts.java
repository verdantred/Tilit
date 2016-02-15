// Everything related to the AccountInfo datastructure and its item validation resides here

package mukkelis.tilit;

import android.text.Editable;
import android.widget.EditText;

import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.routines.checkdigit.IBANCheckDigit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ville on 15.2.2016.
 */

// The state that the newAccount field can have
enum AccountFieldState {
    Empty,
    Valid,
    NonValid
}

// The Accounts class holds all the accounts and handles writing and reading them from/to a file
// and validating new entries
public class Accounts {

    private ArrayList<AccountInfo> items;   // The AccountInfo items that populate the list
    private IBANCheckDigit checker;         // This checks if the given account number is IBAN-valid
    private File filesDir;                  // The path to the file that holds the accounts


    public Accounts(File fileDir){
        checker = new IBANCheckDigit();
        filesDir = fileDir;

    }

    public ArrayList<AccountInfo> getItems(){
        return items;
    }

    public void add(AccountInfo info){
        items.add(info);
    }

    public void remove(int position){
        items.remove(position);
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
    public boolean checkIfEmpty(String s){
        return s.trim().isEmpty();
    }

    // Check if the new account number EditText field is valid or not (in IBAN format)
    public AccountFieldState checkIfValidAcc(String s){
        String acco = s.replaceAll("\\s", "");

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
