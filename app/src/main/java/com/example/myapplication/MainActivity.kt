package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private lateinit var tvWeather: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvTemerature: TextView
    private lateinit var tvGetInfo: TextView
    private lateinit var tvSubmit: TextView
    private lateinit var etMessage: TextView
    private lateinit var rvMessage: RecyclerView

    //call api
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiService: ApiInterface by lazy {
        retrofit.create(ApiInterface::class.java)
    }

    // connect sql lite database
    val messageDAO: MessageDAO by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "Message"
        ).build().messageDAO()
    }

    var listMessage:List<Message> = listOf()
    val mAdapter: MessageAdapter by lazy {
        MessageAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvWeather = findViewById(R.id.tvWeather)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvTemerature = findViewById(R.id.tvTemerature)
        tvGetInfo = findViewById(R.id.tvGetInfo)
        tvSubmit = findViewById(R.id.tvSubmit)
        etMessage = findViewById(R.id.etMessage)
        rvMessage = findViewById(R.id.rvMessage)

        rvMessage.apply {
            mAdapter.submitList(listMessage)
            adapter = mAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        loadDataFromLocal()

        tvGetInfo.setOnClickListener {
            loadDataFromServer()
        }

        tvSubmit.setOnClickListener {
            saveMessageToLocal(etMessage.text.toString())
        }
    }

    fun loadDataFromServer(){
        val call = apiService.getWeatherFocast(
            key = KEY,
            city = CITY,
            aqi = "no"
        )
        call.enqueue(object : Callback<WeatherResponse>{
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    tvWeather.text = weatherResponse?.current?.condition?.text
                    tvHumidity.text ="Humidity: " + weatherResponse?.current?.humidity + "%"
                    tvTemerature.text ="Temperature :" +  weatherResponse?.current?.tempC + "C"
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("Errrror", t.message.toString())
                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun loadDataFromLocal(){
        GlobalScope.launch {
            listMessage = messageDAO.getAllMessage()
            mAdapter.submitList(listMessage)
        }
    }

    fun saveMessageToLocal(content: String){
        if (content.isBlank()) return

        val message = Message(null, content, getCurrentDate())
        GlobalScope.launch {
            messageDAO.insertMessage(message)
            loadDataFromLocal()
            etMessage.text = ""
        }
    }

    private fun getCurrentDate():String {
        val c: Date = Calendar.getInstance().time
        println("Current time => $c")
        val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        return  df.format(c)
    }

    companion object {
        private const val BASE_URL = "http://api.weatherapi.com/v1/"
        private const val KEY = "02c41f75d5674893935155932241204"
        private const val CITY = "Saint Petersburg City"
    }
}