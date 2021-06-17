package android.example.location;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LocationAdapter extends ListAdapter<Locate,LocationViewHolder> {
    private List<Locate> updateUI = new ArrayList<>();
    private onClicked onClicked;

    static class LocationDiff extends DiffUtil.ItemCallback<Locate> {
        @Override
        public boolean areItemsTheSame(@NonNull Locate oldItem, @NonNull Locate newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Locate oldItem, @NonNull Locate newItem) {
            return oldItem.getLocate().equals(newItem.getLocate());
        }
    }

    protected LocationAdapter(@NonNull DiffUtil.ItemCallback<Locate> diffCallback,onClicked onclicked) {
        super(diffCallback);
        this.onClicked=onclicked;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item,parent,false);
        LocationViewHolder wordViewHolder = new LocationViewHolder(view);
        return wordViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Locate current =updateUI.get(position);
        String[] arr=current.getLocate().split(" ");
        holder.latitude.setText(arr[0]);
        holder.longitude.setText(arr[1]);
        holder.time.setText(arr[2]);
        holder.state.setText(arr[3]);
    }

    public void update(List<Locate> N){
        updateUI.clear();
        updateUI.addAll(N);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return updateUI.size();
    }
}

class LocationViewHolder extends RecyclerView.ViewHolder{
    public LocationViewHolder(@NonNull View itemView) {
        super(itemView);
    }
    TextView latitude = (TextView)itemView.findViewById(R.id.latitude);
    TextView longitude = (TextView)itemView.findViewById(R.id.longitude);
    TextView time = (TextView)itemView.findViewById(R.id.time);
    TextView state  = (TextView)itemView.findViewById(R.id.state);

}
interface onClicked{
}