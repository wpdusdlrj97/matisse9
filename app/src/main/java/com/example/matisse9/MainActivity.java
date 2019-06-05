package com.example.matisse9;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    public static final int PICKER_REQUEST_CODE = 1;


    private UriAdapter mAdapter;
    ProgressDialog progressDialog;

    List<Uri> mSelected;
    // List that will contain the selected files/videos

    String mSelectedarray;

    Bitmap bitmap;


    //String ConvertImage;


    String[] bitmap_array;

    boolean check = true;

    String Profile_number = "Profile_number";
    String Profile_image = "Profile_image";
    String Profile_email = "Profile_email";

    String Get_email = "matisse19@naver.com";

    String num;
    String uploadimg;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setAdapter(mAdapter = new UriAdapter(this));

        Button button = findViewById(R.id.button);
        Button upload_button = findViewById(R.id.upload_button);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String[] PERMISSIONS = {
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                };

                if (hasPermissions(MainActivity.this, PERMISSIONS)) {
                    ShowPicker();
                    mAdapter.setData(null);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PICKER_REQUEST_CODE);
                }
            }
        });


        upload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSelected!=null){
                    UploadFunction();
                }else{
                    Toast.makeText(MainActivity.this, "이미지를 선택해주세요", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /**
     * Method that displays the image/video chooser.
     */
    public void ShowPicker() {
        Matisse.from(MainActivity.this)
                .choose(MimeType.ofAll())
                .countable(true)
                .maxSelectable(9)
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new Glide4Engine())
                .forResult(PICKER_REQUEST_CODE);
    }


    // 액티비티에 URI 모음을 가져온다
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKER_REQUEST_CODE && resultCode == RESULT_OK) {

            mSelected = Matisse.obtainResult(data);

            mAdapter.setData(mSelected);
            //Log.d("마티세 선택 ", mSelected.toString());


            /*

            //배열로 URI 모음 가져오기
            mSelectedarray = mSelected.toString();
            Log.d("마티세 선택1 ", mSelectedarray);

            //앞에 [ 제거
            mSelectedarray = mSelectedarray.substring(1);
            Log.d("마티세 선택2 ", mSelectedarray);

            //뒤에 ] 제거
            mSelectedarray = mSelectedarray.replaceAll("]", "");
            Log.d("마티세 선택3 ", mSelectedarray);

            // , 단위로 나누기
            String[] array = mSelectedarray.split(",");

            //비트맵 배열 생성
            bitmap_array = new String[9];

            for (int i = 0; i < array.length; i++) {
                //System.out.println(array[i]);

                array[i] = array[i].trim();
                Log.d("마티세  URI", array[i]);

                // 가져온 array[i]를 URI로 파싱 - > 비트맵으로 만들기
                try {
                    bitmap = (Bitmap) MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(array[i]));
                    Log.d("마티세 비트맵" + i, bitmap.toString());

                    num=String.valueOf(i);
                    uploadimg=getStringImage(bitmap);

                    ImageUploadToServerFunction(num, uploadimg);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            */


        }
    }


    //액티비티로 가져온 mSelect를  배열 -> [,] 제거한뒤 , 단위로 분할, 비트맵
    public void UploadFunction(){

        //배열로 URI 모음 가져오기
        mSelectedarray = mSelected.toString();
        Log.d("마티세 선택1 ", mSelectedarray);

        //앞에 [ 제거
        mSelectedarray = mSelectedarray.substring(1);
        Log.d("마티세 선택2 ", mSelectedarray);

        //뒤에 ] 제거
        mSelectedarray = mSelectedarray.replaceAll("]", "");
        Log.d("마티세 선택3 ", mSelectedarray);

        // , 단위로 나누기
        String[] array = mSelectedarray.split(",");


        for (int i = 0; i < array.length; i++) {
            //System.out.println(array[i]);

            array[i] = array[i].trim();
            Log.d("마티세  URI", array[i]);

            // 가져온 array[i]를 URI로 파싱 - > 비트맵으로 만들기
            try {
                bitmap = (Bitmap) MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(array[i]));
                Log.d("마티세 비트맵" + i, bitmap.toString());

                num=String.valueOf(i);
                uploadimg=getStringImage(bitmap);

                ImageUploadToServerFunction(num, uploadimg);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    //비트맵을 String으로 변환
    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }


    public void ImageUploadToServerFunction(final String number, final String bitmap_s) {


        /*
            ByteArrayOutputStream byteArrayOutputStreamObject;

            byteArrayOutputStreamObject = new ByteArrayOutputStream();

            bitmap0.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStreamObject);

            byte[] byteArrayVar = byteArrayOutputStreamObject.toByteArray();

            ConvertImage = Base64.encodeToString(byteArrayVar, Base64.DEFAULT);
        */




        class AsyncTaskUploadClass extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {

                super.onPreExecute();

                //progressDialog = ProgressDialog.show(MainActivity.this, "Image is Uploading", "Please Wait", false, false);
            }

            @Override
            protected void onPostExecute(String string1) {

                super.onPostExecute(string1);

                // Dismiss the progress dialog after done uploading.
                //progressDialog.dismiss();

                // Printing uploading success message coming from server on android app.
                Toast.makeText(MainActivity.this, string1, Toast.LENGTH_LONG).show();


                // Setting image as transparent after done uploading.
                // register_image.setImageResource(android.R.color.transparent);


            }

            @Override
            protected String doInBackground(Void... params) {

                ImageProcessClass imageProcessClass = new ImageProcessClass();

                HashMap<String, String> HashMapParams = new HashMap<String, String>();


                //String uploadImage = getStringImage(bitmap);
                //Log.d("프로필 비트맵", bitmap.toString());

                HashMapParams.put(Profile_number, number);
                Log.d("프로필 번호", number);

                HashMapParams.put(Profile_email, Get_email);
                Log.d("프로필 이메일", Get_email);

                HashMapParams.put(Profile_image, bitmap_s);
                Log.d("프로필 이미지", bitmap_s);


                String FinalData = imageProcessClass.ImageHttpRequest("http://54.180.122.247/global_communication/matisse.php", HashMapParams);

                return FinalData;
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();

        AsyncTaskUploadClassOBJ.execute();
    }


    public class ImageProcessClass {

        public String ImageHttpRequest(String requestURL, HashMap<String, String> PData) {

            StringBuilder stringBuilder = new StringBuilder();

            try {

                URL url;
                HttpURLConnection httpURLConnectionObject;
                OutputStream OutPutStream;
                BufferedWriter bufferedWriterObject;
                BufferedReader bufferedReaderObject;
                int RC;

                url = new URL(requestURL);

                httpURLConnectionObject = (HttpURLConnection) url.openConnection();

                httpURLConnectionObject.setReadTimeout(19000);

                httpURLConnectionObject.setConnectTimeout(19000);

                httpURLConnectionObject.setRequestMethod("POST");

                httpURLConnectionObject.setDoInput(true);

                httpURLConnectionObject.setDoOutput(true);

                OutPutStream = httpURLConnectionObject.getOutputStream();

                bufferedWriterObject = new BufferedWriter(

                        new OutputStreamWriter(OutPutStream, "UTF-8"));

                bufferedWriterObject.write(bufferedWriterDataFN(PData));

                bufferedWriterObject.flush();

                bufferedWriterObject.close();

                OutPutStream.close();

                RC = httpURLConnectionObject.getResponseCode();

                if (RC == HttpsURLConnection.HTTP_OK) {

                    bufferedReaderObject = new BufferedReader(new InputStreamReader(httpURLConnectionObject.getInputStream()));

                    stringBuilder = new StringBuilder();

                    String RC2;

                    while ((RC2 = bufferedReaderObject.readLine()) != null) {

                        stringBuilder.append(RC2);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return stringBuilder.toString();
        }

        private String bufferedWriterDataFN(HashMap<String, String> HashMapParams) throws UnsupportedEncodingException {

            StringBuilder stringBuilderObject;

            stringBuilderObject = new StringBuilder();

            for (Map.Entry<String, String> KEY : HashMapParams.entrySet()) {

                if (check)

                    check = false;
                else
                    stringBuilderObject.append("&");

                stringBuilderObject.append(URLEncoder.encode(KEY.getKey(), "UTF-8"));

                stringBuilderObject.append("=");

                stringBuilderObject.append(URLEncoder.encode(KEY.getValue(), "UTF-8"));
            }

            return stringBuilderObject.toString();
        }

    }


    /**
     * Helper method that verifies whether the permissions of a given array are granted or not.
     *
     * @param context
     * @param permissions
     * @return {Boolean}
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Callback that handles the status of the permissions request.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PICKER_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                            MainActivity.this,
                            "Permission granted! Please click on pick a file once again.",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            MainActivity.this,
                            "Permission denied to read your External storage :(",
                            Toast.LENGTH_SHORT
                    ).show();
                }

                return;
            }
        }
    }

    //URI 모음을 어댑터로 전달해 하나하나 Glide로 넣어준다
    private static class UriAdapter extends RecyclerView.Adapter<UriAdapter.UriViewHolder> {

        private final Activity context;
        private List<Uri> mUris;
        //private List<String> mPaths;

        Bitmap bitmap9;

        public UriAdapter(Activity context) {
            this.context = context;
            //this.mList = list;
        }


        void setData(List<Uri> uris) {
            mUris = uris;
            //mPaths = paths;
            notifyDataSetChanged();
        }

        @Override
        public UriViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new UriViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.uri_item, parent, false));
        }

        @Override
        public void onBindViewHolder(UriViewHolder holder, int position) {

            // 가져온 URI Glide를 통해 ImageVIew에 삽입
            Glide.with(context)
                    .load(mUris.get(position).toString())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .thumbnail(0.1f)
                    .fitCenter()
                    .into(holder.mUri);

            Log.d("사진 URI", mUris.get(position).toString());

            // 비트맵으로도 가져오기 (서버 업로드 용)
            try {
                bitmap9 = MediaStore.Images.Media.getBitmap(context.getContentResolver(), mUris.get(position));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //holder.mPath.setText(bitmap9.toString());

            Log.d("사진 비트맵", bitmap9.toString());

            Log.d("사진 포지션", String.valueOf(position));

            //여기서 이미지 업로드를 하는 방식은?
            //사진 업로드는 가능, mysql 저장은 어떤 식으로 할것인지
            //사진1, 사진2를 서버에 올릴 때마다 행을 추가해줄까?

            //아니면 비트맵을 다 쌓아두었다가 한꺼번에 올리기

        }

        @Override
        public int getItemCount() {
            return mUris == null ? 0 : mUris.size();
        }

        static class UriViewHolder extends RecyclerView.ViewHolder {

            private ImageView mUri;
            private TextView mPath;

            UriViewHolder(View contentView) {
                super(contentView);
                mUri = (ImageView) contentView.findViewById(R.id.uri);
                //mPath = (TextView) contentView.findViewById(R.id.path);
            }
        }


    }
}