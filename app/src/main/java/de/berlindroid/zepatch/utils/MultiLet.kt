package de.berlindroid.zepatch.utils

inline fun <T1 : Any, T2 : Any, R : Any> T1?.multiLet(p2: T2?, block: (T1, T2) -> R?): R? {
    return if (this != null && p2 != null) block(this, p2) else null
}

inline fun <T1 : Any, T2 : Any, T3 : Any, R : Any> T1?.multiLet(p2: T2?, p3: T3?, block: (T1, T2, T3) -> R?): R? {
    return if (this != null && p2 != null && p3 != null) block(this, p2, p3) else null
}
