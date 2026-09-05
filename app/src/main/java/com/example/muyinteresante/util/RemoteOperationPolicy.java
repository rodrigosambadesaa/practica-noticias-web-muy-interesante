package com.example.muyinteresante.util;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLException;

/** Decisions shared by remote operations and kept free of Android UI state. */
public final class RemoteOperationPolicy {
    public enum PreflightDecision { LOAD_REMOTE, USE_OFFLINE }
    public enum FailureAction { USE_HTTP_RESULT, FEED_UNAVAILABLE, CONNECTIVITY_PROBLEM }

    private RemoteOperationPolicy() { }

    public static PreflightDecision beforeRemoteOperation(boolean usableNetwork) {
        return usableNetwork ? PreflightDecision.LOAD_REMOTE : PreflightDecision.USE_OFFLINE;
    }

    /** A valid HTTP response proves communication and must never trigger a generic probe. */
    public static FailureAction classifyHttpResponse(int statusCode) {
        return statusCode >= 200 && statusCode < 400
                ? FailureAction.USE_HTTP_RESULT
                : FailureAction.FEED_UNAVAILABLE;
    }

    /** Classifies only failures for which the request could not prove server communication. */
    public static FailureAction classifyAmbiguousFailure(Throwable error, boolean generalInternetWorks) {
        if (!isAmbiguousConnectivityFailure(error)) {
            return FailureAction.FEED_UNAVAILABLE;
        }
        return generalInternetWorks
                ? FailureAction.FEED_UNAVAILABLE
                : FailureAction.CONNECTIVITY_PROBLEM;
    }

    public static boolean isAmbiguousConnectivityFailure(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof UnknownHostException
                    || current instanceof ConnectException
                    || current instanceof SocketTimeoutException
                    || current instanceof SSLException) {
                return true;
            }
            current = current.getCause();
        }
        return error instanceof IOException;
    }
}
