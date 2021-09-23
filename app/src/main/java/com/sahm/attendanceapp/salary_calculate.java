package com.sahm.attendanceapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class salary_calculate extends AppCompatActivity {

    String Manager_email,usertype,Manager_name,employee_email,target_name,manager_username,employee_username,target_username,salary,paid_type="";
    SharedPreferences sp;
    String type,namee,email;
    Button generate_report;
    ImageView backarrow;
    ListView listBar;
    List<Salary_list> salary_lists = new ArrayList<>();
    Salary_adapter salary_adapter;
    ProgressDialog progressDialog;
    String[] split;
    String  duration,currency;
    String time_diff,punch_out_time;
    Double s,v,salary_sum=0.0;
    TextView user_currency;
    Spinner spinner_month;
    ArrayList months;
    DecimalFormat decimalFormat;
    Calendar c = Calendar.getInstance();
    String current_month,previous_month, getmonth;
    AlertDialog alertDialog;
    String select="1";
    private AdView mAdView;
    File file;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp=getSharedPreferences("login",MODE_PRIVATE);
        if( sp.contains("user_type")){

            String v=  sp.getString("user_type",usertype).trim();
            if (v.equals("manager"))
            {
                Manager_email  =  sp.getString("email",email).trim();
                Manager_name  =  sp.getString("name",namee).trim();
                manager_username  =  sp.getString("username",namee).trim();
                paid_type = sp.getString("paid_type", "").trim();
                type="manager";
            }
            else if (v.equals("employee"))
            {
                type="employee";
            }
            else{
                Intent intent = new Intent(salary_calculate.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }
        setContentView(R.layout.activity_salary_calculate);

        employee_email = getIntent().getStringExtra("target_email");
        target_name = getIntent().getStringExtra("target_name");
        target_username = getIntent().getStringExtra("target_username");
        salary = getIntent().getStringExtra("salary");
        currency = getIntent().getStringExtra("currency");
        duration = getIntent().getStringExtra("duration");
        decimalFormat = new DecimalFormat("#.##");
        alertDialog = new AlertDialog.Builder(salary_calculate.this).create();

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM");

        backarrow=findViewById(R.id.backarrow);
        listBar = findViewById(R.id.listview);
        user_currency = findViewById(R.id.currency);
        generate_report = findViewById(R.id.generate_report);
        spinner_month = findViewById(R.id.spinner_month);
        user_currency.setText(getResources().getString(R.string.Currency)+": "+currency);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.Fetching_Data));
        progressDialog.setCancelable(false);

        current_month =sdf.format(c.getTime()).toString();   // NOW
        c.add(Calendar.MONTH, -1);
        previous_month=sdf.format(c.getTime());   // One month ago
        months = new ArrayList<String>();
        mAdView = findViewById(R.id.adView);

        if(paid_type.equals("free"))
        {

        }
        else if(paid_type.equals("paid"))
        {

        }
        else
        {
            LoadBannerAdd();
        }



        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        months.clear();
        months.add(current_month);
        months.add(previous_month);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months);
        spinner_month.setAdapter(adapter);
        spinner_month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                progressDialog.show();
                String selectedItem = adapterView.getItemAtPosition(i).toString().trim();
                getdata(selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                salary_calculate.super.onBackPressed();
            }
        });
        generate_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                checkPermission();

            }
        });
    }
    public void getdata(String month)
    {
        getmonth=month;
        Query query =FirebaseDatabase.getInstance().getReference("Attendance").
                child(manager_username).child(target_username).orderByChild("month").equalTo(month);
         FirebaseDatabase.getInstance().getReference("Attendance").
                child(manager_username).child(target_username);

                 query.addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot)
             {
                 salary_lists.clear();
                 listBar.setAdapter(null);
//                 salary_adapter.notifyDataSetChanged();
                 if(dataSnapshot.exists())
                 {
                     for (DataSnapshot snapshot: dataSnapshot.getChildren())
                     {

                         Salary_list sl = new Salary_list();
                         sl.date = snapshot.child("date").getValue().toString();
                         sl.punch_in = snapshot.child("punch_in_time").getValue().toString();
                         punch_out_time =snapshot.child("Punch_out_time").getValue().toString();
                         sl.punch_out=punch_out_time;

                         try {
                             time_diff=time_diff(sl.punch_in,sl.punch_out).toString();
                             split=time_diff.split(":");
                             sl.working_hours=split[0]+" Hr "+split[1]+" min";
                         } catch (ParseException e) {
                             e.printStackTrace();
                         }
                         if(duration.equals("Per Hour"))
                         {
                             if (!punch_out_time.equals("not added"))
                             {
                                 s=Double.parseDouble(salary);
                                 v = s* Double.parseDouble(split[0])+ s/60*Double.parseDouble(split[1]);
                                 sl.earning=  decimalFormat.format(v)+"";
                                 salary_sum = salary_sum + v;
                             }
                             else
                             {
                                 sl.earning="0";
                             }


                         }
                         else
                         {
                             sl.earning="-";
                         }
                         salary_lists.add(sl);
                         sl= null;
                     }

                     if(salary_sum>0.0)
                     {
                         Salary_list sl = new Salary_list();
                         sl.working_hours=getResources().getString(R.string.Total);
                         sl.earning=decimalFormat.format(salary_sum);
                         salary_lists.add(sl);
                         sl= null;
                     }
                     salary_adapter = new Salary_adapter(salary_lists,getBaseContext(),salary_calculate.this);
                     listBar.setAdapter(null);
                     listBar.setAdapter(salary_adapter);
                     salary_adapter.notifyDataSetChanged();
                     progressDialog.dismiss();
                 }
                 else
                 {
                     Toast.makeText(salary_calculate.this, R.string.NO_data_found, Toast.LENGTH_SHORT).show();
                     progressDialog.dismiss();
                 }

             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

                 Toast.makeText(salary_calculate.this, R.string.Error, Toast.LENGTH_SHORT).show();
                 progressDialog.dismiss();

             }
         });

    }
    public String time_diff(String start_time, String end_time) throws ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        DateFormat df = DateFormat.getInstance();
        Date date1 = simpleDateFormat.parse(start_time);
        Date date2 = simpleDateFormat.parse(end_time);
        long difference = date2.getTime() - date1.getTime();

        int day = (int) (difference / (1000*60*60*24));
        int hours = (int) ((difference - (1000*60*60*24*day)) / (1000*60*60));
        int min = (int) (difference - (1000*60*60*24*day) - (1000*60*60*hours)) / (1000*60);

        if(hours<0 )
        {
            hours = hours+24;
        }
        if(min<0)
        {
            min=min+60;
            if(hours==0)
            {
                hours= hours+23;
            }
        }

        return hours+":"+min;
    }
    private void checkPermission() {
        int result = ContextCompat.checkSelfPermission(salary_calculate.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            createPdf();
        } else {
            requestPermission();
        }
    }
    private void requestPermission() {

//        if (ActivityCompat.shouldShowRequestPermissionRationale(PlayerProgressRecording.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//            Toast.makeText(PlayerProgressRecording.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
//        } else {
        ActivityCompat.requestPermissions(salary_calculate.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                    createPdf();
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                    Toast.makeText(this, getResources().getString(R.string.Permission_Required), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    private void createPdf() {

        // TODO Auto-generated method stub
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();

        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sahm";

            File dir = new File(path);
            if(!dir.exists())
                dir.mkdirs();



            file = new File(dir, "Report_"+getmonth+"_"+target_username+".pdf");
            FileOutputStream fOut = new FileOutputStream(file);

            PdfWriter.getInstance(document, fOut);

            //open the document
            document.open();


            Paragraph p1 = new Paragraph("ATTENDANCE REPORT OF "+getmonth.toUpperCase());
            Font f = new Font(Font.FontFamily.TIMES_ROMAN, 25.0f, Font.BOLD, BaseColor.BLACK);
            p1.setFont(f);
            p1.setAlignment(Paragraph.ALIGN_CENTER);

            //add paragraph to document
            document.add(p1);

            Paragraph p2 = new Paragraph("\nName: "+target_name+"\nUserName: "+target_username+"\nSalary :"+currency+" "+salary+" "+duration);
            p2.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(p2);
            PdfPTable table = new PdfPTable(5);
            table.setSpacingBefore(24);

            BaseColor myColor2 = WebColors.getRGBColor("#ffffff");
            Font fontH1 = new Font(Font.FontFamily.TIMES_ROMAN,12,Font.BOLD,myColor2 );
            PdfPCell cell1 = new PdfPCell(new Phrase("Date",fontH1));
            PdfPCell cell2 = new PdfPCell(new Phrase("Punch In",fontH1));
            PdfPCell cell3 = new PdfPCell(new Phrase("Punch Out",fontH1));
            PdfPCell cell4 = new PdfPCell(new Phrase("Total Time",fontH1));
            PdfPCell cell5 = new PdfPCell(new Phrase("Earning",fontH1));
            BaseColor myColor = WebColors.getRGBColor("#42b6ff");
            cell1.setBorder(Rectangle.RIGHT);
            cell2.setBorder(Rectangle.RIGHT);
            cell3.setBorder(Rectangle.RIGHT);
            cell4.setBorder(Rectangle.RIGHT);
            cell5.setBorder(Rectangle.NO_BORDER);
            cell1.setBackgroundColor(myColor);
            cell2.setBackgroundColor(myColor);
            cell3.setBackgroundColor(myColor);
            cell4.setBackgroundColor(myColor);
            cell5.setBackgroundColor(myColor);

            cell1.setVerticalAlignment(Element.ALIGN_CENTER);
            cell1.setHorizontalAlignment(cell1.ALIGN_CENTER);
            cell1.setVerticalAlignment(cell1.ALIGN_CENTER);
            cell2.setHorizontalAlignment(cell1.ALIGN_CENTER);
            cell2.setVerticalAlignment(cell1.ALIGN_CENTER);
            cell3.setHorizontalAlignment(cell1.ALIGN_CENTER);
            cell3.setVerticalAlignment(cell1.ALIGN_CENTER);
            cell4.setHorizontalAlignment(cell1.ALIGN_CENTER);
            cell4.setVerticalAlignment(cell1.ALIGN_CENTER);
            cell5.setHorizontalAlignment(cell1.ALIGN_CENTER);
            cell5.setVerticalAlignment(cell1.ALIGN_CENTER);
            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);
            table.addCell(cell5);
//            document.add(table);


            BaseColor myColor3 = WebColors.getRGBColor("#EEEEEE");
            BaseColor myColor4 = WebColors.getRGBColor("#e3e3e3");
            Font fontH2 = new Font(Font.FontFamily.TIMES_ROMAN,10 );


            int size = salary_lists.size();
            for(int i=0;i<size;i++)
            {

                if(select.equals("1"))
                {

                    cell1 = new PdfPCell(new Phrase( salary_lists.get(i).date,fontH2));
                    cell2 = new PdfPCell(new Phrase(salary_lists.get(i).punch_in,fontH2));
                    cell3 = new PdfPCell(new Phrase(salary_lists.get(i).punch_out,fontH2));
                    cell4 = new PdfPCell(new Phrase(salary_lists.get(i).working_hours,fontH2));
                    cell5 = new PdfPCell(new Phrase(salary_lists.get(i).earning,fontH2));


                    cell1.setBackgroundColor(myColor3);
                    cell2.setBackgroundColor(myColor3);
                    cell3.setBackgroundColor(myColor3);
                    cell4.setBackgroundColor(myColor3);
                    cell5.setBackgroundColor(myColor3);


                    select="0";
                }
                else
                {


                    cell1 = new PdfPCell(new Phrase( salary_lists.get(i).date,fontH2));
                    cell2 = new PdfPCell(new Phrase(salary_lists.get(i).punch_in,fontH2));
                    cell3 = new PdfPCell(new Phrase(salary_lists.get(i).punch_out,fontH2));
                    cell4 = new PdfPCell(new Phrase(salary_lists.get(i).working_hours,fontH2));
                    cell5 = new PdfPCell(new Phrase(salary_lists.get(i).earning,fontH2));

                    cell1.setBackgroundColor(myColor4);
                    cell2.setBackgroundColor(myColor4);
                    cell3.setBackgroundColor(myColor4);
                    cell4.setBackgroundColor(myColor4);
                    cell5.setBackgroundColor(myColor4);

                    select="1";
                }


                cell1.setBorder(Rectangle.RIGHT);
                cell2.setBorder(Rectangle.RIGHT);
                cell3.setBorder(Rectangle.RIGHT);
                cell4.setBorder(Rectangle.RIGHT);
                cell5.setBorder(Rectangle.NO_BORDER);
                cell1.setVerticalAlignment(Element.ALIGN_CENTER);
                cell1.setHorizontalAlignment(cell1.ALIGN_CENTER);
                cell1.setVerticalAlignment(cell1.ALIGN_CENTER);
                cell2.setHorizontalAlignment(cell1.ALIGN_CENTER);
                cell2.setVerticalAlignment(cell1.ALIGN_CENTER);
                cell3.setHorizontalAlignment(cell1.ALIGN_CENTER);
                cell3.setVerticalAlignment(cell1.ALIGN_CENTER);
                cell4.setHorizontalAlignment(cell1.ALIGN_CENTER);
                cell4.setVerticalAlignment(cell1.ALIGN_CENTER);
                cell5.setHorizontalAlignment(cell1.ALIGN_CENTER);
                cell5.setVerticalAlignment(cell1.ALIGN_CENTER);
                table.addCell(cell1);
                table.addCell(cell2);
                table.addCell(cell3);
                table.addCell(cell4);
                table.addCell(cell5);




            }

            document.add(table);





//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.applogo);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 , stream);
//            Image myImg = Image.getInstance(stream.toByteArray());
//            myImg.setAlignment(Image.MIDDLE);
//
//            //add image to document
//            document.add(myImg);





        } catch (DocumentException de) {
            Log.e("PDFCreator", "DocumentException:" + de);
        } catch (IOException e) {
            Log.e("PDFCreator", "ioException:" + e);
        }
        finally
        {

            alertDialog.setIcon(R.drawable.applogo );
            alertDialog.setTitle(getResources().getString(R.string.File_Created));
            alertDialog.setMessage(getResources().getString(R.string.Saved_in_InternalStorage));
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.Share), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/*");
                    Uri uri = FileProvider.getUriForFile(salary_calculate.this, BuildConfig.APPLICATION_ID + ".provider",file);
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, uri );
                    sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.Share_file_with)));

                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();

                }
            });

            alertDialog.show();




            document.close();
        }



    }
    public void createMyPDF(){

        PdfDocument myPdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(300,600,1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

        Paint myPaint = new Paint();
        String myString ="SAHM ATTENDANCE\n" +
                "\n" +
                "Employee Name:\t  \t"+target_name+"\n" +
                "Employee Email:\t  \t"+employee_email+"\n" +
                "Phone: \t\t\t\n" +
                "\n" +
                "Report:\n" +
                "Total Assigned Task:\n" +
                "•\tTask Completed: \t\n" +
                "•\tPending Task:\t\t";// myEditText.getText().toString();
        int x = 10, y=25;
        for (String line:myString.split("\n")){
            myPage.getCanvas().drawText(line, x, y, myPaint);
            y+=myPaint.descent()-myPaint.ascent();
        }
        myPdfDocument.finishPage(myPage);

        File root = new File(Environment.getExternalStorageDirectory(), "Sahm");
        if (!root.exists()) {
            root.mkdirs();
        }
        File textFile = new File( root, target_name+"_Report.pdf");

//        String myFilePath = Environment.getExternalStorageDirectory().getPath() + "/myPDFFile.pdf";
//        File myFile = new File(textFile.toString());
        try {
            myPdfDocument.writeTo(new FileOutputStream(textFile));

            alertDialog.setIcon(R.drawable.applogo );
            alertDialog.setTitle(getResources().getString(R.string.File_Created));
            alertDialog.setMessage(getResources().getString(R.string.Saved_in_InternalStorage));
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.Share), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/*");
                    Uri uri = FileProvider.getUriForFile(salary_calculate.this, BuildConfig.APPLICATION_ID + ".provider", textFile);
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, uri );
                    sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.Share_file_with)));

                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();

                }
            });

            alertDialog.show();





        }
        catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, getResources().getString(R.string.Error), Toast.LENGTH_SHORT).show();
        }

        myPdfDocument.close();
    }
    public void LoadBannerAdd()
    {
        mAdView.setVisibility(View.VISIBLE);
        AdRequest adRequest1 = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest1);
    }

}