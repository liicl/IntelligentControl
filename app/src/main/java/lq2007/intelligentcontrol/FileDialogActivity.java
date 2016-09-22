package lq2007.intelligentcontrol;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于显示设置中的选择监控文件保存目录
 */
public class FileDialogActivity extends AppCompatActivity {

    ListView lst_list;

    BaseAdapter mAdapter;
    List<File> mFilePaths = new ArrayList<>();
    File mFileDir = Environment.getExternalStorageDirectory();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_dialog);

        //设置ListView
        lst_list = (ListView) findViewById(R.id.lst_camera_save_list);
        //1 Adapter
        mAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return mFilePaths.size();
            }

            @Override
            public Object getItem(int position) {
                return mFilePaths.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v= View.inflate(FileDialogActivity.this,R.layout.list_path_item,null);

                //设置显示
                mFileDir = (File) getItem(position);
                TextView tv = (TextView) v.findViewById(R.id.txt_list_fileItem);
                if(position == 0){
                    //返回上一级
                    //若系统版本支持,则居中显示
                    if(Build.VERSION.SDK_INT >= 17) {
                        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    }
                    tv.setText("↑返回上一级↑");
                } else {
                    tv.setText(mFileDir.getName());
                }

                return v;
            }
        };
        lst_list.setAdapter(mAdapter);

        //2 点击事件
        //短按
        lst_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //进入文件夹
                File f = (File) mAdapter.getItem(position);
                if(f != null){
                    loadFiles(f);
                }
            }
        });
        //长按
        lst_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //确定
                mFileDir = (File) mAdapter.getItem(position);
                if(mFileDir != null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(FileDialogActivity.this);
                    builder.setTitle("选定");
                    builder.setMessage("是否将该文件夹作为图片保存地址?");
                    builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(1,new Intent().putExtra("path", mFileDir.getAbsolutePath()));
                            finish();
                        }
                    });
                    builder.show();
                }
                return true;
            }
        });

        //3 显示
        Intent pData = getIntent();
        if(pData != null){
            String path = pData.getStringExtra("thisPath");
            if(path != null && (!TextUtils.isEmpty(path))){
                mFileDir = new File(path);
            }
        }
        Toast.makeText(FileDialogActivity.this, mFileDir.getPath(), Toast.LENGTH_SHORT).show();
        loadFiles(mFileDir);
    }

    /**
     * 生成文件列表并更新ArrayList
     * @param root 要显示文件夹目录File文件
     */
    private void loadFiles(File root){
        if(root == null || (!root.isDirectory())){
            return;
        }
        //获取文件夹
        File[] files = root.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if(pathname == null){
                    return false;
                }
                return pathname.isDirectory();
            }
        });
        //判断是否获得了正确的数组文件
        if(files == null){
            return;
        }
        //清空当前列表
        mFilePaths.clear();
        //添加 返回上一级
        if(root.getParentFile() != null){
            mFilePaths.add(root.getParentFile());
        }
        //获得--将文件夹置入显示ArrayList
        for(File f:files){
            mFilePaths.add(f);
        }
        //更新列表
        mAdapter.notifyDataSetChanged();
    }
}
