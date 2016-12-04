package org.mariotaku.twidere.util

import android.accounts.Account
import android.accounts.AccountManager
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.annotation.ColorInt
import com.bluelinelabs.logansquare.LoganSquare
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.extension.model.account_name
import org.mariotaku.twidere.model.ParcelableCredentials
import org.mariotaku.twidere.annotation.AuthTypeInt
import org.mariotaku.twidere.model.ParcelableCredentialsCursorIndices
import org.mariotaku.twidere.model.account.cred.BasicCredentials
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.model.account.cred.EmptyCredentials
import org.mariotaku.twidere.model.account.cred.OAuthCredentials
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts
import org.mariotaku.twidere.util.support.AccountManagerSupport
import java.util.*

/**
 * Created by mariotaku on 2016/12/3.
 */
fun migrateAccounts(am: AccountManager, db: SQLiteDatabase) {
    am.getAccountsByType(ACCOUNT_TYPE).map { account ->
        AccountManagerSupport.removeAccount(am, account, null, null, null)
    }

    val cur = db.query(Accounts.TABLE_NAME, Accounts.COLUMNS, null, null, null, null, null) ?: return
    try {
        val indices = ParcelableCredentialsCursorIndices(cur)
        cur.moveToFirst()
        while (!cur.isAfterLast) {
            val credentials = indices.newObject(cur)
            val account = Account(credentials.account_name, ACCOUNT_TYPE)
            val userdata = Bundle()
            userdata.putString(ACCOUNT_USER_DATA_KEY, credentials.account_key.toString())
            userdata.putString(ACCOUNT_USER_DATA_TYPE, credentials.account_type)
            userdata.putString(ACCOUNT_USER_DATA_CREDS_TYPE, credentials.getCredentialsType())
            userdata.putString(ACCOUNT_USER_DATA_ACTIVATED, credentials.is_activated.toString())
            userdata.putString(ACCOUNT_USER_DATA_USER, LoganSquare.serialize(credentials.account_user))
            userdata.putString(ACCOUNT_USER_DATA_EXTRAS, credentials.account_extras)
            userdata.putString(ACCOUNT_USER_DATA_COLOR, toHexColor(credentials.color))
            am.addAccountExplicitly(account, null, userdata)
            am.setAuthToken(account, ACCOUNT_AUTH_TOKEN_TYPE, LoganSquare.serialize(credentials.toCredentials()))
            cur.moveToNext()
        }
    } finally {
        cur.close()
    }
}

fun toHexColor(@ColorInt color: Int) = String.format(Locale.ROOT, "#%6X", color)

private fun ParcelableCredentials.toCredentials(): Credentials {
    when (auth_type) {
        AuthTypeInt.OAUTH, AuthTypeInt.XAUTH -> return toOAuthCredentials()
        AuthTypeInt.BASIC -> return toBasicCredentials()
        AuthTypeInt.TWIP_O_MODE -> return toEmptyCredentials()
    }
    throw UnsupportedOperationException()
}

@Credentials.Type
private fun ParcelableCredentials.getCredentialsType(): String {
    return ParcelableCredentialsUtils.getCredentialsType(auth_type)
}


private fun ParcelableCredentials.toOAuthCredentials(): OAuthCredentials {
    val result = OAuthCredentials()
    applyCommonProperties(result)
    result.consumer_key = consumer_key
    result.consumer_secret = consumer_secret
    result.access_token = oauth_token
    result.access_token_secret = oauth_token_secret
    result.same_oauth_signing_url = same_oauth_signing_url
    return result
}

private fun ParcelableCredentials.toBasicCredentials(): BasicCredentials {
    val result = BasicCredentials()
    applyCommonProperties(result)
    result.username = basic_auth_username
    result.password = basic_auth_password
    return result
}

private fun ParcelableCredentials.toEmptyCredentials(): EmptyCredentials {
    val result = EmptyCredentials()
    applyCommonProperties(result)
    return result
}

private fun ParcelableCredentials.applyCommonProperties(credentials: Credentials) {
    credentials.api_url_format = api_url_format
    credentials.no_version_suffix = no_version_suffix
}