package com.example.bill.keepfit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Pedometer2Activity extends AppCompatActivity {

    private static long STEP_COUNT = 0;
    private EditText editText;
    private TextView mTvStep;
    private TextView tvchoicestep;
    private TextView tvAdding;
    private int helpInt;
    private String dataValue;
    private long stepsToStartAgain;
    private String helpName;
    private long newStepsStore;
    private SQLiteDatabase database;
    private String curDate;
    private static final String DB_NAME = "MyHelpdb.db";
    private static final String TABLE_NAME = "time_tbl_WG";
    private int activeGoal=0;
    private float st;
    private float st1;
    private String dateTime;
    private String dateTM;
    private Integer numberTM;
    private Integer stepsTM;
    private String unitSelect;
    private String helpUnit;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedometer2);

        //hereeeeeeeeeeeeeeeeeeeeeeeeee i open the shared preferences of the test mode
        SharedPreferences testModePreferences = this.getSharedPreferences("textModeSetting", MODE_PRIVATE);
        dateTM=testModePreferences.getString("date", null);
        numberTM=testModePreferences.getInt("testM",0);


        //print the active goal just for reference
        print();
        //retract the incoming intent
        Intent intent = getIntent();
        //recover the row that we want to edit
        dataValue = intent.getExtras().getString("string");
        //split the whole string to parts
        String data[] =dataValue.split(" ");
        //store the int value that we want to edit
        helpInt=Integer.parseInt(data[data.length-1].replace("]",""));//the total steps of one specific goal
        helpUnit=data[data.length-2].replace(":","");
        System.out.println("Arxikh: " +helpUnit);
        //store the name of the goal that was chosen
        helpName=data[data.length-4];

        //initialize the Texts
        editText=(EditText) findViewById(R.id.editgoal);
        tvchoicestep=(TextView) findViewById(R.id.tv_choice_step);
        mTvStep=(TextView) findViewById(R.id.tv_current);
        tvAdding=(TextView) findViewById(R.id.tv_adding);



        //shared preferences for the data number to start again
        SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", MODE_WORLD_READABLE);
        float prefNameSteps = myPrefs.getFloat("MyData2", 0);
        System.out.println("Epanekinhsh me timh: "+prefNameSteps);
        //here i convert the units if there is a difference between them
        if(helpUnit.equals(PreviousUnit())) {
            System.out.println("Oi monades einai idies");
            stepsToStartAgain = (long) prefNameSteps;
        }else{
            System.out.println("Oi monades den einai idies");
            stepsToStartAgain= doTheConvertionWhenGoalsChanged(helpUnit,prefNameSteps);
        }

        //after changing from test mode to main mode you need to start from where you ve stopped before
        if(numberTM==0){
            if(stepsToStartAfterChangingTestMode()!=null) {
                //here again i check the units
                if(helpUnit.equals(PreviousUnit())) {
                    stepsToStartAgain = stepsToStartAfterChangingTestMode();
                }else{
                    stepsToStartAgain= doTheConvertionWhenGoalsChanged(helpUnit,stepsToStartAfterChangingTestMode());
                }
              //  stepsToStartAgain = stepsToStartAfterChangingTestMode();
            }
        }


        //return the current date that the goal started
        if(numberTM==0){
            curDate=getCurrentDate();
        }else{
            curDate=dateTM;
        }


        if(checkIsTheSame() != null && !checkIsTheSame().isEmpty() && checkIsTheSame().equals(helpName)  ) {
            System.out.println("bika sto shared preference ara 1");
            System.out.println("ektos kai an einai test mode ara 0 sthn prwth fora " +activeGoal);
            activeGoal=myPrefs.getInt("MyData4", 0);
        }else{
            activeGoal=0;
        }

        //here i play with time
        if(checkDifInTime()>=1 && (activeGoal==1 || activeGoal==0)){
            if(numberTM==0) {
                System.out.println("Date changed! clear the data from the table!");
                stepsToStartAgain = 0;
                activeGoal = 0;
                clearCurrentTable();
            }
            else{
                //continue
            }
        }


        //previous steps
        mTvStep.setText("Previous " +helpUnit+ ": " + String.valueOf(stepsToStartAgain));
        tvAdding.setText("");
       //here was the current date before


        //here i chekc if the n-th goal that has entered is the same as the previous active goal and
        //if it is then continue to be one
        //otherwise make it innactive by giving zero to the active state
        System.out.println(helpName);
        System.out.println(checkIsTheSame());
        if(!helpName.equals(checkIsTheSame()) && checkDifInTime()<1 && checkIsTheSame() != null){
           // System.out.println(checkDifInTime());
            //The database is open!
            ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(this, DB_NAME);
            database = dbOpenHelper.openDataBase();

            try {
                if(numberTM==1){
                    database.execSQL("UPDATE time_tbl_WG SET active='"+0+"' WHERE name='"+checkIsTheSame()+"' AND date='"+dateTM+"'");
                    System.out.println("The previous is not active, 0 has inserted: " +checkIsTheSame());
                    System.out.println("Test mode is ON");
                    System.out.println("null is the name of the checkIsTheSame  after 0 has inserted " +checkIsTheSame());
                }else{
                    database.execSQL("UPDATE time_tbl_WG SET active='"+0+"' WHERE name='"+checkIsTheSame()+"'");
                    System.out.println("The previous is not active, 0 has inserted: " +checkIsTheSame());
                    System.out.println("Test mode is OFF");
                    System.out.println("null is the name of the checkIsTheSame  after 0 has inserted " +checkIsTheSame());
                }
            } catch (SQLException e) {
                System.out.println("An error occured");
            }

        }


        tvchoicestep.setText(" / " +"GOAL : " + String.valueOf(helpInt));
        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().trim().equals("")) {
                        //nothing happened
                    newStepsStore=stepsToStartAgain+0;
                } else {


                if (stepsToStartAgain == 0) {
                    //  STEP_COUNT = 0;
                    newStepsStore = Long.parseLong(editText.getText().toString().trim());
                   // if(newStepsStore>helpInt){
                   //     newStepsStore=helpInt;
                  //  }
                    // setStep(STEP_COUNT);
                    newStepsStore= convertStepsToAnotherUnit();
                    System.out.println("1");

                } else {
                    System.out.println("2");
                    if (stepsToStartAgain > helpInt) {
                      //  STEP_COUNT = 0;
                       // newStepsStore = 0;
                        //add the previous steps
                        newStepsStore = stepsToStartAgain+Long.parseLong(editText.getText().toString().trim());
                        newStepsStore= convertStepsToAnotherUnit();
                        System.out.println("3");
                    } else {
                        STEP_COUNT = stepsToStartAgain;
                     //   newStepsStore = (Long.parseLong(editText.getText().toString().trim()) + STEP_COUNT);
                        newStepsStore = Long.parseLong(editText.getText().toString().trim());
                        newStepsStore= convertStepsToAnotherUnit();
                        newStepsStore=newStepsStore+STEP_COUNT;
                        System.out.println("4");
                        System.out.println("Einai ta kilometres: " +newStepsStore);
                    }

                }
            }
                //editText.setText("");

               // newStepsStore= convertStepsToAnotherUnit();
                tvAdding.setText("Add Steps: " +Long.parseLong(editText.getText().toString().trim()));
                System.out.println("Current " +helpUnit+": " + newStepsStore);
                System.out.println("Total Goal is: " + helpInt);

            }
        });


    }



    private long doTheConvertionWhenGoalsChanged(String unit, float prefNameSteps) {
        long numberToReturn=0;
        System.out.println("EINAI H TIMH " +prefNameSteps);
            if (unit.equals("Yards")) {
                if (unitReturn().equals("Yards")) {
                    numberToReturn = (long) prefNameSteps;

                } else if (unitReturn().equals("Meters")) {
                    numberToReturn = (long) (prefNameSteps * (1.0936133));

                } else if (unitReturn().equals("Miles")) {
                    numberToReturn = (long) (prefNameSteps * (1760 ));

                } else if (unitReturn().equals("Kilometres")) {
                    numberToReturn = (long) (prefNameSteps * (1093));

                } else {
                    numberToReturn = (long) (prefNameSteps * (0.8333333333333334));

                }
            } else if (unit.equals("Meters")) {

                if (unitReturn().equals("Meters")) {
                    numberToReturn = (long) prefNameSteps;

                } else if (unitReturn().equals("Yards")) {
                    numberToReturn = (long) (prefNameSteps * (0.9144));

                } else if (unitReturn().equals("Miles")) {
                    numberToReturn = (long) (prefNameSteps * (1609));

                } else if (unitReturn().equals("Kilometres")) {
                    numberToReturn = (long) (prefNameSteps * (1000));

                } else {
                    numberToReturn = (long) (prefNameSteps * (0.762));

                }
            } else if (unit.equals("Miles")) {
                if (unitReturn().equals("Miles")) {
                    numberToReturn = (long) prefNameSteps;

                } else if (unitReturn().equals("Yards")) {
                    numberToReturn = (long) (prefNameSteps * (0.000568181818));

                } else if (unitReturn().equals("Meters")) {
                    numberToReturn = (long) (prefNameSteps * (0.000621371192));

                } else if (unitReturn().equals("Kilometres")) {
                    numberToReturn = (long) (prefNameSteps * (0.621371192));

                } else {
                    numberToReturn = (long) (prefNameSteps * (0.0004734848484848485));

                }
            } else if (unit.equals("Kilometres")) {
                System.out.println("Bika kilometra");
                if (unitReturn().equals("Kilometres")) {
                    numberToReturn = (long) prefNameSteps;

                } else if (unitReturn().equals("Yards")) {
                    numberToReturn = (long) (prefNameSteps * (0.0009144));

                } else if (unitReturn().equals("Meters")) {

                    numberToReturn = (long) (prefNameSteps * (0.001));

                } else if (unitReturn().equals("Miles")) {
                    numberToReturn = (long) (prefNameSteps * (1.609344));

                } else {
                    numberToReturn = (long) (prefNameSteps * (0.000762));

                }
            } else if (unit.equals("Steps")) {
                if (unitReturn().equals("Steps")) {
                    numberToReturn = (long) prefNameSteps;

                } else if (unitReturn().equals("Yards")) {
                    numberToReturn = (long) (prefNameSteps * (1.2));

                } else if (unitReturn().equals("Meters")) {
                    numberToReturn = (long) (prefNameSteps * (1.31));

                } else if (unitReturn().equals("Miles")) {
                    numberToReturn = (long) (prefNameSteps * (2112));

                } else {
                    numberToReturn = (long) (prefNameSteps * (1312));

                }
            }

        return numberToReturn;
    }

    private long convertStepsToAnotherUnit() {
        long numberUnit=0;
            if (helpUnit.equals("Meters")) {
                // meters
                newStepsStore = (long) (newStepsStore * (0.762));
            } else if (helpUnit.equals("Yards")) {
                // yards
                newStepsStore = (long) (newStepsStore * (0.83333333));
            } else if (helpUnit.equals("Kilometres")) {
                // kilometress
                newStepsStore = (long) (newStepsStore * (0.000762));
            } else if (helpUnit.equals("Miles")) {
                // miles
                newStepsStore = (long) (newStepsStore * (0.0004734848484848485));
            } else {
                // steps
                newStepsStore = newStepsStore;
            }

        numberUnit=newStepsStore;
        System.out.println("einai oi monades: " +numberUnit);
        //return (int) numberUnit;
        return numberUnit;
    }

    private Integer stepsToStartAfterChangingTestMode() {
        //The database is open!
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(this, DB_NAME);
        database = dbOpenHelper.openDataBase();

        Cursor cursor = database.rawQuery("select didsteps from time_tbl_WG where active='" + 1 + "'", null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {

            do {

                stepsTM = cursor.getInt(cursor.getColumnIndex("didsteps"));


            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        System.out.println(stepsTM);
        return stepsTM;
    }


    // Physical back button click handler
    @Override
    public void onBackPressed() {

        if (editText.getText().toString().trim().equals("")) {
            //default value 0 if user doesnt insert a number
            newStepsStore=stepsToStartAgain+0;
        }

            String dataa[] = tvchoicestep.getText().toString().split(" ");
            //store the int value that we want to edit
          //  float st = Float.parseFloat(data[data.length - 1]);
            st= newStepsStore;
            st1 = Float.parseFloat(dataa[dataa.length - 1]);
            //percentage of the current steps/total steps
            String data1 = String.valueOf((st / st1));
            //use of shared preferences
            SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", MODE_WORLD_READABLE);
            SharedPreferences.Editor prefsEditor = myPrefs.edit();
            prefsEditor.putString("MyData", data1);//percentage
            prefsEditor.putFloat("MyData2", st);//current steps
            prefsEditor.putString("MyData3", helpName);//name of the current goal
            System.out.println("Steps to start again are: " + (int) st);


            if(activeGoal==0){
                activeGoal=1;
                prefsEditor.putInt("MyData4", activeGoal);
                System.out.println("Turn out to  1 <<ACTIVE>> " +helpName);
                //check if the name exists in the table
                boolean b=rowNameExists(helpName);
                //check again if exists with the same date
                if(b==true && dateFound(dateTM)==true){
                    //for the test mode check if is active or not
                    System.out.println("To b einai true");
                    if(numberTM==1){
                        System.out.println("to test mode einai 1");
                        updateDB(helpName,helpInt, (int) st, Float.parseFloat(data1),activeGoal,dateTM);
                    }else{
                        System.out.println("to test mode den einai 1");
                        updateDB(helpName,helpInt, (int) st, Float.parseFloat(data1),activeGoal,curDate);
                    }

                }else{
                    System.out.println("To b einai false");
                    if(numberTM==1){
                        System.out.println("to test mode einai 1");
                        //fill the database with the info needed
                        fillDatabase(helpName,helpInt, (int) st,helpUnit ,Float.parseFloat(data1),activeGoal,dateTM);
                    }else{
                        System.out.println("to test mode den einai 1");
                        //fill the database with the info needed
                        fillDatabase(helpName,helpInt, (int) st,helpUnit ,Float.parseFloat(data1),activeGoal,curDate);
                    }

                }
            }else{
                System.out.println("Einai idi active");
                if(numberTM==1){
                    System.out.println("to test mode einai 1");
                    updateDB(helpName,helpInt, (int) st, Float.parseFloat(data1),activeGoal,dateTM);
                }else{
                    System.out.println("to test mode den einai 1");
                    updateDB(helpName,helpInt, (int) st, Float.parseFloat(data1),activeGoal,curDate);
                }

            }
            prefsEditor.commit();
            //clear the edit text
            editText.setText("");
            tvAdding.setText("");
            //here i store to history table
            storeActiveGoal();

            super.finish();
      //  }

    }

    public String checkIsTheSame() {
        String name = null;
        //The database is open!
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(this, DB_NAME);
        database = dbOpenHelper.openDataBase();

        Cursor cursor = database.rawQuery("select name from time_tbl_WG where active='" + 1 + "'", null);
        cursor.moveToFirst();

        if (!cursor.isAfterLast()) {
            // if(cursor.moveToFirst()){
            do {

                name = cursor.getString(cursor.getColumnIndex("name"));



            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
      //  System.out.println("checkIsTheSame: " +name);
        return name;
    }

    public long checkDifInTime() {
        String nameFIrstActive = "";
        nameFIrstActive = checkIsTheSame();
        //The database is open!
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(this, DB_NAME);
        database = dbOpenHelper.openDataBase();

        Cursor cursor = database.rawQuery("select date from time_tbl_WG where name='" + nameFIrstActive + "'", null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {

            do {

                dateTime = cursor.getString(cursor.getColumnIndex("date"));


            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();

        //check the time difference
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        long days = 0;


            if(dateTime != null && !dateTime.isEmpty()) {

                try {
                    System.out.println("Enter sto if");
                    Date oldDate = dateFormat.parse(dateTime);
                    //   System.out.println(oldDate);

                    Date currentDate = dateFormat.parse(curDate);


                    if(oldDate.compareTo(currentDate)==0){
                        days=0;

                    }else{
                        days=1;
                    }

                } catch (ParseException e) {

                    e.printStackTrace();
                }

            }else{
                //didnt exist
                System.out.println("Enter sto else");
            }

        System.out.println("Days: " + days);
        return days;
    }

    public void updateDB(String name, int allSteps, int didSteps, float percentageSteps,int active ,String curDate){
        //The database is open!
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(this, DB_NAME);
        database = dbOpenHelper.openDataBase();

        database.execSQL("UPDATE time_tbl_WG SET allsteps='"+allSteps+"' WHERE name='"+helpName+"'");
        database.execSQL("UPDATE time_tbl_WG SET didsteps='"+didSteps+"' WHERE name='"+helpName+"'");
        database.execSQL("UPDATE time_tbl_WG SET percentage='"+percentageSteps+"' WHERE name='"+helpName+"'");
        //refresh the active part
        database.execSQL("UPDATE time_tbl_WG SET active='"+active+"' WHERE name='"+helpName+"'");

        database.close();
    }


    public void fillDatabase(String name, int allSteps, int didSteps,String unitGoal ,float percentageSteps,int active ,String curDate ){
        //The database is open!
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(this, DB_NAME);
        database = dbOpenHelper.openDataBase();
        //insert data into able
     //   if(numberTM==1){
      //      System.out.println("Eisagwgh me test mode 1");
      //      database.execSQL("insert into time_tbl_WG values('"+name+"','"+allSteps+"','"+didSteps+"','"+percentageSteps+"','"+active+"','"+dateTM+"')");
      //  }else{
       //     System.out.println("Eisagwgh me test mode 0");
            database.execSQL("insert into time_tbl_WG values('"+name+"','"+allSteps+"','"+didSteps+"','"+unitGoal+"','"+percentageSteps+"','"+active+"','"+curDate+"')");
      //  }

        database.close();
    }

    public String getCurrentDate(){
        Date curDate = new Date();
      //  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String DateToStr = format.format(curDate);
     //   System.out.println(DateToStr);
        return DateToStr;
    }

    public boolean rowNameExists(String name){
        Boolean bValue=false;
        //The database is open!
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(this, DB_NAME);
        database = dbOpenHelper.openDataBase();

        Cursor cursor = database.rawQuery("select name from time_tbl_WG", null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {

            do {
                 String nameRow = cursor.getString(cursor.getColumnIndex("name"));
                if(nameRow.equals(name)){
                    bValue=true;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return bValue;
    }

    public boolean dateFound(String dateName){
        Boolean dateValue=false;
        //The database is open!
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(this, DB_NAME);
        database = dbOpenHelper.openDataBase();

        Cursor cursor = database.rawQuery("select name from time_tbl_WG where date='" + dateName + "'", null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {

            do {
                String nameRow = cursor.getString(cursor.getColumnIndex("name"));
                if(nameRow.equals(helpName)){
                    dateValue=true;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return dateValue;
    }


    public void storeActiveGoal(){
        String name="";
        Integer steps=0;
        Integer stepsDid=0;
        float percentage=0f;
        String dateSearch="";
        Integer activeNumber=0;
        SQLiteDatabase databasehelp;
        //here i store the active goal that i want to transfer
        //The database is open!
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(this, DB_NAME);
        database = dbOpenHelper.openDataBase();

        Cursor cursor = database.rawQuery("select * from time_tbl_WG where active='"+1+"'", null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {

            do {
                System.out.println("Collect the data from the active goal");
                name = cursor.getString(cursor.getColumnIndex("name"));
                steps = cursor.getInt(cursor.getColumnIndex("allsteps"));
                stepsDid = cursor.getInt(cursor.getColumnIndex("didsteps"));
                unitSelect=cursor.getString(cursor.getColumnIndex("unit"));
                percentage = cursor.getFloat(cursor.getColumnIndex("percentage"));
                dateSearch = cursor.getString(cursor.getColumnIndex("date"));
                activeNumber=cursor.getInt(cursor.getColumnIndex("active"));

            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();

        //open the database
        ExternalDbOpenHelper dbOpenHelper1 = new ExternalDbOpenHelper(this, "MyHistorydb.db");
        databasehelp = dbOpenHelper1.openDataBase();
        //here test mode again
        System.out.println("einai to test mode: " +numberTM);
        System.out.println(dateSearch);
        System.out.println(dateTM);


        if(numberTM==1 && dateSearch.equals(dateTM)){
            System.out.println(name);
            if(name.equals(checkIsTheSame2())&& (checkIsTheDate().compareTo(curDate) == 0)){ //&& exists!=0 ){
                System.out.println("Exists in the history table");
                databasehelp.execSQL("UPDATE history_tbl_WG SET allsteps='"+steps+"' WHERE name='"+name+"'");
                databasehelp.execSQL("UPDATE history_tbl_WG SET didsteps='"+stepsDid+"' WHERE name='"+name+"'");
                databasehelp.execSQL("UPDATE history_tbl_WG SET percentage='"+percentage+"' WHERE name='"+name+"'");
            }else if(!name.equals(checkIsTheSame2()) && activeNumber==1 && (checkIsTheDate().compareTo(curDate) == 0)) {
                databasehelp.execSQL("DELETE FROM history_tbl_WG WHERE active='"+1+"' AND date='"+dateSearch+"'");
                databasehelp.execSQL("insert into history_tbl_WG values('" + name + "','" + steps + "','" + stepsDid + "','"+unitSelect+"','" + percentage  + "','" + activeNumber + "','"+2+"','" + dateTM + "')");
            }
            else{
                    System.out.println("insert apo to test mode mias kai den exist");
                    databasehelp.execSQL("insert into history_tbl_WG values('" + name + "','" + steps + "','" + stepsDid + "','"+unitSelect+"','" + percentage  + "','" + activeNumber + "','"+2+"','" + dateTM + "')");
                }
            }


        if(numberTM==0 ) {
           // if ((dateSearch.compareTo(curDate) < 0) && dateSearch != null && dateSearch != "") {
                System.out.println("palia: " + dateSearch);
                System.out.println("Nea: " + curDate);
            if(name.equals(checkIsTheSame2())&& (checkIsTheDate().compareTo(curDate) == 0)){ //&& exists!=0 ){
                System.out.println("Exists in the history table");
                databasehelp.execSQL("UPDATE history_tbl_WG SET allsteps='"+steps+"' WHERE name='"+name+"'");
                databasehelp.execSQL("UPDATE history_tbl_WG SET didsteps='"+stepsDid+"' WHERE name='"+name+"'");
                databasehelp.execSQL("UPDATE history_tbl_WG SET percentage='"+percentage+"' WHERE name='"+name+"'");
            }else if(!name.equals(checkIsTheSame2()) && activeNumber==1 && (checkIsTheDate().compareTo(curDate) == 0)) {
                System.out.println("Date has not changed! Afairw to proigoumeno kai vazw to neo");
                databasehelp.execSQL("DELETE FROM history_tbl_WG WHERE active='"+1+"'");
                databasehelp.execSQL("insert into history_tbl_WG values('" + name + "','" + steps + "','" + stepsDid + "','"+unitSelect+"','" + percentage  + "','" + activeNumber + "','"+0+"','" + dateSearch + "')");
            }else if(activeNumber==1 && (checkIsTheDate().compareTo(curDate) < 0)){
                System.out.println("NEW DATE NEW insert data to history");
                databasehelp.execSQL("insert into history_tbl_WG values('" + name + "','" + steps + "','" + stepsDid + "','"+unitSelect+"','" + percentage  + "','" + activeNumber + "','"+0+"','" + dateSearch + "')");
            }
            else{
                System.out.println("insert apo to test mode mias kai den exist");
                databasehelp.execSQL("insert into history_tbl_WG values('" + name + "','" + steps + "','" + stepsDid + "','"+unitSelect+"','" + percentage  + "','" + activeNumber + "','"+0+"','" + dateSearch + "')");
            }


        }
          //  } while (cursor1.moveToNext());
      //  }
      //  cursor1.close();

        databasehelp.close();



    }


    public void clearCurrentTable(){
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(this, DB_NAME);
        database = dbOpenHelper.openDataBase();

        database.execSQL("delete from "+ TABLE_NAME);
        database.close();
    }


    public void print() {
        String name = "";
        Integer steps = 0;
        Integer stepsDid = 0;
        String unit="";
        float percentage = 0f;
        String dateSearch = "";
        Integer activeNumber = 0;
        SQLiteDatabase databasehelp;
        //here i store the active goal that i want to transfer
        //The database is open!
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(this, "MyHelpdb.db");
        database = dbOpenHelper.openDataBase();

        Cursor cursor = database.rawQuery("select * from time_tbl_WG where active='" + 1 + "'", null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            System.out.println("edw1111111");
            do {
                System.out.println("Collect the data from the active goal");
                name = cursor.getString(cursor.getColumnIndex("name"));
                steps = cursor.getInt(cursor.getColumnIndex("allsteps"));
                stepsDid = cursor.getInt(cursor.getColumnIndex("didsteps"));
                unit=cursor.getString(cursor.getColumnIndex("unit"));
                percentage = cursor.getFloat(cursor.getColumnIndex("percentage"));
                dateSearch = cursor.getString(cursor.getColumnIndex("date"));
                activeNumber = cursor.getInt(cursor.getColumnIndex("active"));
                System.out.println(name +" " +steps +" " +stepsDid +" "+ unit+ " " +percentage + " " +dateSearch +" " +activeNumber);

            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
    }

    public String checkIsTheDate() {
        String date = null;
        //The database is open!
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(this, "MyHistorydb.db");
        database = dbOpenHelper.openDataBase();

        Cursor cursor = database.rawQuery("select date from history_tbl_WG where active='" + 1 + "'", null);
        cursor.moveToFirst();

        if (!cursor.isAfterLast()) {
            // if(cursor.moveToFirst()){
            do {
                // System.out.println("Retrieve data now");
                date = cursor.getString(cursor.getColumnIndex("date"));

            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        System.out.println("checkIsTheSame: " +date);
        if(date==null){
            date="";
        }
        return date;
    }

    public String checkIsTheSame2() {
        String name = null;
        //The database is open!
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(this, "MyHistorydb.db");
        database = dbOpenHelper.openDataBase();

        Cursor cursor = database.rawQuery("select name from history_tbl_WG where active='" + 1 + "'", null);
        cursor.moveToFirst();

        if (!cursor.isAfterLast()) {
            // if(cursor.moveToFirst()){
            do {
                // System.out.println("Retrieve data now");
                name = cursor.getString(cursor.getColumnIndex("name"));


            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        if(name==null){
            name="";
        }
        System.out.println("checkIsTheSame2: " +name);
        return name;
    }

    public String unitReturn() {
        String unit = "";
        //The database is open!
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(this, "Mydb.db");
        database = dbOpenHelper.openDataBase();

            //put cursor
            Cursor cursor = database.rawQuery("select unit from tbl_WG WHERE name='" + checkIsTheSame() + "'", null);
            cursor.moveToFirst();

            if (!cursor.isAfterLast()) {
                do {

                    unit = cursor.getString(cursor.getColumnIndex("unit"));

                } while (cursor.moveToNext());
            }

            cursor.close();
        database.close();
        if(unit==null){
            unit="";
        }
        System.out.println("Epistrefomeno unit: " +unit);
        return unit;
    }

    private String PreviousUnit(){

        String unitParsing="";
        //here i extract the previous active unit
        //The database is open!
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(this, DB_NAME);
        database = dbOpenHelper.openDataBase();

        Cursor cursor = database.rawQuery("select unit from time_tbl_WG where active='"+1+"'", null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {

            do {

                unitParsing=cursor.getString(cursor.getColumnIndex("unit"));


            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        System.out.println("Einai h proigoumenh monada: " +unitParsing);
        return unitParsing;
    }


}
