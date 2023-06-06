package com.example.slideshowdemo.model


data class FileDescriptors(
    val id: Int,
    val contentType: Int,
    val slideFilePath: String,
    val isFileExist: Boolean,
    val interval: Int
)