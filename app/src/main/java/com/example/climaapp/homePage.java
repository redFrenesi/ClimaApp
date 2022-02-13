package com.example.climaapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.climaapp.Paises.paises;
import com.example.climaapp.db.paisesDb;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class homePage extends AppCompatActivity implements LocationListener {

    LocationManager locationManager;

    private RequestQueue queque;
    private TextView timeZone,tempActual,fecha,hora;
    private ImageView icon;
    private LinearLayout contenedor;
    private String ZonaHoraria="Ubicación Actual";
    private int TiempoConstante=60;
    private int tiempoTick=0;
    double tActual,temp1;
    private double horas;
    private Spinner comboBox;
    private Timer myTimer;
    DecimalFormat df = new DecimalFormat();
    String Ubicacion;
    double latitudLocal, longitudLocal;
    double latitudGlobal, longitudGlobal;
    int idGlobal;
    boolean start=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        cargarPaises();
    }
    private void init(){

        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }}, 0, 500);
        df.setMaximumFractionDigits(1);
        setContentView(R.layout.activity_home_page);
        hora=findViewById(R.id.horaId);
        fecha=findViewById(R.id.fechaId);
        timeZone=findViewById(R.id.timeZone);
        tempActual=findViewById(R.id.temp);
        icon=findViewById(R.id.iHoyOne);
        contenedor=findViewById(R.id.container);
        comboBox=findViewById(R.id.spinner2);
        queque= Volley.newRequestQueue(this);
        refreshIcon();

        if (ContextCompat.checkSelfPermission(homePage.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(homePage.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        } else {
            getLastLocationNewMethod();
        }

    }
    private void TimerMethod()
    {
        this.runOnUiThread(Timer_Tick);
    }
    private Runnable Timer_Tick = new Runnable() {
    public void run() {
        tiempoTick++;
        if (tiempoTick >= TiempoConstante/2)
        {
            refreshIcon();
            if(!start){
                if(idGlobal<=0)
                {
                    latitudGlobal=latitudLocal;
                    longitudGlobal=longitudLocal;
                    Ubicacion="Ubicación Actual";
                }
                Log.e("intentando obtener datos del servidor: ","recargando" );
                ApiRest2(latitudGlobal, longitudGlobal,idGlobal,Ubicacion);
            }
        }

        if (tiempoTick >= TiempoConstante)
          {
             refreshIcon();
              if (ContextCompat.checkSelfPermission(homePage.this, Manifest.permission.ACCESS_FINE_LOCATION)
                      != PackageManager.PERMISSION_GRANTED) {
                  ActivityCompat.requestPermissions(homePage.this, new String[]{
                          Manifest.permission.ACCESS_FINE_LOCATION
                  }, 100);
              } else {
                  getLastLocationNewMethod();
              }
              ApiRest2(latitudGlobal, longitudGlobal,idGlobal,Ubicacion);
          }
            String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
            String Hour=new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
            hora.setText(Hour);
            fecha.setText(date);
        }
    };
    private void refreshIcon()
    {
     horas= Double.valueOf(new SimpleDateFormat("HH").format(Calendar.getInstance().getTime()));
     if(horas>=18)
         icon.setImageDrawable(getResources().getDrawable(R.drawable.luna));
     else
         icon.setImageDrawable(getResources().getDrawable(R.drawable.sol));
     tiempoTick=0;
 }
    @SuppressLint("MissingPermission")// api vieja y confiable por si falla la nueva
    public void getLocation() {
        Log.e("onCreate: ", "inicio");
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, homePage.this);
            Log.e("onCreate: ", "funciona");
         //   ApiRest();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("onCreate: ", "no funciona");
        }
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        Toast.makeText(this, "" + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT);
        Log.e("onCreate: ", String.valueOf(location.getLatitude()) + "," + String.valueOf(+location.getLongitude()));

    }
    @SuppressLint("MissingPermission")// api nueva
    private void getLastLocationNewMethod(){
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            latitudLocal=location.getLatitude();
                            longitudLocal =location.getLongitude();
                            Log.e("metodo 1 ", String.valueOf(latitudLocal) + "," + String.valueOf(longitudLocal));

                         //  ApiRest();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("metodo 2", "Error trying to get last GPS location");
                       getLocation();
                    }
                });
    }
    private void ApiRest2(double latitud, double longitud,int id,String pais)
    {
        Log.e("consultando datos","datos siendo consultados");
        String url="https://api.openweathermap.org/data/2.5/onecall?lat="+String.valueOf(latitud)+"&lon="+String.valueOf(longitud)+"&exclude=minutely,hourly&appid=5621c00c5d57dd7ce40821da31af78ee";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    start=true;
                    JSONObject actual=response.getJSONObject("current");
                    JSONArray diario= response.getJSONArray("daily");
                    tActual= Double.parseDouble(df.format(actual.getDouble("temp")-273.15));
                    ZonaHoraria=response.getString("timezone");
                    tempActual.setText(String.valueOf(tActual+"°"));
                    timeZone.setText(ZonaHoraria);
                    Log.e( "Recargando", String.valueOf(temp1));
                    paisesDb db = new paisesDb(homePage.this);
                    ArrayList<paises> ListaPaises = db.mostrarPaises();
                    boolean Existencia=false;
                    String tTActual="";
                    for(int i=0;i<ListaPaises.size();i++) {
                        if(ListaPaises.get(i).getNombre().contains(pais))
                        {
                            Existencia=true;
                           break;
                        }
                    }
                    contenedor.removeAllViews();
                    for(int i=0;i<7;i++)
                    {
                        boolean day=false;
                        String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                        String dt = date;  // Start date
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Calendar c = Calendar.getInstance();
                        try {
                            c.setTime(sdf.parse(dt));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        c.add(Calendar.DATE, i);  // number of days to add, can also use Calendar.DAY_OF_MONTH in place of Calendar.DATE
                        SimpleDateFormat sdf1 = new SimpleDateFormat("E");
                        String output = sdf1.format(c.getTime());

                        JSONObject t1 = (JSONObject) diario.getJSONObject(i).get("temp");
                        temp1 = Double.parseDouble(df.format(t1.getDouble("day") - 273.15));
                        tTActual=String.valueOf((temp1) + "°");
                        if(i==0){
                            output="Hoy";
                            tTActual=String.valueOf(tActual + "°");
                            day=true;
                        }
                        agregarCards(tTActual, horas, output,day);
                        Log.e( "diario ",String.valueOf((temp1) + "°"));
                    }
                    JSONObject t2 = (JSONObject) diario.getJSONObject(1).get("temp");
                    JSONObject t3 = (JSONObject) diario.getJSONObject(2).get("temp");
                    JSONObject t4 = (JSONObject) diario.getJSONObject(3).get("temp");
                    JSONObject t5 = (JSONObject) diario.getJSONObject(4).get("temp");
                    JSONObject t6 = (JSONObject) diario.getJSONObject(5).get("temp");
                    JSONObject t7 = (JSONObject) diario.getJSONObject(6).get("temp");
                    double tmp2 = Double.parseDouble(df.format(t2.getDouble("day") - 273.15));
                    double tmp3 = Double.parseDouble(df.format(t3.getDouble("day") - 273.15));
                    double tmp4 = Double.parseDouble(df.format(t4.getDouble("day") - 273.15));
                    double tmp5 = Double.parseDouble(df.format(t5.getDouble("day") - 273.15));
                    double tmp6 = Double.parseDouble(df.format(t6.getDouble("day") - 273.15));
                   if(Existencia)
                   {
                       db.UpdatePais(id,pais,ZonaHoraria,tActual,tmp2,tmp2,tmp3,tmp4,tmp5,tmp6);
                       Log.e("db","Pais actuaLIZADO: "+ pais);
                   }
                   else{
                       db.insertarPais(id,pais,ZonaHoraria,tActual,tmp2,tmp2,tmp3,tmp4,tmp5,tmp6);
                       Log.e("db","Pais Insertado: "+ pais);
                   }
                   // cargarRespaldo();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    Log.e( "no hay red", "error :)");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e( "no hay red", "cargar de la db)");
                start=false;
                if(idGlobal==0)
                {
                    latitudGlobal=latitudLocal;
                    longitudGlobal=longitudLocal;
                    Ubicacion="Ubicación Actual";
                }
                offline(Ubicacion);
            }
        });
        int socketTimeout = 5000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        queque.add(request);
    }
    private void agregarCards(String temp,double hora,String fecha,boolean daynigth)
    {
        View view = getLayoutInflater().inflate(R.layout.objeto_clima_actual,null);
        ImageView logo=view.findViewById(R.id.logoClima);
        TextView dia=view.findViewById(R.id.dia);
        TextView Temp=view.findViewById(R.id.temp);
        logo.setImageDrawable(getResources().getDrawable(R.drawable.sol));
        if(daynigth) {
            if (hora >= 18)
                logo.setImageDrawable(getResources().getDrawable(R.drawable.luna));
            else
                logo.setImageDrawable(getResources().getDrawable(R.drawable.sol));
        }
        dia.setText(fecha);
        Temp.setText(temp);
        contenedor.addView(view);
    }
    public void cargarPaises()  {
        String data="";
        String country="";

         try{
        InputStream inputStream = getResources().openRawResource(R.raw.countries);
        BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(inputStream));
        String eachline = bufferedReader.readLine();
        while (eachline != null) {
            eachline = bufferedReader.readLine();
            data = data+eachline;
        }

    } catch (IOException e) {
             e.printStackTrace();
         }
        data="{"+data+"}";

        JSONObject response = null;
        try {
            response = new JSONObject(data);
            JSONArray array = response.getJSONArray("ref_country_codes");
            ArrayList<String> lista= new ArrayList<String>();;

            for (int i=0;i<array.length();i++)
            {
                JSONObject pais= (JSONObject) array.get(i);
                country= pais.get("country").toString();
             if(i==0) {
                 lista.add("Ubicación Actual");
             }
             lista.add(country);
            }
            ArrayAdapter<String> adp1 = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, lista);
            adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            comboBox.setAdapter(adp1);
            comboBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                    // TODO Auto-generated method stub
                    Ubicacion=lista.get(position);
                    if(lista.get(position).equals("Ubicación Actual"))
                    {
                        idGlobal=0;
                        Ubicacion="Ubicación Actual";
                        ApiRest2(latitudLocal, longitudLocal,0,Ubicacion);
                    }else {
                        try {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject pais = null;
                                pais = (JSONObject) array.get(i);

                                if (pais.get("country").equals((lista.get(position)))) {

                                    int idP = pais.getInt("numeric");
                                    double latitudP = pais.getDouble("latitude");
                                    double longitudP = pais.getDouble("longitude");
                                    String nameP = lista.get(position).toString();
                                    latitudGlobal=latitudP;
                                    longitudGlobal=longitudP;
                                    Ubicacion=nameP;
                                    idGlobal=idP;
                                    ApiRest2(latitudP, longitudP, idP, Ubicacion);
                                    break;
                                }

                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                }

                    @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }



    }
    public  void offline(String pais)
    {
        paisesDb db = new paisesDb(homePage.this);
        ArrayList<paises> ListaPaises = db.mostrarPaises();

        for(int i=0;i<ListaPaises.size();i++) {
            if(ListaPaises.get(i).getNombre().contains(pais))
            {
                ListaPaises.get(i).getNombre();
                ListaPaises.get(i).getTimeZone();
                double tmp1 =   ListaPaises.get(i).getD1();
                double tmp2 =   ListaPaises.get(i).getD2();
                double tmp3 = ListaPaises.get(i).getD3();
                double tmp4 = ListaPaises.get(i).getD4();
                double tmp5 =  ListaPaises.get(i).getD5();
                double tmp6 =  ListaPaises.get(i).getD6();
                double tmp7 = ListaPaises.get(i).getD7();
                ZonaHoraria= ListaPaises.get(i).getTimeZone();
                contenedor.removeAllViews();
                tempActual.setText(String.valueOf(tmp1)+"°");
                timeZone.setText(ZonaHoraria);
                agregarCards(String.valueOf(tmp1)+"°", horas, addDays(0),true);
                agregarCards(String.valueOf(tmp2)+"°", horas,  addDays(1),false);
                agregarCards(String.valueOf(tmp3)+"°", horas,  addDays(2),false);
                agregarCards(String.valueOf(tmp4)+"°", horas,  addDays(3),false);
                agregarCards(String.valueOf(tmp5)+"°", horas,  addDays(4),false);
                agregarCards(String.valueOf(tmp6)+"°", horas,  addDays(5),false);
                agregarCards(String.valueOf(tmp7)+"°", horas,  addDays(6),false);
                break;

            }
        }
    }
    private String addDays(int daysToAdd)
    {
    String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
    String dt = date;  // Start date
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Calendar c = Calendar.getInstance();
    try {
        c.setTime(sdf.parse(dt));
    } catch (ParseException e) {
        e.printStackTrace();
    }
    c.add(Calendar.DATE, daysToAdd);  // number of days to add, can also use Calendar.DAY_OF_MONTH in place of Calendar.DATE
    SimpleDateFormat sdf1 = new SimpleDateFormat("E");
    String output = sdf1.format(c.getTime());

    return output;
}

}