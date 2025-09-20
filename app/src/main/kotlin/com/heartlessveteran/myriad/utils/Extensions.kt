package com.heartlessveteran.myriad.utils

/**
 * Utility extension functions for Project Myriad
 *
 * String extensions for null/empty checks and validation.
 *
 * Functions:
 * - isNullOrEmpty(): Returns `true` if the string is null or empty.
 *
 * @author Project Myriad Team
 */

fun String?.isNullOrEmpty(): Boolean = this == null || this.isEmpty()
