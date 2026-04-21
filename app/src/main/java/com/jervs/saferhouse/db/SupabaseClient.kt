package com.jervs.saferhouse.db

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseManager {
    private const val SUPABASE_URL = "https://fpjmwhiufswcxynosroq.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_8w3qk8rXDU64GYpim3T-KA_KhHqLtii"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Postgrest)
            install(Realtime)
        }
    }
}
