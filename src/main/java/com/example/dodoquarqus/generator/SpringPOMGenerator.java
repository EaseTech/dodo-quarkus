package com.example.dodoquarqus.generator;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

@Component
public class SpringPOMGenerator {

    @Value("${base.dir:/Users/kumar/}")
    public String DIR_NAME;

    public void generate(com.example.dodoquarqus.comment.Component component) throws IOException, XmlPullParserException {
        BufferedReader in = new BufferedReader(new FileReader(getPomPath(component)));
        MavenXpp3Reader r = new MavenXpp3Reader();
        Model model = r.read(in);
        //HAL Explorer
        Dependency dependency = new Dependency();
        dependency.setGroupId("org.springframework.data");
        dependency.setArtifactId("spring-data-rest-hal-explorer");
        model.addDependency(dependency);

        //OPEN API Documentation
        Dependency openAPIDep = new Dependency();
        openAPIDep.setGroupId("org.springdoc");
        openAPIDep.setArtifactId("springdoc-openapi-ui");
        openAPIDep.setVersion("1.5.9");

        model.addDependency(openAPIDep);



        // Actuator
        /**
         * <!-- https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-actuator -->
         * <dependency>
         *     <groupId>org.springframework.boot</groupId>
         *     <artifactId>spring-boot-starter-actuator</artifactId>
         *     <version>2.5.0</version>
         * </dependency>
         */

        Dependency actuator = new Dependency();
        String springBoot = "org.springframework.boot";
        actuator.setGroupId(springBoot);
        actuator.setArtifactId("spring-boot-starter-actuator");

        model.addDependency(actuator);

//        model.addDependency(prometheus);

        /**
         * <dependency>
         *     <groupId>io.micrometer</groupId>
         *     <artifactId>micrometer-registry-prometheus</artifactId>
         * </dependency>
         */

        Dependency micrometer = new Dependency();
        micrometer.setGroupId("io.micrometer");
        micrometer.setArtifactId("micrometer-registry-prometheus");
        model.addDependency(micrometer);

        /**
         * <dependency>
         *   <groupId>org.springframework.boot</groupId>
         *   <artifactId>spring-boot-starter-aop</artifactId>
         * </dependency>
         */
        Dependency aop = new Dependency();
        aop.setGroupId(springBoot);
        aop.setArtifactId("spring-boot-starter-aop");
        model.addDependency(aop);

        /**
         * <dependency>
         *     <groupId>org.springframework.boot</groupId>
         *     <artifactId>spring-boot-starter-validation</artifactId>
         * </dependency>
         */
        Dependency validator = new Dependency();
        validator.setGroupId(springBoot);
        validator.setArtifactId("spring-boot-starter-validation");
        model.addDependency(validator);

        /**
         * <dependency>
         *             <groupId>com.nimbusds</groupId>
         *             <artifactId>oauth2-oidc-sdk</artifactId>
         *         </dependency>
         */
        if(component.getEnableJWT().equalsIgnoreCase("true")) {
            Dependency nimbus = new Dependency();
            nimbus.setGroupId("com.nimbusds");
            nimbus.setArtifactId("oauth2-oidc-sdk");
            model.addDependency(nimbus);
        }

        /**
         * <dependency>
         *     <groupId>org.springframework.boot</groupId>
         *     <artifactId>spring-boot-starter-webflux</artifactId>
         * </dependency>
         */

        Dependency webflux = new Dependency();
        webflux.setGroupId(springBoot);
        webflux.setArtifactId("spring-boot-starter-webflux");
        model.addDependency(webflux);

        /**
         *
         *     <dependency>
         *       <groupId>io.netty</groupId>
         *       <artifactId>netty-resolver-dns-native-macos</artifactId>
         *       <version>4.1.65.Final</version>
         *       <classifier>osx-x86_64</classifier>
         *     </dependency>
         */

        Dependency nettyMacos = new Dependency();
        nettyMacos.setGroupId("io.netty");
        nettyMacos.setArtifactId("netty-resolver-dns-native-macos");
        nettyMacos.setVersion("4.1.65.Final");
        model.addDependency(nettyMacos);
//        nettyMacos.setClassifier("osx-x86_64");

        /**
         *
         * <dependency>
         *     <groupId>io.netty</groupId>
         *     <artifactId>netty-common</artifactId>
         *     <version>4.1.65.Final</version>
         * </dependency>
         */
        Dependency nettyCommon = new Dependency();
        nettyCommon.setGroupId("io.netty");
        nettyCommon.setArtifactId("netty-common");
        nettyCommon.setVersion("4.1.65.Final");
        model.addDependency(nettyCommon);


        Plugin jibPlugin = new Plugin();
        jibPlugin.setGroupId("com.google.cloud.tools");
        jibPlugin.setArtifactId("jib-maven-plugin");
        jibPlugin.setVersion("3.0.0");
        Xpp3Dom configuration = new Xpp3Dom("configuration");
        final Xpp3Dom to = new Xpp3Dom( "to" );
        final Xpp3Dom image = new Xpp3Dom( "image" );
        image.setValue(component.getName());
        to.addChild(image);
        configuration.addChild(to);
        jibPlugin.setConfiguration(configuration);
        model.getBuild().addPlugin(jibPlugin);

        MavenXpp3Writer writer = new MavenXpp3Writer();
        writer.write(new FileOutputStream(getPomPath(component)), model);



    }

    public String getPomPath(com.example.dodoquarqus.comment.Component component) {
        return DIR_NAME.concat(component.getName()).concat("/pom.xml");
    }
}