package com.sprint.annotation;

public @interface Link {
    /**
     * The type of the intermediate node, that is either Anonymous or Shared
     * */
    public enum NodeType {
        Anonymous,
        Shared
    }
    /**
     * @return the name of the linking property
     */
    public String propertyName();

    /**
     * @return the type of the node that is either Anonymous or Shared
     */
    public NodeType nodeType() default NodeType.Anonymous;

    /**
     * @return shared ID, when the intermediate entity is important and used in other annotations
     */
    public String sharedID() default "";
}
