package com.yuleshchenko.weather.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuleshchenko.weather.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.realm.RealmResults;

public class RecycAdapter extends RecyclerView.Adapter<RecycAdapter.ViewHolder> {

    private RealmResults<ForecastRealm> theResults;
    private Context mContext;

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView day;
        public TextView month;
        public TextView week;
        public TextView hour;
        public ImageView icon;
        public TextView main;
        public TextView temp;
        public ViewHolder(View itemView) {
            super(itemView);
            this.day = (TextView) itemView.findViewById(R.id.day);
            this.month = (TextView) itemView.findViewById(R.id.month);
            this.week = (TextView) itemView.findViewById(R.id.week);
            this.hour = (TextView) itemView.findViewById(R.id.hour);
            this.icon = (ImageView) itemView.findViewById(R.id.icon);
            this.main = (TextView) itemView.findViewById(R.id.main);
            this.temp = (TextView) itemView.findViewById(R.id.temperature);
        }
    }

    // Provide a suitable constructor
    public RecycAdapter(Context context, RealmResults<ForecastRealm> realmResults) {
        this.theResults = realmResults;
        this.mContext = context;
    }

    @Override  // Create new views (invoked by the layout manager)
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view
        View listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_for_listview, parent, false);
        final ViewHolder viewHolder = new ViewHolder(listItemView);
        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CallbackInterface myInterface = (CallbackInterface) mContext;
                myInterface.showDetails(viewHolder.getAdapterPosition());
            }
        });
        return viewHolder;
    }

    @Override  // Replace the contents of a view (invoked by the layout manager)
    public void onBindViewHolder(ViewHolder holder, int position) {
        ForecastRealm theForecast = theResults.get(position);  // forecast item based on the position
        // Assign values if the forecast is not null
        if (theForecast != null) {

            // Set the date
            String dateString = theForecast.getDtTxt();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Date date = null;
            try {
                date = dateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Calendar cal = Calendar.getInstance();
            if (date != null) {
                cal.setTime(date);
            }

            // Obtain the date
            String week = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH);
            String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            String month = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH);
            String year = String.valueOf(cal.get(Calendar.YEAR));
            String hour = new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(cal.getTime());

            // Populate the fields
            holder.day.setText(day);
            holder.month.setText(String.format(mContext.getString(R.string.month_year), month, year));
            holder.hour.setText(hour);
            holder.week.setText(week);
            temperatureFormat(theForecast.getTemp(), holder);
            holder.main.setText(theForecast.getMain());
            // Set icon
            int id = mContext.getResources().getIdentifier("icon" + theForecast.getIcon(), "drawable", mContext.getPackageName());
            holder.icon.setImageResource(id);
        }
    }

    @Override  // Return the size of your dataset (invoked by the layout manager)
    public int getItemCount() {
        return theResults.size();
    }

    private void temperatureFormat(double t, ViewHolder holder) {
        String f = "";
        long tRound = Math.round(t);
        if (tRound > 0) {
            f = "+";
            holder.temp.setTextColor(ContextCompat.getColor(mContext, R.color.colorRed));
        } else if (tRound < 0) {
            holder.temp.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        } else {
            holder.temp.setTextColor(ContextCompat.getColor(mContext, R.color.colorBlack));
        }
        holder.temp.setText(String.format(mContext.getString(R.string.degree_sym), f, String.valueOf(tRound)));
    }

}