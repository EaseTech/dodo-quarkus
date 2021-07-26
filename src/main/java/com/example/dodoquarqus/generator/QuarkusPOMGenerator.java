package com.example.dodoquarqus.generator;

import com.example.dodoquarqus.comment.Component;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

@org.springframework.stereotype.Component
public class QuarkusPOMGenerator {

    @Value("${base.dir}")
    public String DIR_NAME;

    public void handlePom(Component component) throws IOException, XmlPullParserException {
        BufferedReader in = new BufferedReader(new FileReader(getPomPath(component)));
        MavenXpp3Reader r = new MavenXpp3Reader();

        Model model = r.read(in);

        /**
         * <dependency>
         *             <groupId>commons-io</groupId>
         *             <artifactId>commons-io</artifactId>
         *             <version>2.10.0</version>
         *         </dependency>
         */
        Dependency commonsIO = new Dependency();
        commonsIO.setGroupId("commons-io");
        commonsIO.setArtifactId("commons-io");
        commonsIO.setVersion("2.10.0");
        model.addDependency(commonsIO);

        /**
         * <dependency>
         *             <groupId>org.jboss.logmanager</groupId>
         *             <artifactId>log4j2-jboss-logmanager</artifactId>
         *         </dependency>
         */
        Dependency log4j = new Dependency();
        log4j.setGroupId("org.jboss.logmanager");
        log4j.setArtifactId("log4j2-jboss-logmanager");
        model.addDependency(log4j);

        if(component.getBackend().getType().equalsIgnoreCase("es")){
            /**
             * <dependency>
             *             <groupId>org.elasticsearch.client</groupId>
             *             <artifactId>elasticsearch-rest-high-level-client</artifactId>
             *         </dependency>
             */
            Dependency es_reactive = new Dependency();
            es_reactive.setGroupId("org.elasticsearch.client");
            es_reactive.setArtifactId("elasticsearch-rest-high-level-client");
            model.addDependency(es_reactive);

        }


        /**
         * <dependency>
         *             <groupId>org.apache.commons</groupId>
         *             <artifactId>commons-lang3</artifactId>
         *             <version>3.12.0</version>
         *         </dependency>
         */
        Dependency commonsLang = new Dependency();
        commonsLang.setGroupId("org.apache.commons");
        commonsLang.setArtifactId("commons-lang3");
        commonsLang.setVersion("3.12.0");
        model.addDependency(commonsLang);

        /**
         * <dependency>
         *     <groupId>org.projectlombok</groupId>
         *     <artifactId>lombok</artifactId>
         *     <version>1.18.20</version>
         *     <scope>provided</scope>
         * </dependency>
         */
        Dependency lombok = new Dependency();
        lombok.setGroupId("org.projectlombok");
        lombok.setArtifactId("lombok");
        lombok.setVersion("1.18.20");
        lombok.setScope("provided");
        model.addDependency(lombok);


        /**
         * <dependency>
         *     <groupId>io.quarkus</groupId>
         *     <artifactId>quarkus-smallrye-jwt</artifactId>
         * </dependency>
         * <dependency>
         *     <groupId>io.quarkus</groupId>
         *     <artifactId>quarkus-smallrye-jwt-build</artifactId>
         * </dependency>
         */
        Dependency jwt = new Dependency();
        jwt.setGroupId("io.quarkus");
        jwt.setArtifactId("quarkus-smallrye-jwt");
        model.addDependency(jwt);

        Dependency jwtBuild = new Dependency();
        jwtBuild.setGroupId("io.quarkus");
        jwtBuild.setArtifactId("quarkus-smallrye-jwt-build");
        model.addDependency(jwtBuild);

        /**
         * <dependency>
         *     <groupId>io.quarkus</groupId>
         *     <artifactId>quarkus-smallrye-openapi</artifactId>
         * </dependency>
         */

        Dependency openAPI = new Dependency();
        openAPI.setGroupId("io.quarkus");
        openAPI.setArtifactId("quarkus-smallrye-openapi");
        model.addDependency(openAPI);

        if(component.isAsync()){
            /**
             * <dependency>
             *       <groupId>io.quarkus</groupId>
             *       <artifactId>quarkus-resteasy-reactive</artifactId>
             *     </dependency>
             *
             *     <dependency>
             *       <groupId>io.smallrye.reactive</groupId>
             *       <artifactId>mutiny-reactor</artifactId>
             *     </dependency>
             */
            Dependency restEasyReactive = new Dependency();
            restEasyReactive.setGroupId("io.quarkus");
            restEasyReactive.setArtifactId("quarkus-resteasy-reactive");
            model.addDependency(restEasyReactive);

            /**
             * <dependency>
             *             <groupId>io.quarkus</groupId>
             *             <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
             *         </dependency>
             */
            Dependency reactiveJackson = new Dependency();
            reactiveJackson.setGroupId("io.quarkus");
            reactiveJackson.setArtifactId("quarkus-resteasy-reactive-jackson");
            model.addDependency(reactiveJackson);

            /**
             * <dependency>
             *             <groupId>com.fasterxml.jackson.core</groupId>
             *             <artifactId>jackson-databind</artifactId>
             *         </dependency>
             */
            Dependency jacksonDataBind = new Dependency();
            jacksonDataBind.setGroupId("com.fasterxml.jackson.core");
            jacksonDataBind.setArtifactId("jackson-databind");
            model.addDependency(jacksonDataBind);

            /**
             * <dependency>
             *             <groupId>io.projectreactor</groupId>
             *             <artifactId>reactor-core</artifactId>
             *         </dependency>
             */

            Dependency reactor = new Dependency();
            reactor.setGroupId("io.projectreactor");
            reactor.setArtifactId("reactor-core");
            model.addDependency(reactor);
        }



        MavenXpp3Writer writer = new MavenXpp3Writer();
        writer.write(new FileOutputStream(getPomPath(component)), model);


    }

    public String getPomPath(Component component) {
        return DIR_NAME.concat(component.getName()).concat("/pom.xml");
    }
}