package com.sesxh.magichook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.sesxh.magicplugin.annotation.ClickJitterClick;
import com.sesxh.magicplugin.annotation.HookMethod;
import com.sesxh.magicplugin.annotation.IgnoreHookMethod;
import com.sesxh.magicplugin.annotation.NodeId;
import com.sesxh.magicplugin.annotation.TraceId;
import com.sesxh.magicplugin.annotation.Value;
import com.sesxh.magicplugin.tools.CheckClickJitterUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;


public class MainActivity extends AppCompatActivity {

    private int num1;
    @BindView(R.id.rv)
    RecyclerView rv;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        test(7,num1,7);
        CheckClickJitterUtil.sDuration=3000;
        List<String> s=new ArrayList<>();
        s.add("第一个");
        s.add("第二个");
        s.add("第三个");
        s.add("第四个");
        MyAdapter adapter=new MyAdapter(s);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull  BaseQuickAdapter<?, ?> adapter, @NonNull  View view, int position) {
                Log.e("测试点击：","点击了onItemClick");
            }
        });
        adapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull  BaseQuickAdapter<?, ?> adapter, @NonNull  View view, int position) {
                Log.e("测试点击：","点击了onItemChildClick");
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            @IgnoreHookMethod
            public void onClick(View v) {
                Log.e("测试点击：","点击了不能点击的");
            }
        });
    }

    @HookMethod
    public int test(@TraceId int num, @NodeId int num1, @Value int num2)  {
        int a = num1 + num2;
        return a;
    }

    @HookMethod
    private DataBean initData(int args1,String args2,DataBean bean ){
        return new DataBean();
    }



@OnClick(R.id.btn)
void oncc(View v){
    Log.e("测试点击：","点击了Butterknife");
}

    private  class  DataBean{
        String d;
        int c;

        public DataBean() {
        }

        public DataBean(String d, int c) {
            this.d = d;
            this.c = c;
        }

        public String getD() {
            return d;
        }

        public void setD(String d) {
            this.d = d;
        }

        public int getC() {
            return c;
        }

        public void setC(int c) {
            this.c = c;
        }

        @Override
        @IgnoreHookMethod
        public String toString() {
            return "DataBean{" +
                    "d=" + d +
                    ", c=" + c +
                    '}';
        }
    }

    class MyAdapter extends BaseQuickAdapter<String,BaseViewHolder>{

        public MyAdapter(List<String> datas) {
            super(R.layout.activity_main_rv,datas);
            addChildClickViewIds(R.id.tv);
        }

        @Override
        protected void convert(BaseViewHolder holder, String s) {
            holder.setText(R.id.tv,s);
        }
    }

}