package org.meveo.postman;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.meveo.service.storage.RepositoryService;
import org.meveo.service.crm.impl.CurrentUserProducer;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.model.customEntities.PostmanCollection;
import org.meveo.model.storage.Repository;
import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.service.git.GitHelper;
import org.meveo.security.MeveoUser;
import javax.script.*;
import javax.xml.bind.DatatypeConverter;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.time.Instant;
import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;

public class GetPostmanCollection extends Script {

    private final static Logger log = LoggerFactory.getLogger(GetPostmanCollection.class);
    private CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private MeveoModuleService moduleService = getCDIBean(MeveoModuleService.class);
    private Repository defaultRepo = repositoryService.findDefaultRepository();
    private CurrentUserProducer currentUserProducer = getCDIBean(CurrentUserProducer.class);

    private String code;

    public void setCode(String code) {
        this.code = code;
    }

    private String result;
    public String getResult() {
        return result;
    }

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);

        PostmanCollection collection = crossStorageApi.find(PostmanCollection.class)
                                                                    .by("code", code)
                                                                    .getResults()
                                                                    .get(0);
        result = collection.getContent();

    }
}
