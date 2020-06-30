package com.sprint;

import org.glassfish.jaxb.core.api.impl.NameConverter;


public class Main {

    public static void main(String[] args) {
        if (args[0].equalsIgnoreCase("uri"))
            URIConvert(args[1]);
        else if (args.length == 1)
            URIConvert(args[0]);
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