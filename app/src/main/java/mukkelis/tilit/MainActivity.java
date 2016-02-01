package mukkelis.tilit;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import mukkelis.tilit.AccountInfo;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<AccountInfo> items;
    private ArrayAdapter<AccountInfo> itemsAdapter;
    private ListView lvItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ADD HERE
        lvItems = (ListView) findViewById(R.id.lvItems);
        items = new ArrayList<>();
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
        etNewName.setText("");
        etNewAccount.setText("");
        itemsAdapter.add(new AccountInfo(nameText, accountText));
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
}
