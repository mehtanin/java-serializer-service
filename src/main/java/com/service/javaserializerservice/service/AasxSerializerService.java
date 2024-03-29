package com.service.javaserializerservice.service;

import io.adminshell.aas.v3.dataformat.aasx.AASXDeserializer;
import io.adminshell.aas.v3.dataformat.aasx.AASXSerializer;
import io.adminshell.aas.v3.dataformat.aasx.InMemoryFile;
import io.adminshell.aas.v3.dataformat.rdf.Serializer;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import org.apache.jena.riot.RDFLanguages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AasxSerializerService {
    public String convertRdfToAasx(String inputEnvPath, String additionalEnvPath, String outputEnvPath, String fileName) throws Exception {
        String fileToLoad = inputEnvPath + "/" + fileName;
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

        File outputFile = new File(outputEnvPath + "/aasenvironment.aasx");
        new AASXSerializer().write(env, fileList, new FileOutputStream(outputFile));
        return outputFile.getName();
    }

    public String convertAasxToRdf(String inputEnvPath, String outputEnvPath, String fileName) throws Exception {
        File inputFile = new File(inputEnvPath + "/" + fileName);
        InputStream in = new FileInputStream(inputFile);
        AASXDeserializer deserializer = new AASXDeserializer(in);
        AssetAdministrationShellEnvironment env = deserializer.read();
        List<AssetAdministrationShell> aasList = env.getAssetAdministrationShells();
        System.out.println("#### AAS Environment: " + inputFile);
        aasList.forEach(aas -> System.out.println("Environment contains AAS: " + aas.getIdShort() + " Submodel: " + aas.getSubmodels().stream().count()));

        Path storagePath = Paths.get(outputEnvPath + "/aasenvironment.ttl");
        String envToRdfString = new Serializer().serialize(env, RDFLanguages.TURTLE);
        Files.write(storagePath, envToRdfString.getBytes(StandardCharsets.UTF_8));

        return storagePath.getFileName().toString();
    }

    public String convertAasxToRdfRepoPhase(String inputPath, String outputPath, String fileName) throws Exception {
        File inputFile = new File(inputPath + "/" + fileName);
        InputStream in = new FileInputStream(inputFile);
        AASXDeserializer deserializer = new AASXDeserializer(in);
        AssetAdministrationShellEnvironment env = deserializer.read();
        List<AssetAdministrationShell> aasList = env.getAssetAdministrationShells();
        System.out.println("#### AAS Environment: " + inputFile);
        aasList.forEach(aas -> System.out.println("Environment contains AAS: " + aas.getIdShort() + " Submodel: " + aas.getSubmodels().stream().count()));

        Path storagePath = Paths.get(outputPath + fileName.replace(".aasx", ".ttl"));
        String envToRdfString = new Serializer().serialize(env, RDFLanguages.TURTLE);
        Files.write(storagePath, envToRdfString.getBytes(StandardCharsets.UTF_8));

        return storagePath.getParent().toString() + "/" + storagePath.getFileName().toString();
    }

    public static String readFileAsString(String file)throws Exception
    {
        return new String(Files.readAllBytes(Paths.get(file)));
    }
}
