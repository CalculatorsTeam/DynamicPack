package com.calculatorsteam.dynamicpack.pack.dynamicrepo

import com.calculatorsteam.dynamicpack.InputValidator
import com.calculatorsteam.dynamicpack.pack.DynamicResourcePack
import com.calculatorsteam.dynamicpack.pack.Remote
import com.calculatorsteam.dynamicpack.sync.SyncBuilder
import com.calculatorsteam.dynamicpack.util.JsonUtil.getString
import com.calculatorsteam.dynamicpack.util.JsonUtil.optLong
import com.calculatorsteam.dynamicpack.util.JsonUtil.optBoolean
import com.calculatorsteam.dynamicpack.util.Urls
import com.google.gson.JsonObject
import java.io.IOException

/**
 * Remote for type = dynamic_repo
 */
class DynamicRepoRemote : Remote() {

    companion object {
        const val REPO_JSON = "dynamicmcpack.repo.json"
        const val REPO_BUILD = "dynamicmcpack.repo.build"
    }

    lateinit var parent: DynamicResourcePack
        private set

    private lateinit var cachedCurrentJson: JsonObject
    private lateinit var cachedRemoteJson: JsonObject
    lateinit var url: String
        private set
    lateinit var buildUrl: String
        private set
    lateinit var packUrl: String
        private set
    lateinit var preferences: DynamicRepoPreferences
        private set

    override fun init(pack: DynamicResourcePack, remote: JsonObject) {
        parent = pack
        cachedRemoteJson = remote
        cachedCurrentJson = pack.currentJson

        url = remote.getString("url")
        InputValidator.throwIsUrlInvalid(url)

        buildUrl = "$url/$REPO_BUILD"
        packUrl = "$url/$REPO_JSON"

        preferences = DynamicRepoPreferences(pack, this)

        val signNoRequired = remote.optBoolean("sign_no_required", false)
        if (signNoRequired == remote.has("public_key")) {
            throw RuntimeException("Please add sign_no_required=true")
        }
    }

    override fun syncBuilder(): SyncBuilder = DynamicRepoSyncBuilder(parent, this)

    @Throws(IOException::class)
    override fun checkUpdateAvailable(): Boolean {
        val content = Urls.parseTextContent(buildUrl, 64)?.trim()
            ?: throw IOException("Empty response from $buildUrl")
        return currentBuild != content.toLong()
    }

    val currentBuild: Long
        get() = cachedCurrentJson.optLong("build", -1)

    val cachedCurrent: JsonObject
        get() = cachedCurrentJson

    val cachedRemote: JsonObject
        get() = cachedRemoteJson

    fun notifyNewRemoteJson(repoJson: JsonObject) {
        val copy = repoJson.deepCopy()
        preferences.notifyNewRemoteJson(copy)
    }
}