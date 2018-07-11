package com.byd.vtdr.fragment;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.byd.vtdr.ActivityImagesViewPager;
import com.byd.vtdr.MainActivity;
import com.byd.vtdr.Model;
import com.byd.vtdr.R;
import com.byd.vtdr.RemoteCam;
import com.byd.vtdr.ServerConfig;
import com.byd.vtdr.connectivity.IFragmentListener;
import com.byd.vtdr.utils.DownloadUtil;
import com.byd.vtdr.view.CustomDialog;
import com.byd.vtdr.view.MyViewPager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by byd_tw on 2018/3/15.
 */

public class FragmentPhotoPreview extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static CustomDialog customDialog = null;

    private ArrayList<Model> mParam1;
    private int mParam2;
    Unbinder unbinder;
    @BindView(R.id.vp_viewPager)
    MyViewPager vpViewPager;
    @BindView(R.id.tv_vpIndex)
    TextView tvVpIndex;
    @BindView(R.id.btn_back_to_gridview)
    ImageButton btnBackToGridview;
    @BindView(R.id.tv_title_photo)
    TextView tvTitlePhoto;
    @BindView(R.id.rl_bar_showTitle)
    RelativeLayout rlBarShowTitle;
    @BindView(R.id.btn_share_preview)
    ImageButton btnSharePreview;
    @BindView(R.id.btn_delete_preview)
    ImageButton btnDeletePreview;
    @BindView(R.id.btn_zoom)
    ImageButton btnZoom;
    @BindView(R.id.ll_bar_editPhoto)
    LinearLayout llBarEditPhoto;
    private static RemoteCam mRemoteCam;
    private MyImagesPagerAdapter myImagesPagerAdapter;
    private ArrayList<Model> photoLists;
    private static int currentItem;
    private static final int FADE_OUT = 1;
    private IFragmentListener mListener;
    public boolean reload = false;
    public boolean customDialogOR = false;

    public static FragmentPhotoPreview newInstance() {
        FragmentPhotoPreview fragmentPhotoPreview = new FragmentPhotoPreview();
        return fragmentPhotoPreview;
    }

    public void setRemoteCam(RemoteCam mRemoteCam) {
        this.mRemoteCam = mRemoteCam;
    }

    public FragmentPhotoPreview() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle bundle = getArguments();//从activity传过来的Bundle
        if (bundle != null) {
            photoLists = (ArrayList<Model>) bundle.getSerializable("mPhotoList");
            currentItem = (bundle.getInt("position"));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frament_photo_preview, container, false);
        unbinder = ButterKnife.bind(this, view);
        if (savedInstanceState != null) {
            ArrayList<String> name = savedInstanceState.getStringArrayList("name");
            ArrayList<String> time = savedInstanceState.getStringArrayList("time");
            ArrayList<Integer> size = savedInstanceState.getIntegerArrayList("size");
            photoLists = new ArrayList<>();
            int length = name.size();
            for (int i = 0; i < length; i++) {
                Model temp = new Model(name.get(i), time.get(i), size.get(i));
                photoLists.add(temp);
            }
            currentItem = (savedInstanceState.getInt("position"));
            customDialogOR = savedInstanceState.getBoolean("customDialogOR");
        }
        if (photoLists.size() != 0) {
            initData();
        }
        reload = true;
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (IFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement IFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void initData() {
        myImagesPagerAdapter = new MyImagesPagerAdapter(photoLists, this);
        vpViewPager.setAdapter(myImagesPagerAdapter);
        vpViewPager.setCurrentItem(currentItem, false);
        vpViewPager.setOffscreenPageLimit(0);
//        tvVpIndex.setText(currentItem + 1 + "/" + urlList.size());
        tvTitlePhoto.setText(photoLists.get(currentItem).getName());
        tvVpIndex.setText(currentItem + 1 + "/" + photoLists.size());
        vpViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentItem = position;
                tvVpIndex.setText(currentItem + 1 + "/" + photoLists.size());
                tvTitlePhoto.setText(photoLists.get(currentItem).getName());
            }
        });

        if (customDialogOR) {
            Dialoagview();
        }

        Message msg = mHandler.obtainMessage(FADE_OUT);
        mHandler.removeMessages(FADE_OUT);
        mHandler.sendMessageDelayed(msg, 3000);

    }


    @OnClick({R.id.btn_back_to_gridview, R.id.btn_share_preview, R.id.btn_export_preview, R.id.btn_delete_preview, R.id
            .btn_zoom})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_back_to_gridview:
                // TODO: 2017/11/29 删除完成了，需要去更新gridview
                reload = false;
                ((MainActivity) getActivity()).updateCardData();
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.btn_share_preview:
                sharePhoto();
                break;
            case R.id.btn_export_preview:
                countsDownload();
                break;
            case R.id.btn_delete_preview:
                Dialoagview();
                break;
            case R.id.btn_zoom:
                Intent intent = new Intent(view.getContext(), ActivityImagesViewPager.class);
                intent.putExtra("mPhotoList", photoLists);
                intent.putExtra("position", currentItem);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private final MyHandler mHandler = new MyHandler(this);

    public static class MyHandler extends Handler {
        private WeakReference<FragmentPhotoPreview> mFragmentPhotoPreview;

        MyHandler(FragmentPhotoPreview fragmentPhotoPreview) {
            mFragmentPhotoPreview = new WeakReference<>(fragmentPhotoPreview);
        }

        @Override
        public void handleMessage(Message msg) {
            FragmentPhotoPreview fragmentPhotoPreview = mFragmentPhotoPreview.get();
            if (fragmentPhotoPreview != null) {
                switch (msg.what) {
                    case FADE_OUT:
                        if (fragmentPhotoPreview.rlBarShowTitle.getVisibility() == View.VISIBLE) {
                            fragmentPhotoPreview.rlBarShowTitle.setVisibility(View.INVISIBLE);
                        }

                        if (fragmentPhotoPreview.llBarEditPhoto.getVisibility() == View.VISIBLE) {
                            fragmentPhotoPreview.llBarEditPhoto.setVisibility(View.INVISIBLE);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public class MyImagesPagerAdapter extends PagerAdapter {

        private ArrayList<Model> mPhotoLists;
        private MainActivity activity;

        MyImagesPagerAdapter(ArrayList<Model> mPhotoLists, FragmentPhotoPreview activity) {
//            this.imageUrls = imageUrls;
            this.mPhotoLists = mPhotoLists;
            this.activity = (MainActivity) getActivity();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
//            String url = imageUrls.get(position);
//            String url = "http://" + ServerConfig.VTDRIP + "/SD0/NORMAL/" +
//            model.getName();
            String url = "http://" + ServerConfig.VTDRIP + "/SD0/PHOTO/" + mPhotoLists.get
                    (position).getName();
            PhotoView photoView = new PhotoView(activity);
            Glide.with(activity).load(url).into(photoView);
            container.addView(photoView);

            photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float v, float v1) {
                    showTitleBar();
                }

                @Override
                public void onOutsidePhotoTap() {
                    showTitleBar();
                }
            });
            return photoView;
        }

        void showTitleBar() {
            //                    如下考虑算法优化
            if (rlBarShowTitle.getVisibility() == View.VISIBLE) {
                rlBarShowTitle.setVisibility(View.INVISIBLE);
            } else {
                rlBarShowTitle.setVisibility(View.VISIBLE);
            }

            if (llBarEditPhoto.getVisibility() == View.VISIBLE) {
                llBarEditPhoto.setVisibility(View.INVISIBLE);
            } else {
                llBarEditPhoto.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getCount() {
//            return imageUrls != null ? imageUrls.size() : 0;
            return mPhotoLists != null ? mPhotoLists.size() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        ArrayList<String> name = new ArrayList<>();
        ArrayList<String> time = new ArrayList<>();
        ArrayList<Integer> size = new ArrayList<>();

        int flag = photoLists.size();
        for (int i = 0; i < flag; i++) {
            name.add(photoLists.get(i).getName());
            time.add(photoLists.get(i).getTime());
            size.add(photoLists.get(i).getSize());
        }
        outState.putStringArrayList("name", name);
        outState.putStringArrayList("time", time);
        outState.putIntegerArrayList("size", size);
        outState.putInt("position", currentItem);
        outState.putBoolean("customDialogOR", customDialogOR);

        super.onSaveInstanceState(outState);
    }

    public void Dialoagview() {

        try {
            if ((this.customDialog != null) && this.customDialog.isShowing()) {
                this.customDialog.dismiss();
            }
        } catch (final IllegalArgumentException e) {
        } catch (final Exception e) {
        } finally {
            this.customDialog = null;
        }
        customDialogOR = true;
        CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
        customDialog = builder.cancelTouchOut(false).view(R.layout
                .fragment_doublebutton_dialog).style(R.style.CustomDialog).setTitle
                (getString(R.string.del_image_sure)).addViewOnclick(R.id.btn_dialogSure, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileHead;
                fileHead = "/tmp/SD0/PHOTO/";
                customDialogOR = false;

                mRemoteCam.deleteFile((String) (fileHead + photoLists.get(currentItem)
                        .getName()));
                photoLists.remove(currentItem);

                if (currentItem == 0 && photoLists.size() == 0) {
                    customDialog.dismiss();
                    reload = false;
                    ((MainActivity) getActivity()).updateCardData();
                    getActivity().getSupportFragmentManager().popBackStack();

                } else {
                    if (currentItem == photoLists.size()) {
                        currentItem--;
                        tvVpIndex.setText(currentItem + 1 + "/" + photoLists.size());
                        tvTitlePhoto.setText(photoLists.get(currentItem).getName());
                        myImagesPagerAdapter.notifyDataSetChanged();
                        // ((MainActivity)getActivity()).updateCardData();

                        customDialog.dismiss();
                    } else {
                        tvVpIndex.setText(currentItem + 1 + "/" + photoLists.size());
                        tvTitlePhoto.setText(photoLists.get(currentItem).getName());
                        myImagesPagerAdapter.notifyDataSetChanged();
                        customDialog.dismiss();
                    }

                }
            }
        }).addViewOnclick(R.id.btn_dialogCancel, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialogOR = false;
                customDialog.dismiss();
            }
        }).build();
        customDialog.show();
    }


    private void countsDownload() {
        String mGetFileName;
        mGetFileName = "http://" + ServerConfig.VTDRIP + "/SD0/PHOTO/" + photoLists.get
                (currentItem).getName();
        String fileName = Environment.getExternalStorageDirectory() + "/行车记录仪" + mGetFileName
                .substring(mGetFileName.lastIndexOf('/'));
        File file = new File(fileName);
        if (!file.exists()) {
            DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService
                    (Context.DOWNLOAD_SERVICE);
            //创建下载任务,downloadUrl就是下载链接
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mGetFileName));
            //指定下载路径和下载文件名
            request.setDestinationInExternalPublicDir("/行车记录仪/", photoLists.get(currentItem)
                    .getName());
            //不显示下载界面
            request.setVisibleInDownloadsUi(true);

            downloadManager.enqueue(request);

            Toast.makeText(getActivity(), R.string.Download_completed, Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getActivity(), R.string.File_downloaded, Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePhoto() {
        String mGetFileName;
        mGetFileName = "http://" + ServerConfig.VTDRIP + "/SD0/PHOTO/" + photoLists.get
                (currentItem).getName();
        String fileName = Environment.getExternalStorageDirectory() + "/行车记录仪" + mGetFileName
                .substring(mGetFileName.lastIndexOf('/'));
        final File file = new File(fileName);
        if (!file.exists()) {
            final DownloadUtil downloadUtil = DownloadUtil.get();
            downloadUtil.download(mGetFileName, "行车记录仪", new DownloadUtil.OnDownloadListener() {
                @Override
                public void onDownloadSuccess() {
                    Uri imageUri = Uri.fromFile(file);
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    shareIntent.setType("image/*");
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)));
                }

                @Override
                public void onDownloading(final int progress) {
                }

                @Override
                public void onDownloadFailed() {
                }

                @Override
                public void onDownloadStart() {
                }
            });
        } else {
            Uri imageUri = Uri.fromFile(file);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)));

        }
    }
}
