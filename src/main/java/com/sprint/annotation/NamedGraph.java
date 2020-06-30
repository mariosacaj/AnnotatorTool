package com.sprint.annotation;

public @interface NamedGraph {
    /**
     * What kind of named graph scheme to use when persisting RDF
     * */
    public enum NamedGraphType {
        /**
         * Persist to a named graph with the same URI as the individual being persisted
         * */
        Instance,
        /**
         * Persist to a specific named graph
         * */
        Static,
    }
    /**
     * The NamedGraph persistence type * @return the type
     */
    public NamedGraphType type() default NamedGraphType.Instance;
    /**
     * The URI of the named graph to persist to * @return the named graph URI
     */
    public String value() default "http://st4rt.eu/ontologies/st4rt/message";
}
