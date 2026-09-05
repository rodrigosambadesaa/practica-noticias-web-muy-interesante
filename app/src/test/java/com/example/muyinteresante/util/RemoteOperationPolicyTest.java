package com.example.muyinteresante.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.ConnectException;
import org.junit.Test;

public class RemoteOperationPolicyTest {
    @Test
    public void offlineImmediatelyUsesCacheWithoutRemoteRequest() {
        assertEquals(RemoteOperationPolicy.PreflightDecision.USE_OFFLINE,
                RemoteOperationPolicy.beforeRemoteOperation(false));
    }

    @Test
    public void usableNetworkAllowsTheRealRequest() {
        assertEquals(RemoteOperationPolicy.PreflightDecision.LOAD_REMOTE,
                RemoteOperationPolicy.beforeRemoteOperation(true));
    }

    @Test
    public void successfulHttpResponseIsDefinitive() {
        assertEquals(RemoteOperationPolicy.FailureAction.USE_HTTP_RESULT,
                RemoteOperationPolicy.classifyHttpResponse(200));
        assertEquals(RemoteOperationPolicy.FailureAction.USE_HTTP_RESULT,
                RemoteOperationPolicy.classifyHttpResponse(304));
    }

    @Test
    public void feedFailureWithGeneralInternetReportsFeedUnavailable() {
        assertEquals(RemoteOperationPolicy.FailureAction.FEED_UNAVAILABLE,
                RemoteOperationPolicy.classifyHttpResponse(503));
        assertEquals(RemoteOperationPolicy.FailureAction.FEED_UNAVAILABLE,
                RemoteOperationPolicy.classifyAmbiguousFailure(
                        new ConnectException("feed unavailable"), true));
    }

    @Test
    public void ambiguousFailureWithoutGeneralInternetReportsConnectivity() {
        ConnectException error = new ConnectException("offline");
        assertTrue(RemoteOperationPolicy.isAmbiguousConnectivityFailure(error));
        assertEquals(RemoteOperationPolicy.FailureAction.CONNECTIVITY_PROBLEM,
                RemoteOperationPolicy.classifyAmbiguousFailure(error, false));
    }
}
