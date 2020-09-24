package com.sprint.annotation.model;

import com.sprint.annotation.NameSpaces;
import com.sprint.annotation.RdfProperty;
import com.sprint.annotation.RdfsClass;
import com.sun.codemodel.*;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.XJC;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.glassfish.jaxb.core.api.impl.NameConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class JavaCodeMan {
    private JCodeModel jcm = null;
    private final File targetPath;
    private Iterator<JDefinedClass> classes= null;
    private JPackage pck = null;

    /**
     * Java code manipulator is instantiated
     *
     * @param targetPath directory where class source will be generated
     */
    public JavaCodeMan(final String targetPath)  {
        this.targetPath = new File(targetPath);
    }


    /**
     * Generates JCodeModel and stores it for future annotation and final build/write down to targetPath
     *
     * @param schemaPath File path related to the xsd standard
     * @throws Exception failure during model generation
     */
    public void generateFromSchema(final String schemaPath) throws Exception {
        File schemaFile = new File(schemaPath);
        final SchemaCompiler sc = XJC.createSchemaCompiler();
        final FileInputStream schemaStream = new FileInputStream(schemaFile);
        final InputSource is = new InputSource(schemaStream);
        is.setSystemId(schemaFile.toURI().toString());

        sc.parseSchema(is);
        sc.setDefaultPackageName(null);

        final S2JJAXBModel s2 = sc.bind();
        this.jcm =  s2.generateCode(null, null);

        setPackageName(find_URI(schemaFile));

    }

    /**
     * Find targetNamespace URI of the actual standard file selected
     *
     * @param schemaFile File object related to the xsd standard file
     */
    private String find_URI(File schemaFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(schemaFile);
        NodeList list = doc.getElementsByTagName("xsd:schema");
        Element node = (Element)list.item(0);

        return node.getAttribute("targetNamespace");
    }


    /**
     * Before actual annotation we need to identify the correct package
     * amongst all the packages produced by the schema compiler
     *
     * @param URI namespace of schemaFile
     */
    private void setPackageName(String URI) {
        String package_name = NameConverter.standard.toPackageName(URI);
        Iterator<JPackage> itr = this.jcm.packages();
        while (itr.hasNext()) {
            JPackage t = itr.next();
            if (!t.name().equals(package_name))
                itr.remove();
        }
        this.pck = this.jcm._package(package_name);
        this.loadClasses();

    }

    /**
     * Loads classes of the package to be annotated
     */
    private void loadClasses() {
        Iterator<JDefinedClass> itr = this.pck.classes();
        ArrayList<JDefinedClass> jDefinedClasses = new ArrayList<>();

        while (itr.hasNext()) {
            JDefinedClass t = itr.next();
            if (!t.name().equals("ObjectFactory"))
                jDefinedClasses.add(t);
        }
        this.classes = jDefinedClasses.iterator();
    }

    /**
     * Write down of the annotated JCodeModel to targetPath
     *
     * @throws IOException failure during build
     */
    public void build() throws IOException {
        try (PrintStream status = new PrintStream(new ByteArrayOutputStream())) {
            this.jcm.build(this.targetPath, status);
        }
    }

    /**
     * Insert namespaces annotation into the Java Code Model, on every complex class
     *
     * @param list_pref_ns list of "ungrouped" prefix - namespace pairs, e.g.:
     *
     *     "xml",
     *     "http://www.w3.org/XML/1998/namespace",
     *     "rdf",
     *     "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     *
     *     MUST FOLLOW THIS ORDER: prefix_1, namespace_1, prefix_2, namespace_2, ...
     */
    public void insertNamespaces(String[] list_pref_ns) {
        Iterator<JDefinedClass> itr = this.classes;

        while (itr.hasNext()) {
            JDefinedClass jclass = itr.next();
            JAnnotationUse annotation = jclass.annotate(jcm.ref(NameSpaces.class));

            JAnnotationArrayMember annotationArrayMember = annotation.paramArray("value");
            for (String paramValue : list_pref_ns) {
                annotationArrayMember.param(paramValue);
            }

        }
        this.loadClasses();
    }

    /**
     * Insert confirmed mapping as a annotation into the JCodeModel tree
     *
     * @param standard_name element to map to concept (ex. 'fareTravelUrl')
     * @param reference_name the reference concept in the target ontology (ex. 'st4rt:Travel')
     * @param reference_type type of concept, must be 'C' if reference_name is a class
     *                       in the ontology, 'P' if it is a property
     * @throws ClassNotFoundException cannot find element to map
     * @throws InputMismatchException trying to annotate a class as if it is a property or vice versa
     */
    public void writeDownAnnotation(String standard_name, String reference_name, char reference_type) throws ClassNotFoundException, InputMismatchException {
        JAnnotatable annotatable = this.searchByName(standard_name);
        if (annotatable instanceof JDefinedClass && reference_type == 'C')
            annotatable.annotate(jcm.ref(RdfsClass.class)).param("value", reference_name);
        else if (annotatable instanceof JFieldVar && reference_type == 'P')
            annotatable.annotate(jcm.ref(RdfProperty.class)).param("propertyName", reference_name);
        else
            throw new InputMismatchException();

    }

    /**
     * Insert confirmed mapping as a annotation into the JCodeModel tree, NO CHECK
     *
     * @param standard_name element to map to concept (ex. 'fareTravelUrl')
     * @param reference_name the reference concept in the target ontology (ex. 'st4rt:Travel')
     *
     * @throws ClassNotFoundException cannot find element to map
     */
    public void annotate(String standard_name, String reference_name) throws ClassNotFoundException {
        JAnnotatable annotatable = this.searchByName(standard_name);
        if (annotatable instanceof JDefinedClass)
            annotatable.annotate(jcm.ref(RdfsClass.class)).param("value", reference_name);
        else if (annotatable instanceof JFieldVar) {
            JAnnotationUse annotation = annotatable.annotate(jcm.ref(RdfProperty.class));
            annotation.param("propertyName", reference_name);
            if (((JFieldVar) annotatable).type() instanceof JClass)
                if (((JClass)((JFieldVar) annotatable).type()).getTypeParameters().size() > 0)
                    annotation.param("isList", true);
        }
    }

    private JAnnotatable searchByName(String name) throws ClassNotFoundException {
        JAnnotatable annotable = getClassByName(name, classes);
        this.loadClasses();
        if (annotable == null)
            throw new ClassNotFoundException();
        else {
            return annotable;
        }
    }


    private JAnnotatable getClassByName(String name, Iterator<JDefinedClass> itr) {
        while (itr.hasNext()) {
            JDefinedClass jclass = itr.next();
            JAnnotationUse ju;
            if ((ju = getAnnotation(jclass, jcm.ref(XmlType.class))) != null) {
                if (annotationEqualsName(ju, jclass.name(), name))
                    return jclass;
            }

            JAnnotatable field = getFieldByName(name, jclass);
            if (field != null) return field;


            JAnnotatable clazz = getClassByName(name, jclass.classes());
            if (clazz != null) return clazz;

        }
        return null;
    }



    private JAnnotatable getFieldByName(String name, JDefinedClass jclass) {
        JAnnotationUse ju;
        Map<String, JFieldVar> fields = jclass.fields();
        // using for-each loop for iteration over Map.entrySet()
        for (Map.Entry<String,JFieldVar> entry : fields.entrySet()) {
            JFieldVar field = entry.getValue();
            if ((ju = getAnnotation(field, jcm.ref(XmlElement.class))) != null) {
                if (annotationEqualsName(ju, entry.getKey(), name))
                    return field;
            }
            if ((ju = getAnnotation(field, jcm.ref(XmlAttribute.class))) != null) {
                if (annotationEqualsName(ju, entry.getKey(), name))
                    return field;
            }
        }
        return null;
    }


    protected static Boolean annotationEqualsName(JAnnotationUse ju, String elem_name, String concept_name) {
        JAnnotationValue ns = ju.getAnnotationMembers().get("name");
        if (ns == null) {
            return elem_name.equals(concept_name);
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        JFormatter jf = new JFormatter(pw, "");
        ns.generate(jf);
        pw.flush();
        String s = sw.toString();
        return s.substring(1, s.length()-1).equals(concept_name);
    }

    protected static JAnnotationUse getAnnotation(JAnnotatable annotatable, JClass annotationClass) {
        for (JAnnotationUse annotation : annotatable.annotations()) {
            if (annotation.getAnnotationClass().equals(annotationClass)) {
                return annotation;
            }
        }
        return null;
    }

    public int test() {
        if (jcm != null) {
            return jcm.countArtifacts();
        }
        return -1;
    }

}


