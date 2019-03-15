package com.appdynamics.extensions.extensionstarter;

import com.appdynamics.extensions.http.HttpClientUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

/**
 * @author Satish Muddam
 */

@RunWith(MockitoJUnitRunner.class)
public class HttpClientUtilsTest {

    @Mock
    private CloseableHttpResponse closeableHttpResponseMock;

    @Mock
    private CloseableHttpClient httpClientMock;

    @Test
    public void testHttp() throws IOException {

        Mockito.when(httpClientMock.execute(Mockito.any(HttpGet.class))).thenReturn(closeableHttpResponseMock);

        HttpClientUtils httpClientUtils = new HttpClientUtils();
        httpClientUtils.getResponseAsStr(httpClientMock, "http://www.google.com");


        Mockito.verify(httpClientMock, Mockito.times(1)).execute(Mockito.any(HttpGet.class));
        Mockito.verify(closeableHttpResponseMock, Mockito.times(1)).close();
    }
}