package [=package]

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import io.micrometer.core.annotation.Timed;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.*;
<#if jwt=="true">
import javax.annotation.security.*;
</#if>
import reactor.core.publisher.*;

import [=servicePath]
import [=entityPath]

import java.util.*;


/**
* Rest Controller that handles incoming requests for [=entityType].
 * This controller generates Timed metrics using Micrometer support.
 * The @Timed annotation creates a Timed time series
 * named http_server_requests which by default contains dimensions
 * for the HTTP status of the response, HTTP method, exception type if the request fails,
 * and the pre-variable substitution parameterized endpoint URI.
 * In addition it also exposes time metrics for each of the method.
*/
@RestController
class [=className] {

    private static Logger log = LoggerFactory.getLogger([=className].class);

    @Autowired
    private [=serviceName] service;

    [=methods]

}


