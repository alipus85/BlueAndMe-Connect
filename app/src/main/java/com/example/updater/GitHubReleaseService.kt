package com.example.updater

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface GitHubReleaseService {
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Authorization") authHeader: String? = null,
        @Header("Accept") acceptHeader: String = "application/vnd.github.v3+json"
    ): Response<GitHubReleaseDto>
}
