package me.dats.com.datsme.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;
import me.dats.com.datsme.Activities.MapsActivity;
import me.dats.com.datsme.Activities.Others_profile;
import me.dats.com.datsme.Fragments.BottomSheetListFragment;
import me.dats.com.datsme.Models.MyItem;
import me.dats.com.datsme.R;

public class BottomSheetViewHolder extends RecyclerView.ViewHolder {

    private TextView name;
    private RelativeLayout name_parent;
    private CircleImageView circleImageView;
    private CardView cardView;

    BottomSheetViewHolder(View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.user_name);
        circleImageView = itemView.findViewById(R.id.listImage_view);
        cardView = itemView.findViewById(R.id.listItemsCardView);
        name_parent = itemView.findViewById(R.id.user_name_parent);
    }

    public void bind(final MyItem model, final Context mContext, int position, final BottomSheetListFragment bottomSheetListFragment) {

        if (position % 2 == 0) {
            name_parent.setBackgroundColor(Color.rgb(245, 6, 6));
        } else {
            name_parent.setBackgroundColor(Color.rgb(92, 220, 84));
        }


        name.setText(model.getTitle());
        circleImageView.setImageBitmap(model.getBitmap());
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (model.getSnippet().equals(FirebaseAuth.getInstance().getCurrentUser().getUid().toString())) {
                    bottomSheetListFragment.dismiss();
                    ((MapsActivity) mContext).getProfileFragment();
                } else {
                    Intent i = new Intent(mContext, Others_profile.class);
                    i.putExtra("from_user_id", model.getSnippet());
                    i.putExtra("userName", model.getTitle());
                    view.getContext().startActivity(i);
                    bottomSheetListFragment.dismiss();
                }

            }
        });

    }
}
