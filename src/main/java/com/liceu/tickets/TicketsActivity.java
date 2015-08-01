package com.liceu.tickets;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TicketsActivity extends Activity {
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private List<String> list;
    private TicketsListFragment lfragment;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.main);

        // Incicialitzem el Drawer (el caixó del menú que es treu fent un swipe a l'esquerra
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        list = new ArrayList<>();
        list.add(getString(R.string.menu_prefs));
        list.add(getString(R.string.menu_refresh));
        list.add(getString(R.string.menu_new));
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list));

        // Incialitzem i posem en marxa el ListFragment (equivalent a l'Activity abans)
        // El ListFragment mostra la ListView dels tickets i fa tota la feina (refresc...)
        lfragment = new TicketsListFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, lfragment)
                .commit();

        // Preparem el Listener pel menú del Drawer
        drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                executeMenu(position);
                drawerLayout.closeDrawer(drawerList);
            }
        });
    }

    // Es crida quan triem una opció del Drawer
    private void executeMenu(int position) {
        switch (position) {
            case 0:
                startActivity(new Intent(this, TicketsPreferences.class));
                break;

            case 1:
                lfragment.refreshData();
                break;
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    // Això és el ListFragment. Fa tota la feina (refresc, clic per veure ticket...)
    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    public static class TicketsListFragment extends ListFragment {
        private TicketsApp tapp;

        public TicketsListFragment() {
            // Empty constructor
        }

        // Called when the activity is first created.
        @Override
        public void onActivityCreated(Bundle savedInstance) {
            super.onActivityCreated(savedInstance);

            tapp = (TicketsApp) getActivity().getApplication();
            setMyListAdapter();

            ListView lv = getListView();
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Ticket t = (Ticket) getListView().getItemAtPosition(position);
                    executeViewTicketActivity(t.id);
                }
            });


        }

        void refreshData() {
            new ataskRefreshTickets().execute();
        }

        private void executeViewTicketActivity(int tickid) {
            Intent i = new Intent(getActivity(), ViewTicketActivity.class);
            i.putExtra("tickid", tickid);
            startActivity(i);
        }

        private void setMyListAdapter() {
            setListAdapter(new TicketAdapter(getActivity(), R.layout.ticklist_row, tapp.database.getTicketList()));
        }


        // ***********************************************************************
        // Adapter class for displaying information about tickets in the main list
        private class TicketAdapter extends ArrayAdapter {
            private List tickets;

            public TicketAdapter(Context context, int resId, List tickList) {
                super(context, resId, tickList);
                tickets = tickList;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.ticklist_row, null);
                }

                Ticket tick = (Ticket) tickets.get(position);

                TextView tt = (TextView) v.findViewById(R.id.ticktext);
                TextView tu = (TextView) v.findViewById(R.id.tickuser);

                String ttext;
                if (tick.text.length() > 120)
                    ttext = tick.text.substring(0, 120);
                else
                    ttext = tick.text;

                tt.setText(ttext);
                tu.setText(tick.user);

                return v;
            }
        }


        // ****************************************
        // Async task for refreshing tickets screen
        private class ataskRefreshTickets extends AsyncTask<Void, Void, Void> {
            ProgressDialog pb;
            boolean err = false;

            protected void onPreExecute() {
                pb = ProgressDialog.show(getActivity(), "", getString(R.string.wait), true);
            }

            protected Void doInBackground(Void... params) {
                try {
                    tapp.database.refreshDataFromServer(tapp.server);
                } catch (ServerError e) {
                    err = true;
                }

                return null;
            }

            protected void onPostExecute(Void param) {
                pb.dismiss();
                if (err) {
                    AlertDialog alertDialog;
                    alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage(getString(R.string.errserver));
                    alertDialog.show();
                }

                setMyListAdapter();
            }
        }


    }

}

/*
public class TicketsActivity extends ListActivity {
    static String TAG = "TicketsActivity";

    TicketsApp tapp;

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tapp = (TicketsApp) getApplication();
        setMyListAdapter();

        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Ticket t = (Ticket) getListView().getItemAtPosition(position);
                executeViewTicketActivity(t.id);
            }
        });


    }

    protected void onResume() {
        setMyListAdapter();
        super.onResume();
    }


    private void executeViewTicketActivity(int tickid) {
        Intent i = new Intent(this, ViewTicketActivity.class);
        i.putExtra("tickid", tickid);
        startActivity(i);
    }


    private void setMyListAdapter() {
        setListAdapter(new TicketAdapter(this, R.layout.ticklist_row, tapp.database.getTicketList()));
    }


    // Inflate res/menu/mainmenu.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }


    // Respond to user click on menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.preferences:
                showPreferences();
                return true;

            case R.id.refresh:
                new ataskRefreshTickets().execute();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showPreferences() {
        startActivity(new Intent(this, TicketsPreferences.class));
    }


    // ***********************************************************************
    // Adapter class for displaying information about tickets in the main list
    private class TicketAdapter extends ArrayAdapter {
        private List tickets;

        public TicketAdapter(Context context, int resId, List tickList) {
            super(context, resId, tickList);
            tickets = tickList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.ticklist_row, null);
            }

            Ticket tick = (Ticket) tickets.get(position);

            TextView tt = (TextView) v.findViewById(R.id.ticktext);
            TextView tu = (TextView) v.findViewById(R.id.tickuser);

            String ttext;
            if (tick.text.length() > 120)
                ttext = tick.text.substring(0, 120);
            else
                ttext = tick.text;

            tt.setText(ttext);
            tu.setText(tick.user);

            return v;
        }
    }

    // ****************************************
    // Async task for refreshing tickets screen
    private class ataskRefreshTickets extends AsyncTask<Void, Void, Void> {
        ProgressDialog pb;
        boolean err = false;

        protected void onPreExecute() {
            pb = ProgressDialog.show(TicketsActivity.this, "", getString(R.string.wait), true);
        }

        protected Void doInBackground(Void... params) {
            try {
                tapp.database.refreshDataFromServer(tapp.server);
            } catch (ServerError e) {
                err = true;
            }

            return null;
        }

        protected void onPostExecute(Void param) {
            pb.dismiss();
            if (err) {
                AlertDialog alertDialog;
                alertDialog = new AlertDialog.Builder(TicketsActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage(getString(R.string.errserver));
                alertDialog.show();
            }

            setMyListAdapter();
        }
    }

}
*/