package a238443.colors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ColorsAdapter extends BaseAdapter{
    private ArrayList<Integer> database = new ArrayList<>();
    private LayoutInflater customInflater;

    ColorsAdapter(Context forAdapter) {
        customInflater = (LayoutInflater)forAdapter.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    void addItem(final Integer newColor) {
        database.add(newColor);
        notifyDataSetChanged();
    }

    void addDatabase(ArrayList<Integer> database) {
        this.database = database;
        notifyDataSetChanged();
    }

    ArrayList<Integer> getDatabase() {
        return database;
    }

    void removeItem(int position) {
        database.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return database.size();
    }

    @Override
    public Integer getItem(int position) {
        return database.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View currentView = convertView;
        ColorHolder holder = new ColorHolder();

        if(convertView == null) {
            currentView = customInflater.inflate(R.layout.color_item,parent,false);
            holder.forColor = currentView.findViewById(R.id.color_view);
            currentView.setTag(holder);
        }
        else
            holder = (ColorHolder) currentView.getTag();


        int currentColor = database.get(position);

        holder.forColor.setBackgroundColor(currentColor);

        return currentView;
    }

    private static class ColorHolder {
        TextView forColor;
    }
}
