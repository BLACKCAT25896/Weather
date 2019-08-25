package com.example.weather;


import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.weather.adapter.WeatherForecastAdapter;
import com.example.weather.common.Common;
import com.example.weather.databinding.FragmentForecastBinding;
import com.example.weather.model.WeatherForecastResult;
import com.example.weather.retrofit.IOpenWeatherMap;
import com.example.weather.retrofit.RetrofitClient;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


public class ForecastFragment extends Fragment {
    private FragmentForecastBinding binding;
    CompositeDisposable compositeDisposable;
    IOpenWeatherMap weatherMap;
    static ForecastFragment instanse;

    public static ForecastFragment getInstanse() {

        if(instanse == null)
            instanse = new ForecastFragment();

        return instanse;
    }

    public ForecastFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        weatherMap = retrofit.create(IOpenWeatherMap.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_forecast, container, false);

        binding.forecastRecyclerView.setHasFixedSize(true);
        binding.forecastRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        getForecastWeatherInfo();




        View view= binding.getRoot();

        return view;
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

    private void getForecastWeatherInfo() {

        compositeDisposable.add(weatherMap.getForecastWeatherByLatLng(
                String.valueOf(Common.current_location.getLatitude()),
                String.valueOf(Common.current_location.getLongitude()),
                Common.APP_ID,
                "metric")
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<WeatherForecastResult>() {
            @Override
            public void accept(WeatherForecastResult weatherForecastResult) throws Exception {

                displayForecastWeather(weatherForecastResult);

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();

            }
        })

        );
    }

    private void displayForecastWeather(WeatherForecastResult weatherForecastResult) {

        binding.txtCityName.setText(new StringBuilder(weatherForecastResult.city.name));
        binding.txtGeoCoord.setText(new StringBuilder(weatherForecastResult.city.coord.toString()));

        WeatherForecastAdapter adapter = new WeatherForecastAdapter(getContext(),weatherForecastResult);
        binding.forecastRecyclerView.setAdapter(adapter);
    }

}
