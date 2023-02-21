package org.meveo.postman;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.meveo.model.customEntities.*;
import org.meveo.service.storage.RepositoryService;
import org.meveo.service.crm.impl.CurrentUserProducer;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.model.storage.Repository;
import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.service.git.GitHelper;
import org.meveo.security.MeveoUser;


import javax.script.*;

import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import javax.script.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.Instant;

import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;

public class ConvertTestEnvironment extends Script {

    private final static Logger log = LoggerFactory.getLogger(ConvertTestEnvironment.class);
  	private CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
	private RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private MeveoModuleService moduleService = getCDIBean(MeveoModuleService.class);
	private Repository defaultRepo = repositoryService.findDefaultRepository();
    private CurrentUserProducer currentUserProducer = getCDIBean(CurrentUserProducer.class);

    private String result;
    public String getResult() {
        return result;
    }

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);

        try {
            List<PostmanEnvironment> env = crossStorageApi.find(PostmanEnvironment.class).limit(1).getResults();

            if (env == null) {
                return;
            }

            var postmanEnv = env.get(0);

            if (postmanEnv == null) {
                result = "Not found any postman environment.";   
                return;
            }

            var testEnv = loadEnvironment(postmanEnv);

            if (testEnv == null) {
                result = "Can not parse the postman environment.";
                return;
            }

            crossStorageApi.createOrUpdate(defaultRepo, testEnv);

            result = "OK";
        }
        catch (Exception ex) {
            result = getStackTrace(ex);
            // throw new RuntimeException("Failed to save api testing environment.", ex);
        }
        
    }

    public apiTestEnvironment loadEnvironment(PostmanEnvironment postmanEnv) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(postmanEnv.getContent(), Map.class);        

        if (map == null) {            
            return null;
        }

        ArrayList<Object> values = (ArrayList<Object>)map.get("values");

        Map<String, String> resultData = new HashMap<String, String>();

        for(Object rawValue : values) {
			Map<String, Object> value = (Map<String, Object>) rawValue;
			Boolean enabled = (Boolean)value.get("enabled");
			if(enabled){
				String key=(String)value.get("key");
				String val=(String)value.get("value");
				resultData.put(key,val);
				log.info("Added "+key+" => "+val+ " to context");
			}
		}
        
        apiTestEnvironment testEnv = new apiTestEnvironment();
        testEnv.setName(postmanEnv.getCode());
        testEnv.setVariables(resultData);

        return testEnv;
	}

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
   }

}
