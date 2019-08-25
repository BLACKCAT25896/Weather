package com.example.weather;


import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.weather.databinding.FragmentCityBinding;
import com.example.weather.retrofit.IOpenWeatherMap;
import com.example.weather.retrofit.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.label305.asynctask.SimpleAsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class CityFragment extends Fragment {
    private FragmentCityBinding binding;
    private List<String> cityList;
    CompositeDisposable compositeDisposable;
    IOpenWeatherMap weatherMap;

    static CityFragment instance;

    public static CityFragment getInstance() {
        if (instance == null)
            instance = new CityFragment();

        return instance;

    }


    public CityFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        weatherMap = retrofit.create(IOpenWeatherMap.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_city, container, false);

        binding.cityNameSearchBar.setEnabled(false);


        new LoadCiteis().execute();


        View view = binding.getRoot();
        return view;
    }

    private class LoadCiteis extends SimpleAsyncTask<List<String>> {
        @Override
        protected List<String> doInBackground() {


            cityList = new ArrayList<>();
            try {
                StringBuilder builder = new StringBuilder();
                InputStream is = getResources().openRawResource(R.raw.city_list);


                GZIPInputStream gzipInputStream = new GZIPInputStream(is);
                InputStreamReader reader = new InputStreamReader(gzipInputStream);
                BufferedReader bf = new BufferedReader(reader);
                String readed;
                 while ((readed= bf.readLine())!=null){

                     builder.append(readed);
                     cityList = new Gson().fromJson(builder.toString(),new TypeToken<List<String>>(){}.getType());

                 }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return cityList;
        }

        @Override
        protected void onSuccess(final List<String> cityList) {
            super.onSuccess(cityList);
            binding.cityNameSearchBar.setEnabled(true);
            binding.cityNameSearchBar.addTextChangeListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    List<String> suggest = new ArrayList<>();
                    for (String search: cityList){
                        if(search.toLowerCase().contains(binding.cityNameSearchBar.getText().toLowerCase()));

                    }

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
    }
}
