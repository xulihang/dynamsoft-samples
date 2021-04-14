package com.dynamsoft.camerax;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HistoryActivity extends AppCompatActivity {
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        listView=findViewById(R.id.HistoryListView);
        loadHistory();
    }

    private void loadHistory(){
        ArrayList<CodeData> arrayList = new ArrayList<CodeData>();
        File directory = this.getExternalFilesDir(null);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            File f = files[i];
            String name=f.getName();
            if (name.endsWith(".jpg")){
                Long timestamp = Long.valueOf(name.replaceAll(".jpg",""));
                String txtname = name.replaceAll(".jpg",".txt");
                Date date = new Date(timestamp);
                File txtFile = new File(directory,txtname);
                Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                arrayList.add(new CodeData(i+1,DateFormat.getDateTimeInstance().format(date), readText(txtFile),bitmap ));
            }
        }
        CustomAdapter customAdapter = new CustomAdapter(this, arrayList);
        listView.setAdapter(customAdapter);
    }

    private String readText(File file){
        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return text.toString();
    }

    public void clearHistoryButton_Clicked(View view){
        File directory = this.getExternalFilesDir(null);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            File f = files[i];
            f.delete();
        }
        loadHistory();
    }

    class CustomAdapter implements ListAdapter {
        ArrayList<CodeData> arrayList;
        Context context;
        public CustomAdapter(Context context, ArrayList<CodeData> arrayList) {
            this.arrayList=arrayList;
            this.context=context;
        }
        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }
        @Override
        public boolean isEnabled(int position) {
            return true;
        }
        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
        }
        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
        }
        @Override
        public int getCount() {
            return arrayList.size();
        }
        @Override
        public Object getItem(int position) {
            return position;
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public boolean hasStableIds() {
            return false;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CodeData cd = arrayList.get(position);
            if(convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                convertView = layoutInflater.inflate(R.layout.history_item, null);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
                TextView tr = convertView.findViewById(R.id.textResult);
                ImageView iv = convertView.findViewById(R.id.captureImageView);
                tr.setText(cd.TimeStamp+"\n"+cd.Code);
                iv.setImageBitmap(cd.Image);
            }
            return convertView;
        }
        @Override
        public int getItemViewType(int position) {
            return position;
        }
        @Override
        public int getViewTypeCount() {
            return 1;
        }
        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    class CodeData {
        int index;
        String TimeStamp;
        String Code;
        Bitmap Image;
        public CodeData(int index, String timeStamp, String code, Bitmap image) {
            this.index=index;
            this.TimeStamp = timeStamp;
            this.Code = code;
            this.Image = image;
        }
    }

}