package dk.sgjesse.r8api;

import static dk.sgjesse.r8api.FileUtils.CLASS_EXTENSION;

/**
 * Utilities for descriptors.
 */
class DescriptorUtils {
    public static final char JAVA_PACKAGE_SEPARATOR = '.';

    public static String guessTypeDescriptor(String name) {
        assert name != null;
        assert name.endsWith(CLASS_EXTENSION) :
                "Name " + name + " must have " + CLASS_EXTENSION + " suffix";
        String descriptor = name.substring(0, name.length() - CLASS_EXTENSION.length());
        if (descriptor.indexOf(JAVA_PACKAGE_SEPARATOR) != -1) {
            throw new RuntimeException("Unexpected class file name: " + name);
        }
        return 'L' + descriptor + ';';
    }
}