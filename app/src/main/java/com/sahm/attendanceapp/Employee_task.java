package com.sahm.attendanceapp;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.List;

public class Employee_task extends AppCompatActivity {
    LinearLayout completed,pending;
    TextView textview_pending, textview_completed;
    ListView listBar;
    Button newtask,reports;
    String Manager_email,usertype,manager,employee_email,target_name ,employee_phone,target_username;
    List<Task_list> array_task_compeleted = new ArrayList<>();
    List<Task_list> array_task_pending = new ArrayList<>();
    SharedPreferences sp;
    ProgressDialog progressDialog;
    task_list_adapter adapter_main,adapter_pending;
    File file;
    String select="1";
    int task_no=0;
    String press;
    int completetask=0,pending_task=0;

    AlertDialog alertDialog;
    private AdView mAdView;

    ImageView backarrow;
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

                Manager_email = sp.getString("email",manager).trim();
            }
            else if (v.equals("employee")){
                Intent intent = new Intent(Employee_task.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(Employee_task.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }
        setContentView(R.layout.activity_employee_task);

        completed = findViewById(R.id.completed);
        pending = findViewById(R.id.pending);
        listBar = findViewById(R.id.listview);
        newtask = findViewById(R.id.new_task);
        reports = findViewById(R.id.generate_report);
        textview_completed = findViewById(R.id.text_completed);
        textview_pending = findViewById(R.id.text_pending);

        backarrow=findViewById(R.id.backarrow);
        progressDialog = new ProgressDialog(this);
        mAdView = findViewById(R.id.adView);

        alertDialog = new AlertDialog.Builder(Employee_task.this).create();


        employee_email = getIntent().getStringExtra("target_email");
        target_name = getIntent().getStringExtra("target_name");
        employee_phone = getIntent().getStringExtra("target_phone");
        target_username = getIntent().getStringExtra("target_username");
        progressDialog.setTitle(getResources().getString(R.string.Fetching_Data));
        LoadBannerAdd();
        Query query = FirebaseDatabase.getInstance().getReference("Employee_Task")
                .orderByChild("Manager").equalTo(Manager_email);





        progressDialog.show();


        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child("Employee_Email").getValue().toString().equals(employee_email)) {

                            if (snapshot.child("Status").getValue().toString().equals("Not Completed")) {
                                Task_list task_list = new Task_list();
                                task_list.task_name = snapshot.child("Task_Name").getValue().toString();
                                task_list.task_description = snapshot.child("Task_Description").getValue().toString();
                                task_list.status = snapshot.child("Status").getValue().toString();
                                task_list.manager_email = snapshot.child("Manager").getValue().toString();
                                task_list.file_url = snapshot.child("File_url").getValue().toString();
                                task_list.file_name = snapshot.child("File_name").getValue().toString();
                                task_list.expiray_date = snapshot.child("Expiry_Date").getValue().toString();
                                task_list.employee_email = snapshot.child("Employee_Email").getValue().toString();
                                task_list.task_key = snapshot.child("key").getValue().toString();
                                array_task_pending.add(task_list);
                                task_list = null;
                                pending_task++;
                            } else if (snapshot.child("Status").getValue().toString().equals("Completed")) {
                                Task_list task_list = new Task_list();
                                task_list.task_name = snapshot.child("Task_Name").getValue().toString();
                                task_list.task_description = snapshot.child("Task_Description").getValue().toString();
                                task_list.status = snapshot.child("Status").getValue().toString();
                                task_list.manager_email = snapshot.child("Manager").getValue().toString();
                                task_list.file_url = snapshot.child("File_url").getValue().toString();
                                task_list.file_name = snapshot.child("File_name").getValue().toString();
                                task_list.expiray_date = snapshot.child("Expiry_Date").getValue().toString();
                                task_list.employee_email = snapshot.child("Employee_Email").getValue().toString();
                                task_list.task_key = snapshot.child("key").getValue().toString();
                                array_task_compeleted.add(task_list);
                                task_list = null;
                                completetask++;
                            }

                        }
                    }
                    adapter_main = new task_list_adapter(array_task_pending,getBaseContext(),Employee_task.this);
                    listBar.setAdapter(adapter_main);
                    adapter_main.notifyDataSetChanged();
                    progressDialog.dismiss();

                }
                else
                {
                    Toast.makeText(Employee_task.this,  getResources().getString(R.string.No_Task_found), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

                progressDialog.dismiss();

            }
        });



        completed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                press="Completed";
                adapter_main = new task_list_adapter(array_task_compeleted,getBaseContext(),Employee_task.this);
                listBar.setAdapter(null);
                listBar.setAdapter(adapter_main);
                adapter_main.notifyDataSetChanged();
                textview_completed.setTextColor(getResources().getColor(R.color.white));

                completed.setBackgroundResource(R.drawable.buttondesign);
                pending.setBackgroundResource(0);

                textview_pending.setTextColor(getResources().getColor(android.R.color.black));



            }
        });

        pending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                press="Not Completed";
                adapter_pending = new task_list_adapter(array_task_pending,getBaseContext(),Employee_task.this);
                listBar.setAdapter(null);
                listBar.setAdapter(adapter_pending);
                adapter_pending.notifyDataSetChanged();

                pending.setBackgroundResource(R.drawable.buttondesign);
                completed.setBackgroundResource(0);


                textview_completed.setTextColor(getResources().getColor(android.R.color.black));
                textview_pending.setTextColor(getResources().getColor(android.R.color.white));

            }
        });
        newtask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Employee_task.this, WorkAssign.class);
                intent.putExtra("employee_email",employee_email);
                intent.putExtra("employee_username",target_username);
                intent.putExtra("employee_name",target_name);
                startActivity(intent);
            }
        });
        reports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkPermission();
//                createMyPDF();
//                Intent intent = new Intent(Employee_task.this, Employee_reportview.class);
//                intent.putExtra("employee_email",employee_email);
//                startActivity(intent);
            }
        });
        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Employee_task.super.onBackPressed();

            }
        });

    }



    private void checkPermission() {
        int result = ContextCompat.checkSelfPermission(Employee_task.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            createMyPDF();
        } else {
            requestPermission();
        }
    }
    private void requestPermission() {

//        if (ActivityCompat.shouldShowRequestPermissionRationale(PlayerProgressRecording.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//            Toast.makeText(PlayerProgressRecording.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
//        } else {
        ActivityCompat.requestPermissions(Employee_task.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                    createMyPDF();
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                    Toast.makeText(this,  getResources().getString(R.string.Permission_Required), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    public void create_file()
    {

        File root = new File(Environment.getExternalStorageDirectory(), "Sahm");
        if (!root.exists()) {
            root.mkdirs();
        }
        File textFile = new File( root, "my_file00.txt");
        try{
            FileOutputStream fos = new FileOutputStream(textFile);
            fos.write("abhx".getBytes());
            fos.close();

            Toast.makeText(this,  getResources().getString(R.string.File_saved), Toast.LENGTH_SHORT).show();

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/*");
            Uri uri = FileProvider.getUriForFile(Employee_task.this, BuildConfig.APPLICATION_ID + ".provider", textFile);
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri );
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(sharingIntent,  getResources().getString(R.string.Share_file_with)));


        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static Bitmap loadBitmapFromView(View v, int width, int height) {
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }




    private void createMyPDF() {

        // TODO Auto-generated method stub
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();

        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sahm";

            File dir = new File(path);
            if(!dir.exists())
                dir.mkdirs();



            file = new File(dir, "Report_"+target_username+".pdf");
            FileOutputStream fOut = new FileOutputStream(file);

            PdfWriter.getInstance(document, fOut);

            //open the document
            document.open();


            Paragraph p1 = new Paragraph("SAHM TIME TASK REPORT");
            Font f = new Font(Font.FontFamily.TIMES_ROMAN, 25.0f, Font.BOLD, BaseColor.BLACK);
            p1.setFont(f);
            p1.setAlignment(Paragraph.ALIGN_CENTER);

            //add paragraph to document
            document.add(p1);

            Paragraph p2 = new Paragraph(
                    "\nName: "+target_name+
                            "\nUserName: "+target_username+
                                "\nTotal Assigned Task:\n" +
                                "•\tTask Completed: "+completetask+"\n" +
                                    "•\tPending Task: "+pending_task);
            document.add(p2);
            PdfPTable table = new PdfPTable(4);
            table.setSpacingBefore(24);
            table.setWidthPercentage(100);

            table.setWidths(new int[]{1,3,7,2});

            BaseColor myColor2 = WebColors.getRGBColor("#ffffff");
            Font fontH1 = new Font(Font.FontFamily.TIMES_ROMAN,12,Font.BOLD,myColor2 );
            PdfPCell cell1 = new PdfPCell(new Phrase("No.",fontH1));
            PdfPCell cell2 = new PdfPCell(new Phrase("Task Name",fontH1));
            PdfPCell cell3 = new PdfPCell(new Phrase("Description",fontH1));
            PdfPCell cell4 = new PdfPCell(new Phrase("Status",fontH1));
            BaseColor myColor = WebColors.getRGBColor("#42b6ff");
            cell1.setBorder(Rectangle.RIGHT);
            cell2.setBorder(Rectangle.RIGHT);
            cell3.setBorder(Rectangle.RIGHT);
            cell4.setBorder(Rectangle.NO_BORDER);
            cell1.setBackgroundColor(myColor);
            cell2.setBackgroundColor(myColor);
            cell3.setBackgroundColor(myColor);
            cell4.setBackgroundColor(myColor);

            cell1.setVerticalAlignment(Element.ALIGN_CENTER);
            cell1.setHorizontalAlignment(cell1.ALIGN_CENTER);
            cell1.setVerticalAlignment(cell1.ALIGN_CENTER);
            cell2.setHorizontalAlignment(cell1.ALIGN_CENTER);
            cell2.setVerticalAlignment(cell1.ALIGN_CENTER);
            cell3.setHorizontalAlignment(cell1.ALIGN_CENTER);
            cell3.setVerticalAlignment(cell1.ALIGN_CENTER);
            cell4.setHorizontalAlignment(cell1.ALIGN_CENTER);
            cell4.setVerticalAlignment(cell1.ALIGN_CENTER);
            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);
//            document.add(table);


            BaseColor myColor3 = WebColors.getRGBColor("#EEEEEE");
            BaseColor myColor4 = WebColors.getRGBColor("#e3e3e3");
            Font fontH2 = new Font(Font.FontFamily.TIMES_ROMAN,10 );



            int size = array_task_compeleted.size();
            for(int i=0;i<size;i++)
            {
                task_no++;

                if(select.equals("1"))
                {

                    cell1 = new PdfPCell(new Phrase(task_no+"."));
                    cell2 = new PdfPCell(new Phrase(array_task_compeleted.get(i).task_name,fontH2));
                    cell3 = new PdfPCell(new Phrase(array_task_compeleted.get(i).task_description,fontH2));
                    cell4 = new PdfPCell(new Phrase(array_task_compeleted.get(i).status,fontH2));


                    cell1.setBackgroundColor(myColor3);
                    cell2.setBackgroundColor(myColor3);
                    cell3.setBackgroundColor(myColor3);
                    cell4.setBackgroundColor(myColor3);


                    select="0";
                }
                else
                {


                    cell1 = new PdfPCell(new Phrase(task_no+"."));
                    cell2 = new PdfPCell(new Phrase(array_task_compeleted.get(i).task_name,fontH2));
                    cell3 = new PdfPCell(new Phrase(array_task_compeleted.get(i).task_description,fontH2));
                    cell4 = new PdfPCell(new Phrase(array_task_compeleted.get(i).status,fontH2));

                    cell1.setBackgroundColor(myColor4);
                    cell2.setBackgroundColor(myColor4);
                    cell3.setBackgroundColor(myColor4);
                    cell4.setBackgroundColor(myColor4);

                    select="1";
                }


                cell1.setBorder(Rectangle.RIGHT);
                cell2.setBorder(Rectangle.RIGHT);
                cell3.setBorder(Rectangle.RIGHT);
                cell4.setBorder(Rectangle.NO_BORDER);
                cell1.setVerticalAlignment(Element.ALIGN_CENTER);
                cell1.setHorizontalAlignment(cell1.ALIGN_CENTER);
                cell1.setVerticalAlignment(cell1.ALIGN_CENTER);
                cell2.setHorizontalAlignment(cell1.ALIGN_CENTER);
                cell2.setVerticalAlignment(cell1.ALIGN_CENTER);
                cell3.setHorizontalAlignment(cell1.ALIGN_CENTER);
                cell3.setVerticalAlignment(cell1.ALIGN_CENTER);
                cell4.setHorizontalAlignment(cell1.ALIGN_CENTER);
                cell4.setVerticalAlignment(cell1.ALIGN_CENTER);
                table.addCell(cell1);
                table.addCell(cell2);
                table.addCell(cell3);
                table.addCell(cell4);


            }



            int size2 = array_task_pending.size();
            for(int i=0;i<size2;i++)
            {
                task_no++;

                if(select.equals("1"))
                {

                    cell1 = new PdfPCell(new Phrase(task_no+"."));
                    cell2 = new PdfPCell(new Phrase(array_task_pending.get(i).task_name,fontH2));
                    cell3 = new PdfPCell(new Phrase(array_task_pending.get(i).task_description,fontH2));
                    cell4 = new PdfPCell(new Phrase(array_task_pending.get(i).status,fontH2));


                    cell1.setBackgroundColor(myColor3);
                    cell2.setBackgroundColor(myColor3);
                    cell3.setBackgroundColor(myColor3);
                    cell4.setBackgroundColor(myColor3);


                    select="0";
                }
                else
                {


                    cell1 = new PdfPCell(new Phrase(task_no+"."));
                    cell2 = new PdfPCell(new Phrase(array_task_pending.get(i).task_name,fontH2));
                    cell3 = new PdfPCell(new Phrase(array_task_pending.get(i).task_description,fontH2));
                    cell4 = new PdfPCell(new Phrase(array_task_pending.get(i).status,fontH2));

                    cell1.setBackgroundColor(myColor4);
                    cell2.setBackgroundColor(myColor4);
                    cell3.setBackgroundColor(myColor4);
                    cell4.setBackgroundColor(myColor4);

                    select="1";
                }


                cell1.setBorder(Rectangle.RIGHT);
                cell2.setBorder(Rectangle.RIGHT);
                cell3.setBorder(Rectangle.RIGHT);
                cell4.setBorder(Rectangle.NO_BORDER);
                cell1.setVerticalAlignment(Element.ALIGN_CENTER);
                cell1.setHorizontalAlignment(cell1.ALIGN_CENTER);
                cell1.setVerticalAlignment(cell1.ALIGN_CENTER);
                cell2.setHorizontalAlignment(cell1.ALIGN_CENTER);
                cell2.setVerticalAlignment(cell1.ALIGN_CENTER);
                cell3.setHorizontalAlignment(cell1.ALIGN_CENTER);
                cell3.setVerticalAlignment(cell1.ALIGN_CENTER);
                cell4.setHorizontalAlignment(cell1.ALIGN_CENTER);
                cell4.setVerticalAlignment(cell1.ALIGN_CENTER);
                table.addCell(cell1);
                table.addCell(cell2);
                table.addCell(cell3);
                table.addCell(cell4);

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
                    Uri uri = FileProvider.getUriForFile(Employee_task.this, BuildConfig.APPLICATION_ID + ".provider",file);
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


    public void createMyPDF11()
    {
        PdfDocument myPdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(300,600,1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

        Paint myPaint = new Paint();
        String myString ="SAHM ATTENDANCE\n" +
                "\n" +
                "Employee Name:\t  \t"+target_name+"\n" +
                "Employee Email:\t  \t"+employee_email+"\n" +
                "Phone: \t\t\t"+employee_phone+"\n" +
                "\n" +
                "Report:\n" +
                "Total Assigned Task:\n" +
                "•\tTask Completed: \t"+completetask+"\n" +
                "•\tPending Task:\t\t"+pending_task;// myEditText.getText().toString();
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
            alertDialog.setTitle( getResources().getString(R.string.File_Created));
            alertDialog.setMessage( getResources().getString(R.string.saved_in_InternalStorage));
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,  getResources().getString(R.string.Share), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/*");
                    Uri uri = FileProvider.getUriForFile(Employee_task.this, BuildConfig.APPLICATION_ID + ".provider", textFile);
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, uri );
                    sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(sharingIntent,  getResources().getString(R.string.Share_file_with)));

                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,  getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();

                }
            });

            alertDialog.show();





        }
        catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this,  getResources().getString(R.string.Error), Toast.LENGTH_SHORT).show();
        }

        myPdfDocument.close();
    }


    // Method for opening a pdf file
    private void viewPdf(String file, String directory) {

        File pdfFile = new File(Environment.getExternalStorageDirectory() + "/" + directory + "/" + file);
        Uri path = Uri.fromFile(pdfFile);

        // Setting the intent for pdf reader
        Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
        pdfIntent.setDataAndType(path, "application/pdf");
        pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            startActivity(pdfIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(Employee_task.this,  getResources().getString(R.string.Cannot_able_to_read), Toast.LENGTH_SHORT).show();
        }
    }
    public void LoadBannerAdd()
    {
        mAdView.setVisibility(View.VISIBLE);
        AdRequest adRequest1 = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest1);
    }





}
