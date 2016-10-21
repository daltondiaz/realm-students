package br.com.thiengo.realmstudents;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.LineNumberReader;
import java.util.LinkedList;
import java.util.List;

import br.com.thiengo.realmstudents.adapter.DisciplineSpinnerAdapter;
import br.com.thiengo.realmstudents.domain.Discipline;
import br.com.thiengo.realmstudents.domain.Grade;
import br.com.thiengo.realmstudents.domain.Student;
import io.realm.Realm;
import io.realm.RealmResults;

public class AddUpdateStudentsActivity extends AppCompatActivity {

    private Realm realm;
    private RealmResults<Student> students;
    private RealmResults<Discipline> disciplines;

    private Student student;
    private EditText etName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_update_students);

        student = new Student();
        etName = (EditText) findViewById(R.id.et_name);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        Button btAddUpdate = (Button) findViewById(R.id.bt_add_update);


        realm = Realm.getDefaultInstance();
        students = realm.where( Student.class ).findAll();

        if( getIntent() != null && getIntent().getLongExtra( Student.ID, 0 ) > 0 ){
            student.setId( getIntent().getLongExtra( Student.ID, 0 ) );

            student = students.where().equalTo("id", student.getId()).findAll().get(0);
            etName.setText( student.getName() );
            tvTitle.setText("Atualizar Aluno");
            btAddUpdate.setText( "Update" );
        }

        disciplines = realm.where(Discipline.class).findAll();


        //First Block
        Spinner spDiscipline = (Spinner) findViewById(R.id.sp_discipline);
        spDiscipline.setAdapter(new DisciplineSpinnerAdapter(this,disciplines));
        View btRemove = findViewById(R.id.bt_remove);
        btRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callRemoveGrade(view);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    // LISTENERS
    public void callAddUpdateStudent( View view ){
        String label = "atualizado";
        if( student.getId() == 0 ){
            students.sort( "id", RealmResults.SORT_ORDER_DESCENDING );
            long id = students.size() == 0 ? 1 : students.get(0).getId() + 1;
            student.setId( id );
            label = "adicionado";
        }

        try{
            realm.beginTransaction();
            student.setName(etName.getText().toString() );
            realm.copyToRealmOrUpdate(student);
            realm.commitTransaction();

            student = realm.where(Student.class).equalTo("id",student.getId()).findFirst();

            realm.beginTransaction();
            student.getGrades().clear();
            student.getGrades().addAll(getGradesFromView(view,disciplines));
            realm.commitTransaction();

            Toast.makeText(AddUpdateStudentsActivity.this, "Aluno "+label, Toast.LENGTH_SHORT).show();
            finish();
        }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(AddUpdateStudentsActivity.this, "Falhou!", Toast.LENGTH_SHORT).show();
        }
    }

    public void callAddGrade(View view){
        LinearLayout llParent = (LinearLayout) view.getParent();
        if(llParent.getChildCount() -1 == disciplines.size()){
            Toast.makeText(this,"Máximo de Disciplinas Atingido",Toast.LENGTH_SHORT).show();
            return ;
        }
        createGradeForView(view,disciplines,null);
    }

    private void callRemoveGrade(View view){
        LinearLayout llParent = (LinearLayout) view.getParent().getParent();

        if(llParent.getChildCount() > 2){
            llParent.removeView((LinearLayout)view.getParent());
        }
    }

    // UTILS
    public void createGradeForView(View view, RealmResults<Discipline> disciplines, Grade grade){
        LayoutInflater layoutInflater = this.getLayoutInflater();
        LinearLayout llChild =
                (LinearLayout) layoutInflater.inflate(R.layout.box_discipline_grade,null);


        Spinner spDiscipline = (Spinner) llChild.findViewById(R.id.sp_discipline);
        spDiscipline.setAdapter(new DisciplineSpinnerAdapter(this,disciplines));

        if(grade != null){
            spDiscipline.setSelection(getDisciplenesPosition(disciplines,grade.getDiscipline()));
        }

        EditText etGrade = (EditText) llChild.findViewById(R.id.et_grade);
        if(grade != null){
            etGrade.setText(String.valueOf(grade.getGrade()));
        }

        View btRemove = llChild.findViewById(R.id.bt_remove);
        btRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callRemoveGrade(view);
            }
        });

        float scale = getResources().getDisplayMetrics().density;
        int margin = (int) (5 * scale * 0.5f);

        // Adicionado porque quando se é trabalhado com view.getParent é
        // perdido alguns parametros do layout
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(margin,margin,margin,margin);

        llChild.setLayoutParams(layoutParams);

        LinearLayout llParent =
                (LinearLayout) view.getParent();
        llParent.addView(llChild,llParent.getChildCount()-1);

    }

    private int getDisciplenesPosition(RealmResults<Discipline> disciplines,Discipline discipline){

        for(int i = 0; i< disciplines.size(); i++){
            if(disciplines.get(i).getId() == discipline.getId()){
                return (i);
            }
        }
        return 0;
    }

    private List<Grade> getGradesFromView(View view, RealmResults<Discipline> disciplines){
        RelativeLayout rlParent = (RelativeLayout) view.getParent();
        List<Grade> grades = new LinkedList<>();
        for (int i=0; i<rlParent.getChildCount();i++){

            if(rlParent.getChildAt(i) instanceof ScrollView){
                ScrollView scrollView = (ScrollView) rlParent.getChildAt(i);
                LinearLayout llChield = (LinearLayout) scrollView.getChildAt(0);

                for (int j=0; j< llChield.getChildCount();j++){
                    if(llChield.getChildAt(j) instanceof LinearLayout){
                        Spinner spDiscipline = (Spinner) llChield.getChildAt(j).findViewById(R.id.sp_discipline);
                        EditText etGrade = (EditText) llChield.getChildAt(j).findViewById(R.id.et_grade);

                        Grade g = new Grade();
                        if(realm.where(Grade.class).findAll().size() > 0){
                            g.setId(realm.where(Grade.class).findAllSorted("id",RealmResults.SORT_ORDER_ASCENDING).get(0).getId()+1+j);
                        }else{
                            g.setId(1);
                        }

                        Discipline d = new Discipline();
                        d.setId(disciplines.get(spDiscipline.getSelectedItemPosition()).getId());
                        d.setName(disciplines.get(spDiscipline.getSelectedItemPosition()).getName());
                        g.setDiscipline(d);

                        g.setGrade(Double.parseDouble(etGrade.getText().toString()));

                        grades.add(g);
                    }
                }
            }

        }

        return grades;
    }





}
