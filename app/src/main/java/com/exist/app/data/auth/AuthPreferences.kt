package com.exist.app.data.auth

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.exist.app.domain.auth.AuthSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.authDataStore by preferencesDataStore(name = "exist_auth")

data class LocalAccount(
    val userId: String,
    val email: String,
    val password: String,
    val fullName: String,
    val birthday: String,
    val profilePhotoUri: String
)

class AuthPreferences(private val context: Context) {
    private object Keys {
        val accessToken = stringPreferencesKey("access_token")
        val userId = stringPreferencesKey("user_id")
        val email = stringPreferencesKey("email")
        val displayName = stringPreferencesKey("display_name")
        val needsOnboarding = booleanPreferencesKey("needs_onboarding")
        val accounts = stringPreferencesKey("accounts_json")
    }

    val session: Flow<AuthSession> = context.authDataStore.data.map { prefs: Preferences ->
        val token = prefs[Keys.accessToken].orEmpty()
        AuthSession(
            accessToken = token,
            userId = prefs[Keys.userId].orEmpty(),
            email = prefs[Keys.email].orEmpty(),
            displayName = prefs[Keys.displayName].orEmpty(),
            needsOnboarding = prefs[Keys.needsOnboarding] ?: false,
            isAuthenticated = token.isNotBlank()
        )
    }

    suspend fun saveSession(session: AuthSession) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.accessToken] = session.accessToken
            prefs[Keys.userId] = session.userId
            prefs[Keys.email] = session.email
            prefs[Keys.displayName] = session.displayName
            prefs[Keys.needsOnboarding] = session.needsOnboarding
        }
    }

    suspend fun clear() {
        context.authDataStore.edit { it.clear() }
    }

    suspend fun clearSessionOnly() {
        context.authDataStore.edit { prefs ->
            prefs.remove(Keys.accessToken)
            prefs.remove(Keys.userId)
            prefs.remove(Keys.email)
            prefs.remove(Keys.displayName)
            prefs.remove(Keys.needsOnboarding)
        }
    }

    suspend fun sessionState(): AuthSession {
        return session.first()
    }

    suspend fun getAccount(email: String): LocalAccount? {
        val list = readAccounts()
        return list.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
    }

    suspend fun upsertAccount(account: LocalAccount) {
        val list = readAccounts().toMutableList()
        val idx = list.indexOfFirst { it.email.equals(account.email, ignoreCase = true) }
        if (idx >= 0) {
            list[idx] = account
        } else {
            list.add(account)
        }
        saveAccounts(list)
    }

    suspend fun setPassword(email: String, newPassword: String): Boolean {
        val list = readAccounts().toMutableList()
        val idx = list.indexOfFirst { it.email.equals(email.trim(), ignoreCase = true) }
        if (idx < 0) return false
        val current = list[idx]
        list[idx] = current.copy(password = newPassword)
        saveAccounts(list)
        return true
    }

    private suspend fun readAccounts(): List<LocalAccount> {
        val raw = context.authDataStore.data.first()[Keys.accounts].orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    add(
                        LocalAccount(
                            userId = o.optString("userId"),
                            email = o.optString("email"),
                            password = o.optString("password"),
                            fullName = o.optString("fullName"),
                            birthday = o.optString("birthday"),
                            profilePhotoUri = o.optString("profilePhotoUri")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private suspend fun saveAccounts(accounts: List<LocalAccount>) {
        val array = JSONArray()
        accounts.forEach { account ->
            val o = JSONObject()
            o.put("userId", account.userId)
            o.put("email", account.email)
            o.put("password", account.password)
            o.put("fullName", account.fullName)
            o.put("birthday", account.birthday)
            o.put("profilePhotoUri", account.profilePhotoUri)
            array.put(o)
        }
        context.authDataStore.edit { prefs ->
            prefs[Keys.accounts] = array.toString()
        }
    }
}
