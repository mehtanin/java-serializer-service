package com.service.javaserializerservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.adminshell.aas.v3.dataformat.aasx.InMemoryFile;
import io.adminshell.aas.v3.dataformat.rdf.Serializer;
import io.adminshell.aas.v3.dataformat.aasx.AASXSerializer;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.dataformat.json.JsonDeserializer;
import io.adminshell.aas.v3.dataformat.aasx.AASXDeserializer;
import org.apache.jena.riot.RDFLanguages;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    @Value("${inputEnvironment.path}")
    private String inputEnvPath;
    @Value("${outputEnvironment.path}")
    private String outputEnvPath;
    @Value("${additionalFile.path}")
    private String additionalEnvPath;

    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

    @RequestMapping("/jsonToRdf")
    String convertJsonToRdf() throws Exception {

        String fileToLoad = inputEnvPath + "/aasenv.json";
        String jsonAsString = readFileAsString(fileToLoad);
        AssetAdministrationShellEnvironment env = new JsonDeserializer().read(jsonAsString);
        List<AssetAdministrationShell> aasList = env.getAssetAdministrationShells();
        System.out.println("#### AAS Environment: " + fileToLoad);
        aasList.forEach(aas -> System.out.println("Environment contains AAS: " + aas.getIdShort() + " Submodel: " + aas.getSubmodels().stream().count()));

        Path storagePath = Paths.get(outputEnvPath+ "/rdfFromJson.ttl");
        String envToRdfString = new Serializer().serialize(env, RDFLanguages.TURTLE);
        Files.write(storagePath, envToRdfString.getBytes(StandardCharsets.UTF_8));
        return "Success!";
    }

    @RequestMapping("/rdfToAasx")
    public String convertRdfToAasx() throws Exception {
        String fileToLoad = inputEnvPath + "/rdfFromJson.ttl";
        String rdfAsString = readFileAsString(fileToLoad);
        AssetAdministrationShellEnvironment env = new Serializer().read(rdfAsString);
        List<AssetAdministrationShell> aasList = env.getAssetAdministrationShells();
        System.out.println("#### AAS Environment: " + fileToLoad);
        aasList.forEach(aas -> System.out.println("Environment contains AAS: " + aas.getIdShort() + " Submodel: " + aas.getSubmodels().stream().count()));

        // Adding additional files to aasx environment
        List<InMemoryFile> fileList = new ArrayList<>();
        Path pdfPath = Paths.get(additionalEnvPath + "/test.pdf");
        byte [] pdfData = Files.readAllBytes(pdfPath);
        InMemoryFile inMemoryFile = new InMemoryFile(pdfData, "/aasx/OperatingManual.pdf");
        fileList.add(inMemoryFile);
        pdfPath = Paths.get(additionalEnvPath + "/pascalsFile.pdf");
        pdfData = Files.readAllBytes(pdfPath);
        inMemoryFile = new InMemoryFile(pdfData, "/aasx/PascalsFile.pdf");
        fileList.add(inMemoryFile);

        File outputFile = new File(outputEnvPath + "/aasxFromRdf.aasx");
        new AASXSerializer().write(env, fileList, new FileOutputStream(outputFile));
        return "Success!";
    }

    @RequestMapping("/aasxToRdf")
    String convertAasxToRdf() throws Exception {
        File inputFile = new File(outputEnvPath + "/aasxFromRdf.aasx");
        InputStream in = new FileInputStream(inputFile);
        AASXDeserializer deserializer = new AASXDeserializer(in);
        AssetAdministrationShellEnvironment env = deserializer.read();
        List<AssetAdministrationShell> aasList = env.getAssetAdministrationShells();
        System.out.println("#### AAS Environment: " + inputFile);
        aasList.forEach(aas -> System.out.println("Environment contains AAS: " + aas.getIdShort() + " Submodel: " + aas.getSubmodels().stream().count()));

        Path storagePath = Paths.get(outputEnvPath + "/rdfFromAasx.ttl");
        String envToRdfString = new Serializer().serialize(env, RDFLanguages.TURTLE);
        Files.write(storagePath, envToRdfString.getBytes(StandardCharsets.UTF_8));

        return "Success!";
    }

    public static String readFileAsString(String file)throws Exception
    {
        return new String(Files.readAllBytes(Paths.get(file)));
    }
}
