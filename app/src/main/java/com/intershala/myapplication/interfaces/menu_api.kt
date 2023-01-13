package com.intershala.myapplication.interfaces

import com.intershala.myapplication.datamodels.MenuItem


enum class RequestType {
    READ, OFFLINE_UPDATE
}

interface MenuApi {
    fun onFetchSuccessListener(list: ArrayList<MenuItem>, requestType: RequestType)
}