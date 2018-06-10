package cn.edu.bistu.cs.se.beiwanglu;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 *
 * @author cjianquan
 * @since 2014年10月31日
 */
public class AddBwlActivity extends Activity {

    private EditText etDate = null,etTime=null,etTitle=null,etContent=null;
    private Button btnSave = null;

    static final int DATE_DIALOG_ID = 0;
    static final int TIME_DIALOG_ID = 1;

    private DbHelper dbhelper;

    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_bwl);

        dbhelper = new DbHelper(this, "db_bwl", null, 1);

        etTitle = (EditText)findViewById(R.id.etTitle);
        etContent = (EditText)findViewById(R.id.etContent);

        etDate = (EditText)findViewById(R.id.etDate);
        etDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //并会调用 onCreateDialog(int)回调函数来请求一个Dialog
                showDialog(DATE_DIALOG_ID);

            }
        });

        etTime = (EditText)findViewById(R.id.etTime);
        etTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //并会调用 onCreateDialog(int)回调函数来请求一个Dialog
                showDialog(TIME_DIALOG_ID);

            }
        });


        btnSave = (Button)findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                ContentValues value = new ContentValues();

                String title = etTitle.getText().toString();
                String content = etContent.getText().toString();
                String noticeDate = etDate.getText().toString();
                String noticeTime = etTime.getText().toString();

                value.put("title", title);
                value.put("content", content);
                value.put("noticeDate", noticeDate);
                value.put("noticeTime", noticeTime);


                SQLiteDatabase db = dbhelper.getWritableDatabase();

                long id = 0;

                long status = 0;
                if(bundle!=null){
                    id = bundle.getLong("id");
                    status = db.update("tb_bwl", value, "id=?", new String[]{bundle.getLong("id")+""});
                }else{
                    status = db.insert("tb_bwl", null, value);
                    id = status;
                }

                if(status!=-1){
                    setAlarm(id);
                    Toast.makeText(AddBwlActivity.this, "保存成功", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(AddBwlActivity.this, "保存失败", Toast.LENGTH_LONG).show();
                }
            }
        });

        //获取上一个activity的传值
        bundle = this.getIntent().getExtras();
        if(bundle!=null){
            etDate.setText(bundle.getString("noticeDate"));
            etTime.setText(bundle.getString("noticeTime"));
            etTitle.setText(bundle.getString("title"));
            etContent.setText(bundle.getString("content"));
        }

    }



    private OnDateSetListener dateSetListener = new OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {

            StringBuilder dateStr = new StringBuilder();
            dateStr.append(year).append("-")
                    .append(month+1).append("-")
                    .append(day);

            etDate.setText(dateStr.toString());
        }
    };


    private OnTimeSetListener timeSetListener = new OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {


            StringBuilder timeStr = new StringBuilder();
            timeStr.append(hour).append(":")
                    .append(minute);

            etTime.setText(timeStr.toString());
        }
    };

    /**
     * 当Activity调用showDialog函数时会触发该函数的调用
     */
    protected Dialog onCreateDialog(int id){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        switch(id){
            case DATE_DIALOG_ID:
                DatePickerDialog dpd = new DatePickerDialog(this,dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                dpd.setCancelable(true);
                dpd.setTitle("选择日期");
                dpd.show();
                break;
            case TIME_DIALOG_ID:
                TimePickerDialog tpd = new TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
                tpd.setCancelable(true);
                tpd.setTitle("选择时间");
                tpd.show();
                break;
            default:
                break;
        }
        return null;
    }

    private AlarmManager alarmManager=null;


    public void setAlarm(long id){

        Log.e("AndroidBWL", "setAlarm start...");


        String noticeDate = etDate.getText().toString();
        String noticeTime = etTime.getText().toString();

        Calendar calendar = Calendar.getInstance();

        calendar.set(Integer.parseInt(noticeDate.split("-")[0]),
                Integer.parseInt(noticeDate.split("-")[1])-1,
                Integer.parseInt(noticeDate.split("-")[2]),
                Integer.parseInt(noticeTime.split(":")[0]),
                Integer.parseInt(noticeTime.split(":")[1]));

        Log.e("AndroidBWL", ""+(calendar.getTimeInMillis()-System.currentTimeMillis()));



        alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);


        Intent intent = new Intent(AddBwlActivity.this, AlarmReceiver.class); //创建Intent对象

        Bundle bundle = new Bundle();
        bundle.putLong("id", id);
        bundle.putString("title", etTitle.getText().toString());
        bundle.putString("content", etContent.getText().toString());
        bundle.putString("noticeDate", etDate.getText().toString());
        bundle.putString("noticeTime", etTime.getText().toString());

        intent.putExtras(bundle);

        //PendingIntent.getBroadcast intent 数据不更新。
        //传不同的 action 来解决这个问题
        intent.setAction("ALARM_ACTION"+calendar.getTimeInMillis());

        PendingIntent pi = PendingIntent.getBroadcast(AddBwlActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT); //创建PendingIntent

        //参数说明：http://www.eoeandroid.com/blog-119358-2995.html
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()+5000, pi); //设置闹钟，当前时间就唤醒

        Log.e("AndroidBWL", "setAlarm end...");

    }

}