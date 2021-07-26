package com.example.dodoquarqus.generator;


import com.example.dodoquarqus.comment.Component;
import com.example.dodoquarqus.comment.ComponentsClass;
import com.google.common.base.CaseFormat;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static com.example.dodoquarqus.processor.ProcessorUtil.getMethodList;

@Getter
@Setter
public abstract class Generator extends FreemarkerConfig {



    @Value("${base.dir:/Users/kumar/}")
    public String DIR_NAME;

    public static final String FORWARD_SLASH = "/";


    public Generator(String basePackagePath) {
        super(basePackagePath);

    }

    public String getComponentPath(Component component) {
        return DIR_NAME.concat(component.getName());
    }

    public String generate(ComponentsClass classToGenerate) {

        Map<String, Object> inputData = classToGenerate.getInputData();
        inputData.put("methods", getMethodList(classToGenerate.getMethods()));
        String dirName = DIR_NAME.concat(classToGenerate.getComponentName()).concat(BASE_WORKING_DIR.concat(classToGenerate.getComponentName().toLowerCase().concat(classToGenerate.getClassType().getPath())));
        File dir = new File(dirName);
        if(! dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dir, CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, classToGenerate.getName().concat(classToGenerate.getClassType().getClassNameSuffix())));
        if(file.exists()) {
            if(classToGenerate.isOverride()) {
                createFile(file,inputData, classToGenerate.getClassType().getFtl());
            }else {
                updateFile(file, inputData);
            }

        }else {
            createFile(file, classToGenerate.getInputData(), classToGenerate.getClassType().getFtl());
        }
        return dir.getAbsolutePath().concat(FORWARD_SLASH).concat(classToGenerate.getName().concat(classToGenerate.getClassType().getClassNameSuffix()));
    }

    public String createFile(File file, Map<String, Object> inputData, String templateName) {

        if(templateName == null || templateName.isEmpty()){
            return "";
        }
        try {
            FileUtils.touch(file);
            Template template = cfg.getTemplate(templateName);

            Writer fileWriter = new FileWriter(file);
            template.process(inputData, fileWriter);
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    public String updateFile(File file, Map<String, Object> inputData) {

        try {
            List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));
            ListIterator<String> listIterator = lines.listIterator(lines.size());
            while (listIterator.hasPrevious()) {
                String value = listIterator.previous();
                if(value != null && value.trim().equals("}")){
                    listIterator.remove();
                    break;
                }

            }
            String newMethods = (String)inputData.get("methods");
            lines.add(System.lineSeparator());
            lines.add(newMethods);
            lines.add(System.lineSeparator());
            lines.add("}");
            FileWriter fw = new FileWriter(file);
            for(String data: lines) {
                if(data == null || data.isEmpty()) {
                    continue;
                }
//                fw.write(System.lineSeparator());
                fw.write(data);
                fw.write(System.lineSeparator());
            }
            fw.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return file.getAbsolutePath();
    }



}
