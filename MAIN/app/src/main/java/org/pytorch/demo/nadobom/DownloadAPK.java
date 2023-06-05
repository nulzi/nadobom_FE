package org.pytorch.demo.nadobom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadAPK extends AppCompatActivity {
    Context context;
    private File outputFile;
    ProgressBar progressBar;
    TextView textView;
    LinearLayout linearLayout;
    DownloadFileFromURL downloadFileAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_apk);
        context = this.getBaseContext();
        Log.d("MyTag","download apk create");
//        linearLayout = (LinearLayout) findViewById(R.id.downloadprogress_layout);
        textView = (TextView) findViewById(R.id.txtView01);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        downloadAPK();
    }

    private void downloadAPK() {
        Log.d("MyTag","downloadapk()");
        // 백그라운드 객체를 만들어 주어야 다운로드 취소가 제대로 동작됨
        downloadFileAsyncTask = new DownloadFileFromURL();
        downloadFileAsyncTask.execute(APIConfig.BASE_URL + "update/excute");
    }

    class DownloadFileFromURL extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            Log.d("MyTag","onPreExecute()");
            super.onPreExecute();
            progressBar.setProgress(0);
        }

        @Override
        protected String doInBackground(String... apkurl) {
            Log.d("MyTag","doInBackground()");
            int count;
            int lenghtOfFile = 0;
            InputStream input = null;
            OutputStream fos = null;

            try {
                URL url = new URL(apkurl[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                lenghtOfFile = connection.getContentLength(); // 파일 크기를 가져옴

                File path = getFilesDir();
                Log.d("MyTag","getFilesDir() "+path);
                outputFile = new File(path, "nadobom.apk");
                if (outputFile.exists()) { // 기존 파일 존재시 삭제하고 다운로드
                    outputFile.delete();
                }

                input = new BufferedInputStream(url.openStream());
                fos = new FileOutputStream(outputFile);
                byte data[] = new byte[1024];
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return String.valueOf(-1);
                    }
                    total = total + count;
                    if (lenghtOfFile > 0) { // 파일 총 크기가 0 보다 크면
                        publishProgress((int) (total * 100 / lenghtOfFile));
                    }
                    fos.write(data, 0, count); // 파일에 데이터를 기록
                }

                fos.flush();

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("UpdateAPP", "Update error! " + e.getMessage());
            } finally {
                Log.d("MyTag","doInBackground() finally");
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException ioex) {
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ioex) {
                    }
                }
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
//            Log.d("MyTag","onProgressUpdate()");
            super.onProgressUpdate(progress);
            // 백그라운드 작업의 진행상태를 표시하기 위해서 호출하는 메소드
            progressBar.setProgress(progress[0]);
            textView.setText("다운로드 : " + progress[0] + "%");
        }

        protected void onPostExecute(String result) {
            Log.d("MyTag","onPostExecute()");
            if (result == null) {
                progressBar.setProgress(0);
                Toast.makeText(getApplicationContext(), "다운로드 완료되었습니다.", Toast.LENGTH_LONG).show();

                System.out.println("getPackageName : " + getPackageName());
                System.out.println("APPLICATION_ID Path : " + BuildConfig.APPLICATION_ID);
                System.out.println("outputFile Path : " + outputFile.getAbsolutePath());
                System.out.println("File getPath : " + outputFile.getPath());

                // 미디어 스캐닝
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{outputFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String s, Uri uri) {

                    }
                });


                // 다운로드한 파일 실행하여 업그레이드 진행하는 코드
                if (Build.VERSION.SDK_INT >= 24) {
                    // Android Nougat ( 7.0 ) and later
                    installApk(outputFile);
                    System.out.println("SDK_INT 24 이상 ");
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri apkUri = Uri.fromFile(outputFile);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                    System.out.println("SDK_INT 23 이하 ");
                }

            } else {
                Toast.makeText(getApplicationContext(), "다운로드 에러", Toast.LENGTH_LONG).show();
            }
        }
        protected void onCancelled() {
            Log.d("MyTag","onCancelled()");
            // cancel메소드를 호출하면 자동으로 호출되는 메소드
            progressBar.setProgress(0);
            textView.setText("다운로드 진행 취소됨");
        }
    }

    public void installApk(File file) {
        Log.d("MyTag","installApk()");
        Log.d("MyTag","context.getApplicationContext().getPackageName(): "+context.getApplicationContext().getPackageName());
        Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider",file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
        finish();
    }
}