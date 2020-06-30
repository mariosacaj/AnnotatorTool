package com.sprint.annotation;

public @interface RdfProperty {

    /**
     * @return the list of @Link that is used to create a chain of triples */
    public Link[] links () default {};
    /**
     * @return the name of the last property in the chain */
    public String propertyName();

    /**
     * @return true if the annotated attribute contains more than one value (is a list) */
    public boolean isList() default false;

    /**
     * @return the hard coded object for the last triple in the chain
     * If the value is set the annotated attribute must not have @RdfsClass in its Java class.
     * */
    public String value() default "";

    /**
     * @return the type of the hard coded object specified by the value
     * property
     * */
    public String dataType() default "String";

}
