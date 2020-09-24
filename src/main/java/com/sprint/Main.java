package com.sprint;

import com.sprint.annotation.model.JavaCodeMan;
import org.glassfish.jaxb.core.api.impl.NameConverter;


public class Main {

    public static void main(String[] args) throws Exception {
        // Use tool as URI to PackageName converter
        if (args[0].equalsIgnoreCase("uri")) {
            URIConvert(args[1]);
            return;
        }

        // args = "xsdFilePath outputDir standard_concept reference_concept reference_type(P/C)"
        // example: "file.xsd output/ FareTravel st4rt:hasFareTravel P"
        JavaCodeMan jcm = new JavaCodeMan(args[1]);
        jcm.generateFromSchema(args[0]);
        jcm.annotate(args[2], args[3]);
        jcm.writeDownAnnotation(args[2], args[3], args[4].charAt(0));
        jcm.insertNamespaces(new String[]{"xml","http://www.w3.org/XML/1998/namespace","rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#"});
        System.out.println(jcm.test());
        jcm.build();

    }

    /**
     * from http://domainmodel.pts_fsm.org/2015/10/29/transportation
     * to org.pts_fsm.domainmodel._2015._10._29.transportation
     *
     * @param URI URI to convert into package namespace
     */
    public static void URIConvert(String URI) {
        System.out.println(NameConverter.standard.toPackageName(URI));
    }

}