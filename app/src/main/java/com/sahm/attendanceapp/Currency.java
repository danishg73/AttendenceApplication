package com.sahm.attendanceapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;


public class Currency extends AppCompatActivity {
    String usertype, Manager_email, manager_username;
    SharedPreferences sp;
    String m_username, m_name, manager_name, manager, v, curr;
    Spinner spinner;
    String selected_currency, current_currency;
    ImageView backarroow;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        sp = getSharedPreferences("login", MODE_PRIVATE);
        if (sp.contains("user_type")) {
            v = sp.getString("user_type", usertype).trim();
            if (v.equals("manager"))
            {

                Manager_email = sp.getString("email", manager).trim();
                manager_username = sp.getString("username", m_username).trim();
                manager_name = sp.getString("name", m_name).trim();
                current_currency = sp.getString("currency", curr).trim();


            }
            else if (v.equals("employee"))
            {
                Intent intent = new Intent(Currency.this, Employee_Dashboard.class);
                startActivity(intent);
                this.finish();
            }
            else
                {
                Intent intent = new Intent(Currency.this, LoginDashboardActivity.class);
                startActivity(intent);
                }

            spinner = (Spinner) findViewById(R.id.spinner);
            backarroow = findViewById(R.id.backarrow);

            String[] currency = new String[]{
                    "AED ??.??", "AFN Af", "ALL L", "AMD ??", "AOA Kz",
                    "ARS $", "AUD $", "AWG ??", "AZN ??????", "BAM ????",
                    "BBD $", "BDT ???", "BGN ????", "BHD ??.??", "BIF ???",
                    "BMD $", "BND $", "BOB Bs.", "BRL R$", "BSD $",
                    "BTN", "BWP P", "BYN Br", "BZD $", "CAD $", "CDF ???",
                    "CHF ???", "CLP $", "CNY ??", "COP $", "CRC ???", "CUP $",
                    "CVE $", "CZK K??", "DJF ???", "DKK kr", "DOP $", "DZD ??.?? "
                    , "EGP ??", "ERN Nfk", "ETB", "EUR ???", "FJD $", "FKP ?? "
                    , "GBP ??", "GEL ???", "GHS ???", "GIP ??", "GMD D", "GNF ???"
                    , "GTQ Q", "GYD $", "HKD $", "HNL L", "HRK Kn", "HTG G"
                    , "HUF Ft", "IDR Rp", "ILS ???", "INR ???", "IQD ??.??", "IRR ???",
                    "ISK Kr", "JMD $", "JOD ??.??", "JPY ?? ", "KES Sh", "KGS"
                    , "KHR ???", "KPW ???", "KRW ???", "KWD ??.??", "KYD $", "KZT ???"
                    , "LAK ???", "LBP ??.??", "LKR Rs", "LRD $", "LSL L", "LYD ??.??"
                    , "MAD ??.??.", "MDL L", "MGA", "MKD ??????", "MMK K", "MNT ???"
                    , "MOP P", "MRU UM", "MUR ???", "MVR ??.", "MWK MK", "MXN $"
                    , "MYR RM", "MZN MTn", "NAD $", "NGN ???", "NIO C$", "NOK kr"
                    , "NPR ???", "NZD $", "OMR ??.??.", "PAB B/.", "PEN S/.", "PGK K"
                    , "PHP ???", "PKR ???", "PLN z??", "PYG ???", "QAR ??.??", "RON L"
                    , "RSD din", "RUB ??.", "RWF ???", "SAR ??.??", "SBD $", "SCR ???"
                    , "SDG ??", "SEK kr", "SGD $", "SHP ??", "SLL Le", "SOS Sh"
                    , "SRD $", "STN Db", "SYP ??.??", "SZL L", "THB ???", "TJS ????"
                    , "TMT m", "TND ??.??", "TOP T$", "TRY ???", "TTD $", "TWD $"
                    , "TZS Sh", "UAH ???", "UGX Sh", "USD $", "UYU $", "UZS"
                    , "VEF Bs F", "VND ???", "VUV Vt", "WST T", "XAF ???", "XCD $"
                    , "XPF ???", "YER ???", "ZAR R", "ZMW ZK", "ZWL $"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, currency);

            int pos = Arrays.asList(currency).indexOf(current_currency);
            spinner.setAdapter(adapter);
            spinner.setSelection(pos);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {

                    selected_currency = (String) parent.getItemAtPosition(position);
                    if (!selected_currency.equals(current_currency)) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("Currency", selected_currency.trim());
                        map.put("manager_email", Manager_email);
                        map.put("manager_username", manager_username);
                        FirebaseDatabase.getInstance().getReference("Currency").child(manager_username)
                                .setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    SharedPreferences.Editor edit = sp.edit();
                                    edit.putString("currency", selected_currency.trim());
                                    edit.apply();
                                    Toast.makeText(Currency.this, R.string.Updated, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Currency.this, R.string.Error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }


                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // TODO Auto-generated method stub
                }
            });


            backarroow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Currency.super.onBackPressed();
                }
            });


        }
    }
}

