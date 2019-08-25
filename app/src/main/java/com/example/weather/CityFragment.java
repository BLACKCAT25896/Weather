package com.example.weather;


import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.weather.common.Common;
import com.example.weather.databinding.FragmentCityBinding;
import com.example.weather.model.WeatherResult;
import com.example.weather.retrofit.IOpenWeatherMap;
import com.example.weather.retrofit.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.label305.asynctask.SimpleAsyncTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
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
            binding.cityNameSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                @Override
                public void onSearchStateChanged(boolean enabled) {

                }

                @Override
                public void onSearchConfirmed(CharSequence text) {
                    getWeatherInformation(text.toString());
                    binding.cityNameSearchBar.setLastSuggestions(cityList);

                }

                @Override
                public void onButtonClicked(int buttonCode) {

                }
            });

            binding.cityNameSearchBar.setLastSuggestions(cityList);
            binding.loading.setVisibility(View.GONE);
            binding.weatherPanel.setVisibility(View.VISIBLE);



        }
    }

    private void getWeatherInformation(String cityName) {


        compositeDisposable.add(weatherMap.getWeatherByCityName(cityName,
                Common.APP_ID,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {

                        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/")
                                .append(weatherResult.getWeather().get(0).getIcon())
                                .append(".png").toString()).into(binding.imageWeather);

                        binding.txtCityName.setText(weatherResult.getName());
                        binding.description.setText(new StringBuilder("Weather in ")
                                .append(weatherResult.getName()).toString());
                        binding.temperature.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getTemp())).append("°C").toString());
                        binding.txtDateTime.setText(Common.convertUnixToDate(weatherResult.getDt()));
                        binding.txtPressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hpa").toString());
                        binding.txtHumidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append(" %").toString());
                        binding.txtSunrise.setText(Common.convertUnixToHour(weatherResult.getSys().getSunrise()));
                        binding.txtSunset.setText(Common.convertUnixToHour(weatherResult.getSys().getSunset()));
                        binding.txtGeoCoord.setText(new StringBuilder(weatherResult.getCoord().toString()).toString());


                        binding.weatherPanel.setVisibility(View.VISIBLE);
                        binding.loading.setVisibility(View.GONE);





                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                })
        );





    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}
