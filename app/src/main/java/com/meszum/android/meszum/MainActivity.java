package com.meszum.android.meszum;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TareaWSListar tarea = new TareaWSListar();
        tarea.execute("http://meszumtest-erueloi.rhcloud.com/api/events/?format=json");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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

    //Tarea Asíncrona para llamar al WS de listado en segundo plano
    private class TareaWSListar extends AsyncTask<String,Integer,Boolean> {

        private String[] clientes;
        private List<Event> events;

        protected Boolean doInBackground(String... params) {

            boolean resul = true;
            URL url;
            HttpURLConnection urlConnection;
            JSONArray response = new JSONArray();

            try
            {
                url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                int responseCode = urlConnection.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK){
                    String responseString = readStream(urlConnection.getInputStream());
                    Log.v("CatalogClient", responseString);
                    response = new JSONArray(responseString);
                }else{
                    Log.v("CatalogClient", "Response code:"+ responseCode);
                }

                events = new ArrayList<>();
                for(int i=0; i<response.length(); i++)
                {
                    JSONObject obj = response.getJSONObject(i);
                    int idEvent = obj.getInt("id");
                    String strTitleEvent = obj.getString("title");
                    String address = obj.getString("address");
                    String poster = obj.getString("poster");
                    String description = obj.getString("description");
                    events.add(new Event(idEvent, strTitleEvent, description, poster));
                }
            }
            catch(Exception ex)
            {
                Log.e("ServicioRest","Error!", ex);
                resul = false;
            }
            return resul;
        }

        private String readStream(InputStream in) {
            BufferedReader reader = null;
            StringBuffer response = new StringBuffer();
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return response.toString();
        }

        protected void onPostExecute(Boolean result) {

            if (result)
            {
                RecyclerView rv = (RecyclerView)findViewById(R.id.rv);
//                rv.setHasFixedSize(true);
//                rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                rv.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
                RVAdapter adapter = new RVAdapter(events);
                rv.setAdapter(adapter);

            }
        }
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.EventViewHolder>{

        public class EventViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            TextView personName;
            TextView personAge;
            ImageView personPhoto;

            EventViewHolder(View itemView) {
                super(itemView);
                cv = (CardView)itemView.findViewById(R.id.cv);
                personName = (TextView)itemView.findViewById(R.id.person_name);
                personAge = (TextView)itemView.findViewById(R.id.person_age);
                personPhoto = (ImageView)itemView.findViewById(R.id.person_photo);
            }
        }
        List<Event> events;
        RVAdapter(List<Event> lstEvents){
            this.events = lstEvents;
        }

        @Override
        public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.event, viewGroup, false);
            EventViewHolder pvh = new EventViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(EventViewHolder eventViewHolder, int i) {
            eventViewHolder.personName.setText(events.get(i).title);
            eventViewHolder.personAge.setText(events.get(i).description);
            Context context = eventViewHolder.personPhoto.getContext();
            Picasso.with(context).load(events.get(i).poster).resize(350, 560).into(eventViewHolder.personPhoto);
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

    }
}

class Event {
    int id;
    String title;
    String description;
    String poster;

    Event(int id, String title, String description, String poster) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.poster = poster;
    }
}


