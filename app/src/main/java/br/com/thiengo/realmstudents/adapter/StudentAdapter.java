package br.com.thiengo.realmstudents.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import br.com.thiengo.realmstudents.AddUpdateStudentsActivity;
import br.com.thiengo.realmstudents.R;
import br.com.thiengo.realmstudents.domain.Student;
import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 */
public class StudentAdapter extends RealmBaseAdapter<Student> implements ListAdapter {

    public StudentAdapter(Context context, RealmResults<Student> realmResults, boolean automaticUpdate ){
        super(context, realmResults, automaticUpdate);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomViewHolder holder;

        if( convertView == null ){
            convertView = inflater.inflate(R.layout.item_student, parent, false);
            holder = new CustomViewHolder();
            convertView.setTag( holder );

            holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            holder.btUpdate = (Button) convertView.findViewById(R.id.bt_update);
            holder.btRemove = (Button) convertView.findViewById(R.id.bt_remove);
        }
        else{
            holder = (CustomViewHolder) convertView.getTag();
        }

        final Student d = realmResults.get(position);
        holder.tvName.setText( d.getName() );

        holder.btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(context, AddUpdateStudentsActivity.class);
                it.putExtra(Student.ID, d.getId());
                context.startActivity(it);
            }
        });

        holder.btRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                d.removeFromRealm();
                realm.commitTransaction();
                realm.close();
            }
        });

        return convertView;
    }

    private static class CustomViewHolder{
        TextView tvName;
        Button btUpdate;
        Button btRemove;
    }
}
