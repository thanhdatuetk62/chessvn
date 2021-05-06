package vn.edu.chessUI.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import vn.edu.chessUI.OnPeerSelected;
import vn.edu.chessUI.R;

public class PeerListAdapter extends RecyclerView.Adapter<PeerListAdapter.PeerViewHolder> {
    private final ArrayList<String> mPeerList;
    private final LayoutInflater mInflater;
    OnPeerSelected callback;

    public PeerListAdapter(Context context, ArrayList<String> peerList, OnPeerSelected callback) {
        mPeerList = peerList;
        mInflater = LayoutInflater.from(context);
        this.callback = callback;
    }

    @NonNull
    @Override
    public PeerListAdapter.PeerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.peer_item, parent, false);
        return new PeerViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull PeerListAdapter.PeerViewHolder holder, int position) {
        String mCurrent = mPeerList.get(position);
        TextView tv = holder.peerItemView.findViewById(R.id.peer_name);
        tv.setText(mCurrent);
    }

    @Override
    public int getItemCount() {
        return mPeerList.size();
    }

    public class PeerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ViewGroup peerItemView;
        final PeerListAdapter mAdapter;

        public PeerViewHolder(@NonNull View itemView, PeerListAdapter adapter) {
            super(itemView);
            peerItemView = itemView.findViewById(R.id.peer);
            mAdapter = adapter;
            peerItemView.findViewById(R.id.connect_button).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Get position of
            int mPosition = getLayoutPosition();
            callback.onSelected(mPosition);
        }
    }
}
