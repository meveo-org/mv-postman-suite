package org.meveo.postman;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class SavePostmanCollection extends Script {
	
	private final static Logger log = LoggerFactory.getLogger(NhanTesting.class);
    private CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private MeveoModuleService moduleService = getCDIBean(MeveoModuleService.class);
    private Repository defaultRepo = repositoryService.findDefaultRepository();
    private CurrentUserProducer currentUserProducer = getCDIBean(CurrentUserProducer.class);

    private String moduleCode;

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    private String result;

    public String getResult() {
        return result;
    }

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);
        try {
            MeveoUser user = currentUserProducer.getCurrentUser();
            var module = moduleService.findByCode(moduleCode);
            File modulePathDoc = GitHelper.getRepositoryDir(user, module.getGitRepository());
            if (modulePathDoc == null) {
                throw new BusinessException("cannot load postman collection, module directory not found");
            }
            var files = modulePathDoc.toPath().resolve("facets").resolve("postman").toFile();
            for (File f : files.listFiles()) {

                PostmanCollection postmanTestCollection = new PostmanCollection();
                postmanTestCollection.setCode(f.getName());
                postmanTestCollection.setContent(readFile(f));
                postmanTestCollection.setContentHash(getChecksum(f));

                try {
                    crossStorageApi.createOrUpdate(defaultRepo, postmanTestCollection);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to save followed merchant.", e);
                }
            }
        } catch (Exception ex) {
            result = ex.toString();
        }
    }

    private String readFile(File file) {
        StringBuilder data = new StringBuilder();
        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                data.append(myReader.nextLine());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return data.toString();
    }

    private String getChecksum(File file) throws IOException {
		String checksum = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(Files.readAllBytes(file.toPath()));
            byte[] digest = md.digest();
            checksum = DatatypeConverter.printHexBinary(digest).toUpperCase();
            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
		return checksum;
    }
	
}
