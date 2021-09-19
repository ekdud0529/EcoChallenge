package com.example.monthlychallenge;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChalListFragment extends Fragment {
    String TAG = "ChallListFragment";

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Challenge> arrayList;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    Button btn_addList;
    Spinner sp_addList;
    EditText ed_addList;

    Context ct = getActivity();

    int cnt=1;


    public void dialog(View view){

        AlertDialog.Builder ad = new AlertDialog.Builder(ct);
        View dialogView = getLayoutInflater().inflate(R.layout.add_list_dialog,null);

        ad.setTitle("Challenge");

        Spinner sp = (Spinner) dialogView.findViewById(R.id.addListSp);
        EditText edCount = (EditText) dialogView.findViewById(R.id.addLIstCount);
        EditText edItem = (EditText) dialogView.findViewById(R.id.addLIstItem);

        ArrayAdapter<CharSequence> ad_monthly = ArrayAdapter.createFromResource(getActivity(), R.array.monthly_array, android.R.layout.simple_spinner_item);
        ad_monthly.setDropDownViewResource(android.R.layout.simple_spinner_item);
        sp.setAdapter(ad_monthly);

        ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String ed_itemVal = edItem.getText().toString();
                String ed_countVal = edCount.getText().toString();
                String sp_val = sp.getSelectedItem().toString();
                String img_val = "https://firebasestorage.googleapis.com/v0/b/monthlychallenge-fb8a3.appspot.com/o/20caf1d857530.jpg?alt=media&token=9ebb9be4-c883-4e42-b659-4dcc365c5abd";

                Log.e(TAG, cnt + " " + sp_val + " " + ed_countVal + " " + ed_itemVal);

                //firebase로 입력받은 값 넘기기
                final FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference ref = db.getReference("challengeList");

                Challenge add_List = new Challenge(sp_val, ed_countVal, ed_itemVal, img_val);
                if(ed_countVal != ""){
                    cnt += 1;
                    String chalCnt = String.valueOf(cnt);
                    ref.child(chalCnt).setValue(add_List)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            showChalList(view);
                            Log.e(TAG, "저장 성공");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "저장 실패");
                        }
                    });
                    Log.e(TAG, "수행되었을지..");
                }
                else{
                    Toast.makeText(getActivity(), "내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                }

                adapter.notifyDataSetChanged();
            }
        });

        ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        // 창띄우기
        ad.setView(dialogView);
        ad.create();
        ad.show();
    }

    public void showChalList(View view){
        recyclerView = view.findViewById(R.id.recyclerView); //아이디 연결
        recyclerView.setHasFixedSize(true); //리사이클러뷰 기존 성능 강화
        layoutManager = new LinearLayoutManager(ct);
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>(); //Challenge 담을 어레이 리스트 (어댑터 쪽으로 날림)

        database = FirebaseDatabase.getInstance();

        databaseReference = database.getReference("challengeList"); //디비 테이블 연결
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 파이어베이스의 데이터 받아오는 곳
                arrayList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Challenge challenge = snapshot.getValue(Challenge.class); //Challenge 객체에 데이터 담기
                    arrayList.add(challenge);
                    cnt++;
                }
                adapter.notifyDataSetChanged(); //리스트 저장 새로고침
            }
            // 파이어베이스 데이터를 Challenge 클래스에 넣어주고 이를 arrayList에 넣어 Adapter에 쏘는 로직

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 디비 가져오던중 에러 발생시 동작
                Log.e("ChalListFragment", String.valueOf(error.toException()));
            }
        });

        adapter = new ChalListAdapter(arrayList, ct);
        recyclerView.setAdapter(adapter); //리사이클러뷰에 어댑터 연결
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chal_list, container, false);
        ct = container.getContext();

        showChalList(view);

        // dialog
        btn_addList = view.findViewById(R.id.addList);

        btn_addList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog(view);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }


}