package edu.stanford.onyi98.myyelpclone

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.yelp.com/v3/"
private const val API_KEY = "rqzwdnLZSgW8gS72mMhPwhRVmmkHEmAfVa_fHfTJUi38iK8qrTUltIBqP9BNtuYT3-8BJfSakXVN4HVDC9R8oEJQJwgXW7WXaIV1GTEsjPCOd_pC5YuO6xH0aX2hX3Yx"
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!this.isOnline()) {
            showAlertDialog()

        }

        val restaurants = mutableListOf<YelpRestaurant>()
        val adapter = RestaurantsAdapter (this, restaurants)
        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager(this)

        val retrofit =
            Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
                .build()
        val yelpService = retrofit.create(YelpService::class.java)

        yelpService.searchRestuarants("Bearer $API_KEY", "Avocado Toast", "New York")
            .enqueue(object: Callback<YelpSearchResult>{

                override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                    Log.i(TAG, "onFailure $t")
                }


                override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                    Log.i(TAG, "onResponse $response")
                    val body = response.body()
                    if (body == null) {
                        Log.w(TAG, "Valid response was not received from Yelp API. Now exiting.")
                        return
                    }
                    restaurants.addAll(body.restaurants)
                    adapter.notifyDataSetChanged()
                }

            })


    }

    /*
    In order to show that the isOnline() is working, I am incorporating a dialog box, similar
    to that form MyMaps, to prompt the user to connect to internet and restart.
    Something for future use, would be presenting connection options or prompting them to
    come off airplane mode and reload the app.
     */
    private fun showAlertDialog() {
        val internetView = LayoutInflater.from(this).inflate(R.layout.dialog_connect_now, null)
        val dialog =
            AlertDialog.Builder(this)
                .setTitle("Not Connected to Internet")
                .setView(internetView)
                .setNegativeButton("Quitting", null)
                .setPositiveButton("I'll Connect and Re-Open", null)
                .show()
    }


    fun isOnline(): Boolean {
        val cm =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

}