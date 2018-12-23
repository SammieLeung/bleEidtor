package com.sean.lxp.bleeditor;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHoler> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<BluetoothDevice> mDevcieList;

    public DeviceAdapter(Context context, List<BluetoothDevice> list) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        mDevcieList = list;
    }

    @NonNull
    @Override
    public DeviceViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.device_item, parent, false);
        DeviceViewHoler holer = new DeviceViewHoler(view);
        holer.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null){
                    listener.OnClick((BluetoothDevice) v.getTag());
                }
            }
        });
        return holer;
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHoler holder, int position) {
        BluetoothDevice device=mDevcieList.get(position);
        holder.name.setText(mDevcieList.get(position).getName());
        holder.mac.setText(mDevcieList.get(position).getAddress());
        holder.itemView.setTag(mDevcieList.get(position));
    }

    @Override
    public int getItemCount() {
        return mDevcieList.size();
    }

    public interface OnClickListener {
        public void OnClick(BluetoothDevice device);
    }

    public void setDevcieList(List<BluetoothDevice> list){
        mDevcieList=list;
    }
    private OnClickListener listener;

    public void setOnClickListener(OnClickListener clickListener) {
        listener = clickListener;
    }

    public static class DeviceViewHoler extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView mac;
        public DeviceViewHoler(View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.device_name);
            mac=itemView.findViewById(R.id.mac);
        }
    }
}
