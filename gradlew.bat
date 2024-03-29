package Husin.com;

import Husin.com.model.BukuResult;
import Husin.com.network.ApiClient;
import Husin.com.network.BukuService;
import android.os.Bundle;
import android.telecom.Call;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    private static String EXTRA = "extra";
    String pengarang = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText edtSearch = (EditText) findViewById(R.id.et_search);
        edtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                pengarang = edtSearch.getText().toString().trim();
                loadBuku(pengarang);

            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int after) {


            }

            @Override
            public void afterTextChanged(Editable editable s) {

            }
        });
        loadBuku("");

    }
    private void loadBuku(String pengarang) {
        BukuService service = ApiClient.getmRetrofit().create(BukuService.class);
        Call<BukuResult> bukus = service.getBuku(pengarang);

    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             