package dk.sgjesse.r8api;

import com.android.tools.r8.ByteDataView;

import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static dk.sgjesse.r8api.FileUtils.CLASS_EXTENSION;
import static dk.sgjesse.r8api.FileUtils.MODULE_INFO_CLASS;

/**
 * Utilities for ZIP files.
 */
class ZipUtils {
    public static boolean isClassFile(String entry) {
        String name = entry.toLowerCase();
        if (name.endsWith(MODULE_INFO_CLASS)) {
            return false;
        }
        if (name.startsWith("meta-inf") || name.startsWith("/meta-inf")) {
            return false;
        }
        return name.endsWith(CLASS_EXTENSION);
    }

    public static void writeToZipStream(
            ZipOutputStream stream, String entry, ByteDataView content, int compressionMethod)
            throws IOException {
        byte[] buffer = content.getBuffer();
        int offset = content.getOffset();
        int length = content.getLength();
        CRC32 crc = new CRC32();
        crc.update(buffer, offset, length);
        ZipEntry zipEntry = new ZipEntry(entry);
        zipEntry.setMethod(compressionMethod);
        zipEntry.setSize(length);
        zipEntry.setCrc(crc.getValue());
        zipEntry.setTime(0);
        stream.putNextEntry(zipEntry);
        stream.write(buffer, offset, length);
        stream.closeEntry();
    }
}
