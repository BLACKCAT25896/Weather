package com.example.weather;


import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.weather.common.Common;
import com.example.weather.databinding.FragmentTodayWeatherBinding;
import com.example.weather.model.WeatherResult;
import com.example.weather.retrofit.IOpenWeatherMap;
import com.example.weather.retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class TodayWeatherFragment extends Fragment {
    private FragmentTodayWeatherBinding binding;
    CompositeDisposable compositeDisposable;
    IOpenWeatherMap weatherMap;


    static TodayWeatherFragment instance;

    public static TodayWeatherFragment getInstance() {
        if(instance == null)
            instance = new TodayWeatherFragment();

        return instance;

    }

    public TodayWeatherFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        weatherMap = retrofit.create(IOpenWeatherMap.class);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_today_weather, container, false);
        View itemView = binding.getRoot();

        getWeatherInfo();
        return itemView;
    }

    private void getWeatherInfo() {


        compositeDisposable.add(weatherMap.getWeatherByLatLng(String.valueOf(Common.current_location),
                String.valueOf(Common.current_location.getLongitude()),
                Common.APP_ID,
                "metric")
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<WeatherResult>() {
            @Override
            public void accept(WeatherResult weatherResult) throws Exception {
                //load Image
                Picasso.get().load(new StringBuilder("https://openweathermap.org/img/wn/")
                        .append(weatherResult.getWeather().get(0).getIcon())
                .append(".png").toString()).into(binding.weatherImage);
                binding.txtCityName.setText(weatherResult.getName());
                binding.description.setText(new StringBuilder("Weather in ")
                        .append(weatherResult.getName()).toString());
                binding.temperature.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getTemp())).append("°C").toString());
                binding.txtDateTime.setText(Common.convertUnixToDate(weatherResult.getDt()));
                binding.txtPressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hpa").toString());
                binding.txtHumidity.setText(new StringBuilder(weatherResult.getMain().getHumidity()).append(" %").toString());
                binding.txtSunrise.setText(Common.convertUnixToHour(weatherResult.getSys().getSunrise()));
                binding.txtSunset.setText(Common.convertUnixToHour(weatherResult.getSys().getSunset()));
                binding.txtGeoCoord.setText(new StringBuilder("[").append(weatherResult.getCoord().toString()).append("]").toString());


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

}
